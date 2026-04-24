package com.example.back.executionjobs.entity;

public enum ExecutionJobStatus {
    QUEUED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    RETRYING
}
