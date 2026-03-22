package com.example.back.dto;

public class PythonHealthResponse {

    private String status;
    private String service;

    public PythonHealthResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}