package edu.cit.gabutan.channel;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
class TianggeStockCoordinator {

    private final TianggeChannel tianggeChannel;

    private final ThreadLocal<Boolean> deferring =
            ThreadLocal.withInitial(() -> false);

    private final ThreadLocal<Map<String, Integer>> deferredStock =
            ThreadLocal.withInitial(LinkedHashMap::new);

    TianggeStockCoordinator(TianggeChannel tianggeChannel) {
        this.tianggeChannel = tianggeChannel;
    }

    void startDeferring() {
        deferredStock.get().clear();
        deferring.set(true);
    }

    boolean isDeferring() {
        return deferring.get();
    }

    void defer(String productId, int available) {
        deferredStock.get().put(productId, available);
    }

    void publishDeferred() {

        Map<String, Integer> stock =
                new LinkedHashMap<>(deferredStock.get());

        deferredStock.get().clear();
        deferring.set(false);

        for (Map.Entry<String, Integer> entry
                : stock.entrySet()) {

            tianggeChannel.publishStock(
                    entry.getKey(),
                    entry.getValue());
        }
    }

    void discardDeferred() {
        deferredStock.get().clear();
        deferring.set(false);
    }
}