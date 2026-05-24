package com.leaderboard.model;

/**
 * Normalized bank transaction parsed from ACB API response.
 */
public class BankTransaction {

    public enum Type { IN, OUT, UNKNOWN }

    private final Type type;
    private final double amount;
    private final String message;
    private final String time;
    private final Double balance;
    private final String reference;

    private String userId;
    private boolean matchedPrefix;

    public BankTransaction(Type type, double amount, String message, String time, Double balance, String reference) {
        this.type = type;
        this.amount = amount;
        this.message = message;
        this.time = time;
        this.balance = balance;
        this.reference = reference;
    }

    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public Double getBalance() { return balance; }
    public String getReference() { return reference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isMatchedPrefix() { return matchedPrefix; }
    public void setMatchedPrefix(boolean matchedPrefix) { this.matchedPrefix = matchedPrefix; }
}
