package Models;

import java.sql.Timestamp;

public class PinStatus {

    private int pinLockId;
    private int accountId;
    private int attempts;
    private Timestamp blockedUntil;

    public PinStatus() {}

    public PinStatus(int accountId, int attempts, Timestamp blockedUntil) {
        this.accountId = accountId;
        this.attempts = attempts;
        this.blockedUntil = blockedUntil;
    }

    public PinStatus(int pinLockId, int accountId, int attempts, Timestamp blockedUntil) {
        this.pinLockId = pinLockId;
        this.accountId = accountId;
        this.attempts = attempts;
        this.blockedUntil = blockedUntil;
    }

    public int getPinLockId() {
        return pinLockId;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getAttempts() {
        return attempts;
    }

    public Timestamp getBlockedUntil() {
        return blockedUntil;
    }

    public void setPinLockId(int pinLockId) {
        this.pinLockId = pinLockId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void setBlockedUntil(Timestamp blockedUntil) {
        this.blockedUntil = blockedUntil;
    }
}


