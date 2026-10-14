package com.app.human.approval.model;

public record AgentReply(
        String message,
        RefundRequest refundRequest
) {}
