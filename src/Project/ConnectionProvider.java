/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;


import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author Tushar Kumar Das
 */
public class ConnectionProvider {
         // ✅ App-specific config folder
    private static final String APP_DIR =
            System.getProperty("user.home") + File.separator +
            "AppData" + File.separator +
            "Local" + File.separator +
            "VyaparSetu";

    private static final String CONFIG_FILE =
            APP_DIR + File.separator + "db.properties";

    private static Properties props = new Properties();

    // ================= LOAD CONFIG =================
    private static void loadProps() throws Exception {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) {
            throw new Exception("DB config not found");
        }
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
        }
    }

    // ================= MAIN CONNECTION =================
    public static Connection getCon() {
        try {
            loadProps();
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://" +
                    props.getProperty("db.host") + ":" +
                    props.getProperty("db.port") + "/" +
                    props.getProperty("db.name") +
                    "?useSSL=false&serverTimezone=UTC";

            return DriverManager.getConnection(
                    url,
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

        } catch (Exception e) {
            e.printStackTrace(); // NEVER hide this
            return null;
        }
    }

    // ================= TEST CONNECTION =================
    public static Connection testConnection(
            String host, String port, String db,
            String user, String pass) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                     "?useSSL=false&serverTimezone=UTC";

        return DriverManager.getConnection(url, user, pass);
    }

    // ================= SAVE CONFIG =================
    public static void saveConfig(String pass) throws Exception {

        // ✅ Ensure directory exists
        File dir = new File(APP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db.name", "grocery");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", pass);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "VyaparSetu Database Configuration");
        }
    }
}
