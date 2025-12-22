/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author Tushar Kumar Das
 */
public class AuthService {

    public static boolean login(String username, String role, String pin) throws Exception {

        Connection con = ConnectionProvider.getCon();

        String sql = "SELECT id, pin_hash, role, status, failed_attempts " +
                     "FROM users WHERE username = ?";

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, username);

        ResultSet rs = pst.executeQuery();

        if (!rs.next()) {
            throw new Exception("Invalid username or PIN");
        }

        int userId = rs.getInt("id");
        String dbPinHash = rs.getString("pin_hash");
        String dbRole = rs.getString("role");
        String status = rs.getString("status");
        int failedAttempts = rs.getInt("failed_attempts");

        if (!status.equalsIgnoreCase("active")) {
            throw new Exception("Account is " + status);
        }

        if (!dbRole.equalsIgnoreCase(role)) {
            throw new Exception("Incorrect role selected");
        }

        // Verify PIN
        if (!BCrypt.checkpw(pin, dbPinHash)) {
            failedAttempts++;

            PreparedStatement upd = con.prepareStatement(
                "UPDATE users SET failed_attempts = ? WHERE username = ?"
            );
            upd.setInt(1, failedAttempts);
            upd.setString(2, username);
            upd.executeUpdate();

            if (failedAttempts >= 5) {
                PreparedStatement suspend = con.prepareStatement(
                    "UPDATE users SET status='suspended' WHERE username=?"
                );
                suspend.setString(1, username);
                suspend.executeUpdate();
            }

            throw new Exception("Invalid username or PIN");
        }

        // Reset attempts
        PreparedStatement reset = con.prepareStatement(
            "UPDATE users SET failed_attempts=0, last_login=NOW() WHERE username=?"
        );
        reset.setString(1, username);
        reset.executeUpdate();

        // ✅ SET SESSION HERE (THIS WAS MISSING)
        Session.userId = userId;
        Session.username = username;
        Session.role = dbRole;

        return true;
    }
}
