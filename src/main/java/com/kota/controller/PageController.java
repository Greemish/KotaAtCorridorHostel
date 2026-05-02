package com.kota.controller;

import com.kota.model.Order;
import com.kota.model.User;
import com.kota.repository.OrderRepository;
import com.kota.security.UserPrincipal;
import com.kota.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MenuService menuService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final UserService userService;
    private final FinancialService financialService;
    private final PaymentService paymentService;
    private final QueueService queueService;
    private final OrderRepository orderRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/student/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public String studentDashboard(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        List<Order> recentOrders = orderRepository.findByStudentIdOrderByCreatedAtDesc(principal.getId());
        model.addAttribute("orders", recentOrders);
        model.addAttribute("menuItems", menuService.getAllAvailableItems());
        return "student/dashboard";
    }

    @GetMapping("/student/menu")
    @PreAuthorize("hasRole('STUDENT')")
    public String menuPage(Model model) {
        model.addAttribute("menuItems", menuService.getAllAvailableItems());
        return "student/menu";
    }

    @GetMapping("/student/cart")
    @PreAuthorize("hasRole('STUDENT')")
    public String cartPage(Model model) {
        model.addAttribute("menuItems", menuService.getAllAvailableItems());
        return "student/cart";
    }

    @GetMapping("/student/orders/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public String orderStatus(@PathVariable Long id, Model model, @AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "student/order-status";
    }

    @GetMapping("/student/orders")
    @PreAuthorize("hasRole('STUDENT')")
    public String orderHistory(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("orders", orderService.getMyOrders(principal.getId()));
        return "student/order-history";
    }

    @GetMapping("/staff/dashboard")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String staffDashboard(Model model) {
        model.addAttribute("orders", orderService.getPaidOrders());
        model.addAttribute("stockAlerts", inventoryService.getLowStockAlerts());
        return "staff/dashboard";
    }

    @GetMapping("/staff/queue")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String queuePage(Model model) {
        model.addAttribute("queue", queueService.buildQueueResponse());
        return "staff/queue";
    }

    @GetMapping("/staff/stock-alerts")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String stockAlerts(Model model) {
        model.addAttribute("alerts", inventoryService.getLowStockAlerts());
        return "staff/stock-alerts";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("menuItems", menuService.getAllItems());
        model.addAttribute("stockAlerts", inventoryService.getLowStockAlerts());
        model.addAttribute("todayReport", financialService.getDailyReport(LocalDate.now()));
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/menu")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminMenuPage(Model model) {
        model.addAttribute("menuItems", menuService.getAllItems());
        model.addAttribute("ingredients", inventoryService.getAllIngredients());
        return "admin/menu";
    }

    @GetMapping("/admin/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public String inventoryPage(Model model) {
        model.addAttribute("ingredients", inventoryService.getAllIngredients());
        model.addAttribute("alerts", inventoryService.getLowStockAlerts());
        return "admin/inventory";
    }

    @GetMapping("/admin/financials")
    @PreAuthorize("hasRole('ADMIN')")
    public String financialsPage(Model model,
                                  @RequestParam(required = false) String from,
                                  @RequestParam(required = false) String to) {
        LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusDays(30);
        LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
        model.addAttribute("reports", financialService.getPeriodReport(fromDate, toDate));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "admin/financials";
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public String paymentsPage(Model model) {
        model.addAttribute("transactions", paymentService.getPaymentTransactions());
        return "admin/payments";
    }

    @GetMapping("/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String settingsPage() {
        return "admin/settings";
    }

    @GetMapping("/admin/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public String auditLogsPage(Model model) {
        return "admin/audit-logs";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "payment/success";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "payment/cancel";
    }

    @GetMapping("/payment/failure")
    public String paymentFailure(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "payment/failure";
    }
}
