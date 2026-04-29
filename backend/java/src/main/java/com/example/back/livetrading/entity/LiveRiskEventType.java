package com.example.back.livetrading.entity;

public enum LiveRiskEventType {
    ORDER_REJECTED,
    ORDER_ACCEPTED,
    CIRCUIT_BREAKER_TRIGGERED,
    CIRCUIT_BREAKER_RESET,
    KILL_SWITCH_ACTIVATED,
    KILL_SWITCH_RESET,
    POSITION_SYNC_FAILED
}
