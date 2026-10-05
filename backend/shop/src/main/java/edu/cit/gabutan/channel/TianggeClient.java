package edu.cit.gabutan.channel;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Instant;

class TianggeClient {

    private static final String BASE_URL =
            "https://legacysupply.onrender.com/tiangge/v1";

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String apiKey;
    private final String instanceId;
    private final Instant startedAt;

    TianggeClient(String instanceId, Instant startedAt) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
    
        factory.setConnectTimeout(java.time.Duration.ofSeconds(5));
        factory.setReadTimeout(java.time.Duration.ofSeconds(10));
    
        this.restTemplate = new RestTemplate(factory);
    
        this.clientId = System.getenv("LS_CLIENT_ID");
        this.apiKey = System.getenv("LS_API_KEY");
        this.instanceId = instanceId;
        this.startedAt = startedAt;

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "LS_CLIENT_ID environment variable is required."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "LS_API_KEY environment variable is required."
            );
        }
    }

    void heartbeat() {

        long uptimeSeconds =
        java.time.Duration.between(
                startedAt,
                Instant.now()
        ).getSeconds();

        String body = """
        {
          "appName": "gabutan-shop",
          "startedAt": "%s",
          "uptimeSeconds": %d
        }
        """.formatted(
        startedAt,
        uptimeSeconds
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Id", clientId);
        headers.setBearerAuth(apiKey);
        headers.set("X-Client-Instance", instanceId);

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/instances/heartbeat",
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Tiangge heartbeat failed: " +
                    response.getStatusCode()
            );
        }
    }
    void publishListings() {

        String body = """
                [
                  {
                    "sellerSku": "P100",
                    "title": "Wireless Mouse",
                    "supplierSku": "RZT-6368"
                  },
                  {
                    "sellerSku": "P200",
                    "title": "Mechanical Keyboard",
                    "supplierSku": "RZT-3795"
                  },
                  {
                    "sellerSku": "P300",
                    "title": "USB-C Hub",
                    "supplierSku": "RZT-2377"
                  }
                ]
                """;
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Id", clientId);
        headers.setBearerAuth(apiKey);
        headers.set("X-Client-Instance", instanceId);
    
        HttpEntity<String> request =
                new HttpEntity<>(body, headers);
    
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/listings",
                HttpMethod.PUT,
                request,
                String.class
        );
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Tiangge listings publication failed: " +
                    response.getStatusCode()
            );
        }
    }
    void publishStock(String sellerSku, int available) {

        String body = """
                [
                  {
                    "sellerSku": "%s",
                    "available": %d
                  }
                ]
                """.formatted(sellerSku, available);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Id", clientId);
        headers.setBearerAuth(apiKey);
        headers.set("X-Client-Instance", instanceId);
    
        HttpEntity<String> request =
                new HttpEntity<>(body, headers);
    
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/stock",
                HttpMethod.PUT,
                request,
                String.class
        );
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Tiangge stock publication failed: " +
                    response.getStatusCode()
            );
        }
    }
    void publishCurrentStock(java.util.List<edu.cit.gabutan.inventory.Inventory> inventory) {

        StringBuilder body = new StringBuilder("[\n");
    
        for (int i = 0; i < inventory.size(); i++) {
            var item = inventory.get(i);
    
            body.append("""
                    {
                      "sellerSku": "%s",
                      "available": %d
                    }
                    """.formatted(
                    item.getProductId(),
                    item.getStock()
            ));
    
            if (i < inventory.size() - 1) {
                body.append(",");
            }
    
            body.append("\n");
        }
    
        body.append("]");
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Id", clientId);
        headers.setBearerAuth(apiKey);
        headers.set("X-Client-Instance", instanceId);
    
        HttpEntity<String> request =
                new HttpEntity<>(body.toString(), headers);
    
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/stock",
                HttpMethod.PUT,
                request,
                String.class
        );
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Tiangge current stock publication failed: " +
                    response.getStatusCode()
            );
        }
    }
    TianggeFeedResponse getFeed(Long after, int limit) {

        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException(
                    "Tiangge feed limit must be between 1 and 50.");
        }
    
        String url = BASE_URL + "/feed?limit=" + limit;
    
        if (after != null) {
            url += "&after=" + after;
        }
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Client-Id", clientId);
        headers.setBearerAuth(apiKey);
        headers.set("X-Client-Instance", instanceId);
    
        HttpEntity<Void> request =
                new HttpEntity<>(headers);
    
        ResponseEntity<TianggeFeedResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        TianggeFeedResponse.class
                );
    
        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null) {
    
            throw new IllegalStateException(
                    "Tiangge feed request failed: "
                            + response.getStatusCode());
        }
    
        return response.getBody();
    }
    void decideOrder(
        String tianggeOrderId,
        String decision,
        Long shopOrderId,
        String reason) {

    String body = """
            {
              "decision": "%s",
              "shopOrderId": "%s",
              "reason": "%s"
            }
            """.formatted(
            decision,
            shopOrderId,
            reason == null ? "" : reason.replace("\"", "\\\"")
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Client-Id", clientId);
    headers.setBearerAuth(apiKey);
    headers.set("X-Client-Instance", instanceId);

    HttpEntity<String> request =
            new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
            restTemplate.exchange(
                    BASE_URL + "/orders/" + tianggeOrderId + "/decision",
                    HttpMethod.POST,
                    request,
                    String.class
            );

    if (!response.getStatusCode().is2xxSuccessful()) {
        throw new IllegalStateException(
                "Tiangge order decision failed: "
                        + response.getStatusCode()
        );
    }
}
void confirmCancellation(String tianggeOrderId) {

    String body = """
            {
              "restocked": true
            }
            """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Client-Id", clientId);
    headers.setBearerAuth(apiKey);
    headers.set("X-Client-Instance", instanceId);

    HttpEntity<String> request =
            new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
            restTemplate.exchange(
                    BASE_URL
                            + "/orders/"
                            + tianggeOrderId
                            + "/cancellation",
                    HttpMethod.POST,
                    request,
                    String.class
            );

    if (!response.getStatusCode().is2xxSuccessful()) {
        throw new IllegalStateException(
                "Tiangge cancellation confirmation failed: "
                        + response.getStatusCode()
        );
    }
}
}