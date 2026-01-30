package Models;

import java.time.LocalDateTime;

public class Transaction {

    private int transactionId;
    private int accountId;
    private double amount;
    private LocalDateTime transactionTime;
    private String transactionMessage;

    public Transaction() {}

    public Transaction(int accountId, double amount, String transactionMessage) {
        this.accountId = accountId;
        this.amount = amount;
        this.transactionMessage = transactionMessage;
    }

    public Transaction(int transactionId, int accountId,
                       double amount, LocalDateTime transactionTime,
                       String transactionMessage) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionMessage = transactionMessage;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }
}

