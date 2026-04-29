package com.example.back.livetrading.service;

import com.example.back.livetrading.config.LiveTradingProperties;
import com.example.back.livetrading.entity.LiveOrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BinanceLiveExchangeAdapter implements LiveExchangeAdapter {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper;
    private final LiveTradingProperties properties;

    public BinanceLiveExchangeAdapter(ObjectMapper objectMapper, LiveTradingProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String exchange() {
        return "binance";
    }

    @Override
    public Optional<BigDecimal> getLatestPrice(String symbol) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.binance().baseUrl() + "/api/v3/ticker/price?symbol=" + encode(symbol)))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Live price request failed exchange=binance symbol={} status={}", symbol, response.statusCode());
                return Optional.empty();
            }
            JsonNode node = objectMapper.readTree(response.body());
            return Optional.of(new BigDecimal(node.path("price").asText()));
        } catch (Exception exception) {
            log.warn("Live price request failed exchange=binance symbol={} error={}", symbol, exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public LiveOrderResult placeOrder(LiveOrderRequest orderRequest, ExchangeCredentials credentials) {
        if (!properties.realOrderSubmissionEnabled()) {
            return new LiveOrderResult(
                    "binance-live-disabled-" + Instant.now().toEpochMilli(),
                    LiveOrderStatus.ACCEPTED,
                    null,
                    null
            );
        }
        String query = "symbol=" + encode(orderRequest.symbol())
                + "&side=" + orderRequest.side()
                + "&type=" + orderRequest.type()
                + "&quantity=" + encode(orderRequest.quantity().stripTrailingZeros().toPlainString())
                + (orderRequest.requestedPrice() == null ? "" : "&price="
                        + encode(orderRequest.requestedPrice().stripTrailingZeros().toPlainString())
                        + "&timeInForce=GTC")
                + "&timestamp=" + System.currentTimeMillis();
        JsonNode node = signedRequest("/api/v3/order", "POST", query, credentials);
        String orderId = node.path("orderId").asText();
        String status = node.path("status").asText("NEW");
        return new LiveOrderResult(orderId, mapStatus(status), null, null);
    }

    @Override
    public void cancelOrder(String orderId, String symbol, ExchangeCredentials credentials) {
        if (!properties.realOrderSubmissionEnabled()) {
            return;
        }
        String query = "symbol=" + encode(symbol) + "&orderId=" + encode(orderId) + "&timestamp=" + System.currentTimeMillis();
        signedRequest("/api/v3/order", "DELETE", query, credentials);
    }

    @Override
    public LiveOrderResult getOrder(String orderId, String symbol, ExchangeCredentials credentials) {
        if (!properties.realOrderSubmissionEnabled()) {
            return new LiveOrderResult(orderId, LiveOrderStatus.ACCEPTED, null, null);
        }
        String query = "symbol=" + encode(symbol) + "&orderId=" + encode(orderId) + "&timestamp=" + System.currentTimeMillis();
        JsonNode node = signedRequest("/api/v3/order", "GET", query, credentials);
        return new LiveOrderResult(orderId, mapStatus(node.path("status").asText()), null, null);
    }

    @Override
    public List<LiveOrderResult> getOpenOrders(String symbol, ExchangeCredentials credentials) {
        return List.of();
    }

    @Override
    public List<ExchangePositionSnapshot> getPositions(ExchangeCredentials credentials) {
        return List.of();
    }

    @Override
    public List<ExchangeBalanceSnapshot> getBalances(ExchangeCredentials credentials) {
        return List.of();
    }

    @Override
    public boolean pingConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.binance().baseUrl() + "/api/v3/ping"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() < 400;
        } catch (Exception exception) {
            log.warn("Live exchange ping failed exchange=binance error={}", exception.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateCredentials(ExchangeCredentials credentials) {
        return credentials.apiKey() != null
                && credentials.apiSecret() != null
                && credentials.apiKey().length() >= 8
                && credentials.apiSecret().length() >= 8;
    }

    private JsonNode signedRequest(String path, String method, String query, ExchangeCredentials credentials) {
        try {
            String signedQuery = query + "&signature=" + sign(query, credentials.apiSecret());
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.binance().testnetBaseUrl() + path + "?" + signedQuery))
                    .timeout(Duration.ofSeconds(10))
                    .header("X-MBX-APIKEY", credentials.apiKey());
            HttpRequest request = switch (method) {
                case "POST" -> builder.POST(HttpRequest.BodyPublishers.noBody()).build();
                case "DELETE" -> builder.DELETE().build();
                default -> builder.GET().build();
            };
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Binance signed request failed with status " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (Exception exception) {
            throw new IllegalStateException("Binance signed request failed", exception);
        }
    }

    private String sign(String query, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(query.getBytes(StandardCharsets.UTF_8)));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private LiveOrderStatus mapStatus(String status) {
        return switch (status) {
            case "FILLED" -> LiveOrderStatus.FILLED;
            case "PARTIALLY_FILLED" -> LiveOrderStatus.PARTIALLY_FILLED;
            case "CANCELED", "EXPIRED" -> LiveOrderStatus.CANCELED;
            case "REJECTED" -> LiveOrderStatus.REJECTED;
            default -> LiveOrderStatus.ACCEPTED;
        };
    }
}
