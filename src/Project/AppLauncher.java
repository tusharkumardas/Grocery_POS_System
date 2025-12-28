/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import grocery.LoginForm;
import grocery.Database_Setup;
import java.io.File;
/**
 *
 * @author Tushar Kumar Das
 */
public class AppLauncher {
     public static void main(String[] args) {

        // Check if DB config exists
        File config = new File("db.properties");

        if (!config.exists()) {
            new Database_Setup().setVisible(true);
            return;
        }

        // Try DB connection
        if (ConnectionProvider.getCon() == null) {
            new Database_Setup().setVisible(true);
            return;
        }

        // Init DB tables
        DBInit.initialize();

        // Normal flow
        new LoginForm().setVisible(true);
    }
    
}
