package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.Payment;
import com.psychology.booking.service.PaymentService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<Payment> createPayment(@RequestBody Map<String, Object> params,
                                         @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Long bookingId = Long.valueOf(params.get("bookingId").toString());
        String paymentMethod = params.getOrDefault("paymentMethod", "mock").toString();
        
        return Result.success(paymentService.createPayment(bookingId, userId, paymentMethod));
    }

    /**
     * 模拟支付回调（仅用于测试）
     */
    @PostMapping("/mock-callback")
    public Result<Payment> mockCallback(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo").toString();
        boolean success = Boolean.parseBoolean(params.getOrDefault("success", "true").toString());
        
        return Result.success(paymentService.mockPaymentCallback(orderNo, success));
    }

    @PostMapping("/{id}/refund")
    public Result<Payment> refund(@PathVariable Long id,
                                  @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(paymentService.refund(id, userId));
    }

    @GetMapping("/order/{orderNo}")
    public Result<Payment> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(paymentService.getByOrderNo(orderNo));
    }
}
