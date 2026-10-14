package com.app.human.approval.model;

public record Order(
        String id,
        long amountMinor,
        String currency
) {
}
