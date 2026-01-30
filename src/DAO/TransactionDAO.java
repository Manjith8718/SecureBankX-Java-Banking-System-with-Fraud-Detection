package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionDAO {

    // ---------------- INSERT TRANSACTION ----------------
    public static void insertTransaction(int accountId, double amount, String message) {

        String sql = """
            INSERT INTO transactions(account_id, amount, transaction_message)
            VALUES (?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setDouble(2, amount);
            ps.setString(3, message);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- AVG OF LAST 5 WITHDRAWALS ----------------
    public static double getAvgLast5Withdrawals(int accountId) {

        String sql = """
            SELECT AVG(amount)
            FROM (
                SELECT amount
                FROM transactions
                WHERE account_id = ?
                  AND transaction_message = 'DEBIT'
                ORDER BY transaction_time DESC
                LIMIT 5
            ) t
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1); // returns 0 if no rows
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
