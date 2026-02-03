package Models;

public class Account {

    private int accountId;
    private int userId;
    private long accountNumber;
    private double balance;
    private String status;
    private String pinHash;

    public String getPinHash() {
        return pinHash;
    }


    public Account() {}

    public Account(int userId, long accountNumber, String pinHash) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.pinHash = pinHash;
        this.status = "ACTIVE";
        this.balance = 0.0;
    }

    public Account(int accountId, int userId, long accountNumber,
                   double balance, String status, String pinHash) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.pinHash = pinHash;
    }


    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }


    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

}
