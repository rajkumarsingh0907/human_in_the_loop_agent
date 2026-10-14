package com.app.human.approval.model;

import java.util.UUID;

public record RefundRequest(
        UUID id,
        String orderId,
        long amountMinor,
        String currency,
        String reason,
        Status status
) {}
