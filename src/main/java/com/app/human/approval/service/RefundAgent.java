package com.app.human.approval.service;

import com.app.human.approval.model.AgentReply;
import com.app.human.approval.model.CustomerMessage;
import com.app.human.approval.model.Order;
import com.app.human.approval.model.RefundRequest;
import com.app.human.approval.tool.ApprovalTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class RefundAgent {

    private static final Logger log =
            LoggerFactory.getLogger(RefundAgent.class);

    private final ChatClient chatClient;
    private final OrderService orders;
    private final ApprovalService approvals;

    public RefundAgent(
            @Qualifier("refundChatClient") ChatClient chatClient,
            OrderService orders,
            ApprovalService approvals
    ) {
        this.chatClient = chatClient;
        this.orders = orders;
        this.approvals = approvals;
    }

    public AgentReply handle(CustomerMessage input) {
        if (input.orderId() == null ||
                input.message() == null ||
                input.message().isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Order ID and message are required"
            );
        }

        Order order = orders.get(input.orderId());

        log.info(
                "Agent request started: orderId={}",
                order.id()
        );

        ApprovalTools tools = new ApprovalTools(
                approvals,
                order
        );

        String answer;

        try {
            answer = chatClient.prompt()
                    .system(system -> system.param(
                            "order",
                            order.toString()
                    ))
                    .user(input.message())
                    .tools(tools)
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            log.error(
                    "Agent request failed: orderId={}, errorType={}",
                    order.id(),
                    exception.getClass().getSimpleName()
            );

            throw exception;
        }

        RefundRequest proposal = tools.getProposal();

        if (proposal == null) {
            log.info(
                    "Agent request completed without a refund proposal: orderId={}",
                    order.id()
            );

            return new AgentReply(answer, null);
        }

        log.info(
                "Agent request completed: orderId={}, requestId={}, status={}",
                order.id(),
                proposal.id(),
                proposal.status()
        );

        return new AgentReply(
                "Refund request status: " + proposal.status(),
                proposal
        );
    }
}
