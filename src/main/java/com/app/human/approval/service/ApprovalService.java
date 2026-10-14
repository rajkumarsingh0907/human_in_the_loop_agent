package com.app.human.approval.service;

import com.app.human.approval.model.Order;
import com.app.human.approval.model.RefundRequest;
import com.app.human.approval.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ApprovalService {

    private static final Logger log =
            LoggerFactory.getLogger(ApprovalService.class);

    private final Map<UUID, RefundRequest> requests = new HashMap<>();

    public synchronized RefundRequest create(
            Order order,
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "A refund reason is required"
            );
        }

        // One refund case per order in this demo.
        for (RefundRequest existing : requests.values()) {
            if (existing.orderId().equals(order.id())) {
                log.info(
                        "Existing refund request returned: " +
                                "orderId={}, requestId={}, status={}",
                        order.id(),
                        existing.id(),
                        existing.status()
                );

                return existing;
            }
        }

        RefundRequest request = new RefundRequest(
                UUID.randomUUID(),
                order.id(),
                order.amountMinor(),
                order.currency(),
                reason,
                Status.PENDING_APPROVAL
        );

        requests.put(request.id(), request);

        log.info(
                "Refund proposal created: " +
                        "requestId={}, orderId={}, status={}",
                request.id(),
                request.orderId(),
                request.status()
        );

        return request;
    }

    public synchronized RefundRequest get(UUID id) {
        RefundRequest request = requests.get(id);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Refund request not found"
            );
        }

        return request;
    }

    public synchronized RefundRequest decide(
            UUID id,
            boolean approved
    ) {
        RefundRequest request = get(id);
        log.info(
                "Review decision received: requestId={}, approved={}",
                id,
                approved
        );

        if (request.status() != Status.PENDING_APPROVAL) {
            log.info(
                    "Decision ignored because request is already handled: " +
                            "requestId={}, status={}",
                    id,
                    request.status()
            );
            return request;
        }

        Status nextStatus;

        if (approved) {
            log.info(
                    "Approval accepted: requestId={}, orderId={}",
                    id,
                    request.orderId()
            );

            executeRefund(request);
            nextStatus = Status.EXECUTED;
        } else {
            nextStatus = Status.REJECTED;
        }

        RefundRequest updated = new RefundRequest(
                request.id(),
                request.orderId(),
                request.amountMinor(),
                request.currency(),
                request.reason(),
                nextStatus
        );

        requests.put(id, updated);

        log.info(
                "Refund request updated: requestId={}, status={}",
                id,
                nextStatus
        );

        return updated;
    }

    private void executeRefund(RefundRequest request) {
        log.info(
                "SIMULATED refund executed: " +
                        "requestId={}, orderId={}, amountMinor={}, currency={}",
                request.id(),
                request.orderId(),
                request.amountMinor(),
                request.currency()
        );
    }
}
