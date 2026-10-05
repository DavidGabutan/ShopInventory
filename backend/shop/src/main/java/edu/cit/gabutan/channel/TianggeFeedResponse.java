package edu.cit.gabutan.channel;

import java.util.List;

record TianggeFeedResponse(
        List<TianggeFeedEvent> events,
        Long nextCursor
) {
}