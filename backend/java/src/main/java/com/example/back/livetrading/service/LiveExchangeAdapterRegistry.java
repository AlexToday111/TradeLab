package com.example.back.livetrading.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class LiveExchangeAdapterRegistry {

    private final Map<String, LiveExchangeAdapter> adapters;

    public LiveExchangeAdapterRegistry(List<LiveExchangeAdapter> adapters) {
        this.adapters = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(adapter -> adapter.exchange().toLowerCase(), Function.identity()));
    }

    public LiveExchangeAdapter requireAdapter(String exchange) {
        LiveExchangeAdapter adapter = adapters.get(exchange.toLowerCase());
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported live exchange: " + exchange);
        }
        return adapter;
    }
}
