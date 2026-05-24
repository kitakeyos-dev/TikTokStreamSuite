package com.leaderboard.model;

/**
 * A pushed incoming bank transaction kept in local history.
 */
public class BankDeposit {
    private String userId;
    private double amount;
    private String message;
    private String transactionTime;
    private String reference;
    private String recordedAt;
    private boolean synced;

    public BankDeposit() {}

    public BankDeposit(String userId, double amount, String message,
                       String transactionTime, String reference, String recordedAt) {
        this.userId = userId;
        this.amount = amount;
        this.message = message;
        this.transactionTime = transactionTime;
        this.reference = reference;
        this.recordedAt = recordedAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTransactionTime() { return transactionTime; }
    public void setTransactionTime(String transactionTime) { this.transactionTime = transactionTime; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public String uniqueKey() {
        return (message + "|" + transactionTime).trim();
    }
}
