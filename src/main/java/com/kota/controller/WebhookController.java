package com.kota.controller;

import com.kota.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping("/yoco")
    public ResponseEntity<Void> handleYocoWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Yoco-Signature", required = false) String signature) {
        if (signature == null) {
            log.warn("Yoco webhook received without signature");
            return ResponseEntity.badRequest().build();
        }
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
