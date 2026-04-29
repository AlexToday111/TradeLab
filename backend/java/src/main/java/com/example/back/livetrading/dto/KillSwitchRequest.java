package com.example.back.livetrading.dto;

public record KillSwitchRequest(
        String reason,
        boolean cancelOpenOrders
) {
}
