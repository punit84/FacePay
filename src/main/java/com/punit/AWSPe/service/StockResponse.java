package com.punit.AWSPe.service;

public class StockResponse {
    private String company;
    private String symbol;
    private String message;

    public StockResponse(String company, String symbol, String message) {
        this.company = company;
        this.symbol = symbol;
        this.message = message;
    }

    // Getters and setters
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
