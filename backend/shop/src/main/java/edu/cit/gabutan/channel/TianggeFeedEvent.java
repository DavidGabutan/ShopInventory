package edu.cit.gabutan.channel;

import java.util.List;

record TianggeFeedEvent(
        Long seq,
        String eventId,
        String type,
        String orderId,
        String placedAt,
        String decisionDeadline,
        String cancelledAt,
        String confirmDeadline,
        List<TianggeFeedLine> lines
) {
}