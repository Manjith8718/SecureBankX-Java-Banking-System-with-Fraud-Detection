package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PinDAO {

    public static void insertPin(int accountId) {
        String sql = "INSERT INTO pin_lock(account_id) VALUES (?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            // Likely already exists
            System.out.println("Pin lock already exists for account " + accountId);
        }
    }

    public static void incrementAttempts(int accountId) {
        String sql = "UPDATE pin_lock SET attempts = attempts + 1 WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getAttempts(int accountId) {
        String sql = "SELECT attempts FROM pin_lock WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("attempts");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void resetAttempts(int accountId) {
        String sql = "UPDATE pin_lock SET attempts = 0 WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void blockFor24Hours(int accountId) {
        String sql = """
            UPDATE pin_lock
            SET blocked_until = DATE_ADD(NOW(), INTERVAL 24 HOUR)
            WHERE account_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void unfreezeIfExpired(int accountId) {
        String sql = """
            UPDATE accounts a
            JOIN pin_lock p ON a.account_id = p.account_id
            SET a.status = 'ACTIVE',
                p.attempts = 0,
                p.blocked_until = NULL
            WHERE a.account_id = ?
              AND p.blocked_until IS NOT NULL
              AND p.blocked_until <= NOW()
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
