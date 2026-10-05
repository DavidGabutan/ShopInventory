package edu.cit.gabutan.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class TianggeChannelImpl implements TianggeChannel, TianggeRuntime {

    private static final Logger log =
            LoggerFactory.getLogger(TianggeChannelImpl.class);

    private final String instanceId;
    private final java.time.Instant startedAt;
    private final TianggeClient client;

    TianggeChannelImpl() {
        this.instanceId = UUID.randomUUID().toString();
        this.startedAt = java.time.Instant.now();
        this.client = new TianggeClient(instanceId, startedAt);
    
        log.info("Tiangge instance ID: {}", instanceId);
        log.info("Tiangge startup time: {}", startedAt);
    }

    @Override
    public void heartbeat() {
        client.heartbeat();

        log.info(
                "Tiangge heartbeat successful. Instance ID: {}",
                instanceId
        );
    }

    @Override
    public void publishListings() {
        client.publishListings();

        log.info(
                "Tiangge listings published successfully. Instance ID: {}",
                instanceId
        );
    }
    @Override
    public void publishStock(String sellerSku, int available) {

    client.publishStock(sellerSku, available);

    log.info(
            "Tiangge stock published: {} = {}",
            sellerSku,
            available
        );
    }
    @Override
    public void publishCurrentStock(
        java.util.List<edu.cit.gabutan.inventory.Inventory> inventory) {

    client.publishCurrentStock(inventory);

    log.info(
            "Tiangge current inventory stock published successfully.");
    }
    TianggeClient getClient() {
        return client;
    }
    @Override
    public String getInstanceId() {
        return instanceId;
    }
    
    @Override
    public java.time.Instant getStartedAt() {
        return startedAt;
    }
}