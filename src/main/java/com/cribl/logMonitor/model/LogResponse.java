package com.cribl.logMonitor.model;

import java.util.List;

public class LogResponse {
    private String status;
    private List<String> lines;

    public LogResponse(String status, List<String> lines) {
        this.status = status;
        this.lines = lines;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
