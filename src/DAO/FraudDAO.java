package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class FraudDAO {

    // ---------------- RAPID TRANSACTION CHECK ----------------
    // Rule: 5 transactions within last 2 minutes
    public static boolean isRapidTransaction(int accountId) {

        String sql = """
            SELECT COUNT(*)
            FROM transactions
            WHERE account_id = ?
              AND transaction_time >= NOW() - INTERVAL 2 MINUTE
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) >= 5;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- INSERT FRAUD (DB CONSTRAINT SAFE) ----------------
    public static void insertFraud(int accountId, String fraudType) {

        String sql = """
            INSERT INTO frauds_alerts(account_id, fraud_type, status)
            VALUES (?, ?, 'PENDING')
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setString(2, fraudType);
            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            // Fraud already exists (constraint hit) → ignore
            System.out.println("Fraud already recorded for this account & type.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- VIEW ALL PENDING FRAUDS ----------------
    public static void viewAllFrauds() {

        String sql = """
            SELECT fraud_id, account_id, fraud_type, detected_time, status
            FROM frauds_alerts
            WHERE status = 'PENDING'
            ORDER BY detected_time DESC
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- FRAUD ALERTS ---");
            System.out.printf("%-10s %-12s %-15s %-22s %-10s%n",
                    "FraudID", "AccountID", "Type", "DetectedTime", "Status");

            while (rs.next()) {
                System.out.printf("%-10d %-12d %-15s %-22s %-10s%n",
                        rs.getInt("fraud_id"),
                        rs.getInt("account_id"),
                        rs.getString("fraud_type"),
                        rs.getTimestamp("detected_time"),
                        rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- RESOLVE FRAUD ----------------
    public static boolean resolveFraud(int fraudId) {

        String sql = "UPDATE frauds_alerts SET status = 'RESOLVED' WHERE fraud_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fraudId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkExists(int fraudId)
    {
        String sql = "SELECT fraud_id from frauds_alerts where fraud_id = ?";
        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, fraudId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
