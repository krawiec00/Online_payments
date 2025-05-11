package com.exercise;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testApplyDiscount() {
        BigDecimal value = new BigDecimal("200.00");
        int discount = 10;
        BigDecimal result = invokeApplyDiscount(value, discount);
        assertEquals(new BigDecimal("180.00"), result);
    }

    @Test
    void testChooseBestMethodPromotionBeatsPoints() {
        Order order = new Order();
        order.id = "O1";
        order.value = new BigDecimal("100.00");
        order.promotions = List.of("PROMO1");

        PaymentMethod promo = new PaymentMethod();
        promo.id = "PROMO1";
        promo.discount = 20;
        promo.limit = new BigDecimal("500");

        PaymentMethod points = new PaymentMethod();
        points.id = "PUNKTY";
        points.discount = 15;
        points.limit = new BigDecimal("500");

        Map<String, PaymentMethod> paymentMap = new HashMap<>();
        paymentMap.put("PROMO1", promo);
        paymentMap.put("PUNKTY", points);

        Main.PaymentDecision decision = invokeChooseBestMethod(order, paymentMap);
        assertEquals("PROMO1", decision.method);
        assertEquals(20, decision.discount);
    }

    @Test
    void testApplyPaymentPartialPoints() {
        Order order = new Order();
        order.id = "O2";
        order.value = new BigDecimal("100.00");

        PaymentMethod points = new PaymentMethod();
        points.id = "PUNKTY";
        points.discount = 15;
        points.limit = new BigDecimal("50.00");

        PaymentMethod backup = new PaymentMethod();
        backup.id = "CARD";
        backup.discount = 0;
        backup.limit = new BigDecimal("200.00");

        Map<String, PaymentMethod> paymentMap = new HashMap<>();
        paymentMap.put("PUNKTY", points);
        paymentMap.put("CARD", backup);

        Map<String, BigDecimal> used = new HashMap<>();
        Main.PaymentDecision decision = new Main.PaymentDecision("PUNKTY_PARTIAL", 10);

        Main.applyPayment(order, decision, paymentMap, used);

        assertEquals(new BigDecimal("10.00"), used.get("PUNKTY"));
        assertEquals(new BigDecimal("81.00"), used.get("CARD"));
        assertEquals(new BigDecimal("40.00"), points.limit); // 50 - 10
        assertEquals(new BigDecimal("119.00"), backup.limit); // 200 - 81
    }

    private BigDecimal invokeApplyDiscount(BigDecimal val, int disc) {
        return Main.applyDiscount(val, disc);
    }

    private Main.PaymentDecision invokeChooseBestMethod(Order order, Map<String, PaymentMethod> paymentMap) {
        return Main.chooseBestMethod(order, paymentMap);
    }
}
