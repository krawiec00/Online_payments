package com.exercise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


class Order {
    public String id;
    public BigDecimal value;
    public List<String> promotions;
}

class PaymentMethod {
    public String id;
    public int discount;
    public BigDecimal limit;
}

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }

        ObjectMapper mapper = new ObjectMapper();
        Order[] orders = mapper.readValue(new File(args[0]), Order[].class);
        PaymentMethod[] methods = mapper.readValue(new File(args[1]), PaymentMethod[].class);

        Map<String, PaymentMethod> paymentMap = Arrays.stream(methods)
                .collect(Collectors.toMap(pm -> pm.id, pm -> pm));

        Map<String, BigDecimal> usedAmounts = new HashMap<>();

        for (Order order : orders) {
            PaymentDecision decision = chooseBestMethod(order, paymentMap);
            applyPayment(order, decision, paymentMap, usedAmounts);
        }

        printUsedAmounts(usedAmounts);
    }

    static PaymentDecision chooseBestMethod(Order order, Map<String, PaymentMethod> paymentMap) {
        BigDecimal orderValue = order.value;
        String chosenMethod = null;
        BigDecimal discountedValue = orderValue;
        int appliedDiscount = 0;

        // 1. Promocje z order.promotions
        if (order.promotions != null) {
            for (String promotion : order.promotions) {
                PaymentMethod pm = paymentMap.get(promotion);
                if (pm != null && pm.limit.compareTo(orderValue) >= 0) {
                    BigDecimal promoValue = applyDiscount(orderValue, pm.discount);
                    if (promoValue.compareTo(discountedValue) < 0) {
                        discountedValue = promoValue;
                        chosenMethod = pm.id;
                        appliedDiscount = pm.discount;
                    }
                }
            }
        }

        // 2. Całość punktami (PUNKTY)
        PaymentMethod points = paymentMap.get("PUNKTY");
        if (points != null && points.limit.compareTo(orderValue) >= 0) {
            BigDecimal promoValue = applyDiscount(orderValue, points.discount);
            if (promoValue.compareTo(discountedValue) < 0) {
                discountedValue = promoValue;
                chosenMethod = "PUNKTY";
                appliedDiscount = points.discount;
            }
        }

        // 3. Częściowo punktami
        BigDecimal tenPercent = orderValue.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
        if (points != null && points.limit.compareTo(tenPercent) >= 0) {
            BigDecimal promoValue = applyDiscount(orderValue, 10);
            if (promoValue.compareTo(discountedValue) < 0) {
                chosenMethod = "PUNKTY_PARTIAL";
                appliedDiscount = 10;
            }
        }

        // 4. Bez promocji
        if (chosenMethod == null) {
            for (PaymentMethod pm : paymentMap.values()) {
                if (pm.limit.compareTo(orderValue) >= 0) {
                    chosenMethod = pm.id;
                    appliedDiscount = 0;
                    break;
                }
            }
        }

        return new PaymentDecision(chosenMethod, appliedDiscount);
    }

     static void applyPayment(Order order, PaymentDecision decision, Map<String, PaymentMethod> paymentMap, Map<String, BigDecimal> usedAmounts) {
        BigDecimal orderValue = order.value;
        String method = decision.method;
        int discount = decision.discount;

        PaymentMethod points = paymentMap.get("PUNKTY");

        switch (method) {
            case "PUNKTY":
                BigDecimal paidWithPoints = applyDiscount(orderValue, discount);
                points.limit = points.limit.subtract(paidWithPoints);
                usedAmounts.merge("PUNKTY", paidWithPoints, BigDecimal::add);
                break;

            case "PUNKTY_PARTIAL":
                BigDecimal tenPercent = orderValue.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
                points.limit = points.limit.subtract(tenPercent);
                usedAmounts.merge("PUNKTY", tenPercent, BigDecimal::add);

                BigDecimal rest = orderValue.subtract(tenPercent);
                BigDecimal restWithDiscount = applyDiscount(rest, 10);
                for (PaymentMethod pm : paymentMap.values()) {
                    if (!pm.id.equals("PUNKTY") && pm.limit.compareTo(restWithDiscount) >= 0) {
                        pm.limit = pm.limit.subtract(restWithDiscount);
                        usedAmounts.merge(pm.id, restWithDiscount, BigDecimal::add);
                        break;
                    }
                }
                break;

            default:
                PaymentMethod pm = paymentMap.get(method);
                if (pm != null) {
                    BigDecimal paid = applyDiscount(orderValue, discount);
                    pm.limit = pm.limit.subtract(paid);
                    usedAmounts.merge(pm.id, paid, BigDecimal::add);
                }
        }
    }

     static BigDecimal applyDiscount(BigDecimal amount, int discountPercent) {
        return amount.multiply(BigDecimal.valueOf(100 - discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

     static void printUsedAmounts(Map<String, BigDecimal> usedAmounts) {
        System.out.println("Wydane środki z każdej metody:");
        usedAmounts.forEach((method, amount) -> {
            System.out.println(method + ": " + amount.setScale(2, RoundingMode.HALF_UP));
        });
    }

    static class PaymentDecision {
        String method;
        int discount;

        PaymentDecision(String method, int discount) {
            this.method = method;
            this.discount = discount;
        }
    }
}
