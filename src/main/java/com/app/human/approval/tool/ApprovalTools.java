package com.app.human.approval.tool;

import com.app.human.approval.model.Order;
import com.app.human.approval.model.RefundRequest;
import com.app.human.approval.service.ApprovalService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ApprovalTools {

    private final ApprovalService approvals;
    private final Order order;

    private RefundRequest proposal;

    public ApprovalTools(
            ApprovalService approvals,
            Order order
    ) {
        this.approvals = approvals;
        this.order = order;
    }

    @Tool(
            description = """
                    Request human approval for a full refund of the assigned order.
                    This creates a pending request. It does not execute a refund.
                    """,
            returnDirect = true
    )
    public RefundRequest requestRefundApproval(
            @ToolParam(description = "Brief business reason for the refund")
            String reason
    ) {
        proposal = approvals.create(order, reason);
        return proposal;
    }

    public RefundRequest getProposal() {
        return proposal;
    }
}

