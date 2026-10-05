package edu.cit.gabutan.supplier.legacy;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import edu.cit.gabutan.channel.TianggeRuntime;

@Component
class LegacySupplyClient {

    private static final String BASE_URL =
            "https://legacysupply.onrender.com/api/v1";

    private final RestTemplate restTemplate;
    private final TianggeRuntime tianggeRuntime;
    private String sessionToken;

    LegacySupplyClient(TianggeRuntime tianggeRuntime) {
        this.restTemplate = new RestTemplate();
        this.tianggeRuntime = tianggeRuntime;
    }
    LegacyProductInfo getProductInfo(String productId) {

    return switch (productId) {

        case "P100" ->
                new LegacyProductInfo(
                        "RZT-6368",
                        20
                );

        case "P200" ->
                new LegacyProductInfo(
                        "RZT-3795",
                        20
                );

        case "P300" ->
                new LegacyProductInfo(
                        "RZT-2377",
                        10
                );

        default ->
                throw new IllegalArgumentException(
                        "Unknown product: " + productId
                );
    };
}

    String signIn() {

        String clientId = System.getenv("LS_CLIENT_ID");
        String apiKey = System.getenv("LS_API_KEY");

        if (clientId == null || apiKey == null) {
            throw new IllegalStateException(
                    "LS_CLIENT_ID and LS_API_KEY environment variables are required."
            );
        }

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AuthRequest>
                    <ClientId>%s</ClientId>
                    <ApiKey>%s</ApiKey>
                </AuthRequest>
                """.formatted(clientId, apiKey);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                headers.set(
                        "X-Client-Instance",
                        tianggeRuntime.getInstanceId()
                );

        HttpEntity<String> request =
                new HttpEntity<>(xml, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        BASE_URL + "/auth/token",
                        HttpMethod.POST,
                        request,
                        String.class
                );

        sessionToken =
                LegacySupplyTranslator.extractSessionToken(
                        response.getBody()
                );

        return sessionToken;
    }

    String getCatalog() {

        ensureSession();

        try {
            return sendAuthenticatedGet("/catalog");
        } catch (RuntimeException ex) {

            if (isSessionError(ex)) {
                signIn();
                return sendAuthenticatedGet("/catalog");
            }

            throw ex;
        }
    }

    String placePurchaseOrder(
            String supplierSku,
            int quantity,
            String buyerRef,
            String requestId) {

        ensureSession();

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <PurchaseOrder>
                    <SupplierSku>%s</SupplierSku>
                    <Qty>%d</Qty>
                    <BuyerRef>%s</BuyerRef>
                </PurchaseOrder>
                """.formatted(
                supplierSku,
                quantity,
                buyerRef
        );

        try {
            return sendAuthenticatedPost(
                    "/purchase-orders",
                    xml,
                    requestId
            );

        } catch (RuntimeException ex) {

            if (isSessionError(ex)) {
                signIn();

                return sendAuthenticatedPost(
                        "/purchase-orders",
                        xml,
                        requestId
                );
            }

            throw ex;
        }
    }

    String getPurchaseOrder(String poNumber) {

        ensureSession();

        try {
            return sendAuthenticatedGet(
                    "/purchase-orders/" + poNumber
            );

        } catch (RuntimeException ex) {

            if (isSessionError(ex)) {
                signIn();

                return sendAuthenticatedGet(
                        "/purchase-orders/" + poNumber
                );
            }

            throw ex;
        }
    }

    private void ensureSession() {

        if (sessionToken == null || sessionToken.isBlank()) {
            signIn();
        }
    }

    private String sendAuthenticatedGet(String path) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LS-Session", sessionToken);
        headers.set(
                "X-Client-Instance",
                tianggeRuntime.getInstanceId()
        );

        HttpEntity<Void> request =
                new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        BASE_URL + path,
                        HttpMethod.GET,
                        request,
                        String.class
                );

        return response.getBody();
    }

    private String sendAuthenticatedPost(
            String path,
            String xml,
            String requestId) {

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(MediaType.APPLICATION_XML);
                headers.set("X-LS-Session", sessionToken);
                headers.set("X-Request-Id", requestId);
                headers.set(
                        "X-Client-Instance",
                        tianggeRuntime.getInstanceId()
                );

        HttpEntity<String> request =
                new HttpEntity<>(xml, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        BASE_URL + path,
                        HttpMethod.POST,
                        request,
                        String.class
                );

        return response.getBody();
    }

    private boolean isSessionError(RuntimeException ex) {

        String message = ex.getMessage();

        return message != null &&
                (
                    message.contains("E-AUTH-07") ||
                    message.contains("E-AUTH-03") ||
                    message.contains("E-AUTH-02")
                );
    }
    
}
