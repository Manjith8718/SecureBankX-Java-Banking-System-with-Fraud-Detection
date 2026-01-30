package DAO;

import Models.Manager;
import Utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManagerDAO {

    // ---------------- CHECK IF MANAGER EXISTS ----------------
    public static boolean managerExists() {

        String sql = "SELECT 1 FROM manager LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- CREATE MANAGER ----------------
    public static boolean createManager(Manager m) {

        String sql = "INSERT INTO manager (email, password) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, m.getEmail());
            ps.setString(2, m.getPassword());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- VALIDATE MANAGER LOGIN ----------------
    public static boolean validateManager(String email, String password) {

        String sql = "SELECT password FROM manager WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                return BCrypt.checkpw(password, storedHash);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
