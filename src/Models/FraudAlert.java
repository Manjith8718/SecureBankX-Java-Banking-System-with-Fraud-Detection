package Models;

import java.sql.Timestamp;

public class FraudAlert {

    private int fraudId;
    private int accountId;
    private String fraudType;
    private Timestamp detectedTime;
    private String status;

    public FraudAlert() {}

    public FraudAlert(int accountId, String fraudType) {
        this.accountId = accountId;
        this.fraudType = fraudType;
        this.status = "PENDING";
    }

    public FraudAlert(int fraudId, int accountId, String fraudType,
                      Timestamp detectedTime, String status) {
        this.fraudId = fraudId;
        this.accountId = accountId;
        this.fraudType = fraudType;
        this.detectedTime = detectedTime;
        this.status = status;
    }

    public int getFraudId() {
        return fraudId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getFraudType() {
        return fraudType;
    }

    public Timestamp getDetectedTime() {
        return detectedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setFraudId(int fraudId) {
        this.fraudId = fraudId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setFraudType(String fraudType) {
        this.fraudType = fraudType;
    }

    public void setDetectedTime(Timestamp detectedTime) {
        this.detectedTime = detectedTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
