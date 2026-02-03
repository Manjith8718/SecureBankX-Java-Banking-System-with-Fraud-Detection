package DAO;

import Models.Account;
import Utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AccountDAO {

    // ---------------- CREATE ACCOUNT ----------------
    public static boolean createAccount(Account a) {
        String sql = """
            INSERT INTO accounts(user_id, account_number, status, balance,pin_hash)
            VALUES (?, ?, 'ACTIVE', 0.0,?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, a.getUserId());
            ps.setLong(2, a.getAccountNumber());
            ps.setString(3,a.getPinHash());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Account already exists or DB error");
            return false;
        }
    }

    // ---------------- VALID ACCOUNT ----------------
    public static boolean validAccount(long accNo) {
        String sql = "SELECT 1 FROM accounts WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accNo);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- VALIDATE PIN ----------------
    public static boolean validatePin(long accNo, String enteredPin) {
        String sql = "SELECT pin_hash FROM accounts WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("pin_hash");
                return BCrypt.checkpw(enteredPin, storedHash);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- CREDIT ----------------
    public static boolean creditAmount(Connection con, long accNo, double amt) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amt);
            ps.setLong(2, accNo);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------- DEBIT ----------------
    public static boolean debitAmount(Connection con, long accNo, double amt) throws SQLException {
        String sql = """
            UPDATE accounts
            SET balance = balance - ?
            WHERE account_number = ? AND balance >= ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amt);
            ps.setLong(2, accNo);
            ps.setDouble(3, amt);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------- GET ACCOUNT ID ----------------
    public static int getAccountId(long accNo) {
        String sql = "SELECT account_id FROM accounts WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("account_id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ---------------- ACCOUNT STATUS ----------------
    public static boolean isFrozen(long accNo) {
        String sql = "SELECT status FROM accounts WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return "FROZEN".equalsIgnoreCase(rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- FREEZE ----------------
    public static void freezeAccount(int accountId) {
        String sql = "UPDATE accounts SET status = 'FROZEN' WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- UNFREEZE ----------------
    public static boolean unfreezeAccount(int accountId) {
        String sql = "UPDATE accounts SET status = 'ACTIVE' WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- CREATE / CHANGE PIN ----------------
    public static boolean createPin(long accNo, String hash) {
        String sql = "UPDATE accounts SET hash_pin = ? WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setLong(2, accNo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static long generateUniqueAccountNumber() {

        long accNo;
        String checkSql = "SELECT 1 FROM accounts WHERE account_number=?";

        do {
            accNo = 100000000000L + (long)(Math.random() * 900000000000L);

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(checkSql)) {

                ps.setLong(1, accNo);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    break; // unique
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (true);

        return accNo;
    }

    public static Double checkBalance(long accountNumber) {

        String sql = "SELECT balance FROM accounts WHERE account_number = ? AND status = 'ACTIVE'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                System.out.println("Account not found or inactive.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // account not found / error
    }


    public static boolean checkAccountId(int accountid)
    {
         String sql = "SELECT account_id FROM accounts WHERE account_number = ?";
         try(Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
         {
             ps.setInt(1, accountid);
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
