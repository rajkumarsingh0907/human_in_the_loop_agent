package com.app.human.approval.controller;

import com.app.human.approval.model.AgentReply;
import com.app.human.approval.model.CustomerMessage;
import com.app.human.approval.model.HumanDecision;
import com.app.human.approval.model.RefundRequest;
import com.app.human.approval.service.ApprovalService;
import com.app.human.approval.service.RefundAgent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/refunds")
public class RefundController {

    private final RefundAgent agent;
    private final ApprovalService approvals;

    public RefundController(
            RefundAgent agent,
            ApprovalService approvals
    ) {
        this.agent = agent;
        this.approvals = approvals;
    }

    @PostMapping("/request")
    public AgentReply request(
            @RequestBody CustomerMessage message
    ) {
        return agent.handle(message);
    }

    @GetMapping("/{id}")
    public RefundRequest get(
            @PathVariable UUID id
    ) {
        return approvals.get(id);
    }

    @PostMapping("/{id}/decision")
    public RefundRequest decide(
            @PathVariable UUID id,
            @RequestBody HumanDecision decision
    ) {
        if (decision.approved() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "approved must be true or false"
            );
        }

        return approvals.decide(id, decision.approved());
    }
}
