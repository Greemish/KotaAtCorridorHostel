package com.kota.service;

import com.kota.config.YocoConfig;
import com.kota.dto.request.ProcessRefundRequest;
import com.kota.dto.response.PaymentTransactionResponse;
import com.kota.exception.PaymentException;
import com.kota.exception.ResourceNotFoundException;
import com.kota.model.Order;
import com.kota.model.PaymentTransaction;
import com.kota.model.Refund;
import com.kota.model.WebhookEvent;
import com.kota.repository.OrderRepository;
import com.kota.repository.PaymentTransactionRepository;
import com.kota.repository.RefundRepository;
import com.kota.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final YocoConfig yocoConfig;
    private final RestTemplate yocoRestTemplate;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final NotificationService notificationService;

    @Transactional
    public String createCheckoutSession(Order order) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setAmount(order.getTotalAmount());
        transaction.setYocoFee(calculateYocoFee(order.getTotalAmount()));
        transaction.setCurrency("ZAR");
        transaction.setStatus(PaymentTransaction.Status.PENDING);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(yocoConfig.getSecretKey());

            Map<String, Object> body = new HashMap<>();
            body.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            body.put("currency", "ZAR");
            body.put("successUrl", "/payment/success?orderId=" + order.getId());
            body.put("cancelUrl", "/payment/cancel?orderId=" + order.getId());
            body.put("failureUrl", "/payment/failure?orderId=" + order.getId());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = yocoRestTemplate.postForEntity(
                    yocoConfig.getCheckoutUrl(), request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> responseBody = response.getBody();
                String checkoutId = (String) responseBody.get("id");
                String redirectUrl = (String) responseBody.get("redirectUrl");
                transaction.setYocoCheckoutId(checkoutId);
                paymentTransactionRepository.save(transaction);
                return redirectUrl;
            }
        } catch (Exception e) {
            log.warn("Yoco API unavailable, using mock checkout for order {}: {}", order.getOrderNumber(), e.getMessage());
        }

        // Mock checkout URL for development/testing
        paymentTransactionRepository.save(transaction);
        return "/payment/success?orderId=" + order.getId() + "&mock=true";
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (!verifyWebhookSignature(payload, signature)) {
            throw new PaymentException("Invalid webhook signature");
        }

        try {
            // Parse event ID and type from payload (simplified JSON parsing)
            String eventId = extractJsonField(payload, "id");
            String eventType = extractJsonField(payload, "type");

            if (webhookEventRepository.existsByEventId(eventId)) {
                log.info("Duplicate webhook event received: {}", eventId);
                return;
            }

            WebhookEvent event = new WebhookEvent();
            event.setEventId(eventId);
            event.setEventType(eventType);
            event.setPayload(payload);
            webhookEventRepository.save(event);

            if ("payment.succeeded".equals(eventType)) {
                processPaymentSucceeded(payload);
            } else if ("payment.failed".equals(eventType)) {
                processPaymentFailed(payload);
            }

            event.setProcessed(true);
            webhookEventRepository.save(event);
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            throw new PaymentException("Webhook processing failed: " + e.getMessage());
        }
    }

    private void processPaymentSucceeded(String payload) {
        String paymentId = extractJsonField(payload, "paymentId");
        String checkoutId = extractJsonField(payload, "checkoutId");

        paymentTransactionRepository.findByYocoCheckoutId(checkoutId).ifPresent(transaction -> {
            transaction.setStatus(PaymentTransaction.Status.SUCCEEDED);
            transaction.setYocoPaymentId(paymentId);
            paymentTransactionRepository.save(transaction);

            Order order = transaction.getOrder();
            order.setStatus(Order.Status.PAID);
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);
            notificationService.sendOrderStatusUpdate(order);
            notificationService.sendNewOrderToStaff(order);
        });
    }

    private void processPaymentFailed(String payload) {
        String checkoutId = extractJsonField(payload, "checkoutId");
        paymentTransactionRepository.findByYocoCheckoutId(checkoutId).ifPresent(transaction -> {
            transaction.setStatus(PaymentTransaction.Status.FAILED);
            paymentTransactionRepository.save(transaction);

            Order order = transaction.getOrder();
            order.setStatus(Order.Status.CANCELLED);
            orderRepository.save(order);
        });
    }

    @Transactional
    public void processRefund(ProcessRefundRequest request, String performedBy) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(request.getPaymentTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", request.getPaymentTransactionId()));

        Refund refund = new Refund();
        refund.setPaymentTransaction(transaction);
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setProcessedBy(performedBy);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(yocoConfig.getSecretKey());

            Map<String, Object> body = new HashMap<>();
            body.put("paymentId", transaction.getYocoPaymentId());
            body.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());

            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = yocoRestTemplate.postForEntity(
                    yocoConfig.getRefundUrl(), httpRequest, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                refund.setYocoRefundId((String) response.getBody().get("id"));
                refund.setStatus("SUCCEEDED");
            } else {
                refund.setStatus("FAILED");
            }
        } catch (Exception e) {
            log.error("Refund API error: {}", e.getMessage());
            refund.setStatus("FAILED");
        }

        refundRepository.save(refund);
    }

    public List<PaymentTransactionResponse> getPaymentTransactions() {
        return paymentTransactionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentTransactionResponse getTransactionById(Long id) {
        return paymentTransactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));
    }

    public BigDecimal calculateYocoFee(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.0295")).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    yocoConfig.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int colonIdx = json.indexOf(":", idx + search.length());
        if (colonIdx == -1) return "";
        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;
        if (valueStart >= json.length()) return "";
        if (json.charAt(valueStart) == '"') {
            int valueEnd = json.indexOf("\"", valueStart + 1);
            return valueEnd == -1 ? "" : json.substring(valueStart + 1, valueEnd);
        }
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd == -1) valueEnd = json.indexOf("}", valueStart);
        return valueEnd == -1 ? json.substring(valueStart).trim() : json.substring(valueStart, valueEnd).trim();
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction pt) {
        PaymentTransactionResponse r = new PaymentTransactionResponse();
        r.setId(pt.getId());
        r.setOrderId(pt.getOrder().getId());
        r.setOrderNumber(pt.getOrder().getOrderNumber());
        r.setAmount(pt.getAmount());
        r.setYocoFee(pt.getYocoFee());
        r.setStatus(pt.getStatus().name());
        r.setCardLast4(pt.getCardLast4());
        r.setCreatedAt(pt.getCreatedAt());
        return r;
    }
}
