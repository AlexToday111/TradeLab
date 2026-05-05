package com.example.back.livetrading.startup;

import com.example.back.livetrading.config.LiveTradingProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveTradingStartupSafetyCheck implements ApplicationRunner {

    private final LiveTradingProperties liveTradingProperties;

    @Value("${security.jwt.secret:}")
    private String jwtSecret;

    @Value("${python.parser.internal-secret:}")
    private String pythonInternalSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (!liveTradingProperties.realOrderSubmissionEnabled()) {
            return;
        }

        Map<String, String> unsafeValues = new LinkedHashMap<>();
        collectUnsafe(
                unsafeValues,
                "LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY",
                liveTradingProperties.credentialEncryptionKey()
        );
        collectUnsafe(unsafeValues, "SECURITY_JWT_SECRET", jwtSecret);
        collectUnsafe(unsafeValues, "PYTHON_PARSER_INTERNAL_SECRET", pythonInternalSecret);

        if (!unsafeValues.isEmpty()) {
            throw new IllegalStateException(
                    "Unsafe live trading startup configuration: replace change-me values for "
                            + String.join(", ", unsafeValues.keySet())
            );
        }
    }

    private void collectUnsafe(Map<String, String> unsafeValues, String name, String value) {
        if (isUnsafeSecret(value)) {
            unsafeValues.put(name, value);
        }
    }

    private boolean isUnsafeSecret(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("change-me") || normalized.startsWith("change-me-");
    }
}
