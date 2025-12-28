/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import java.io.File;
/**
 *
 * @author Tushar Kumar Das
 */
public class AppPaths {
    public static File getConfigDir() {
        String base = System.getenv("APPDATA"); // C:\Users\...\AppData\Roaming
        File dir = new File(base, "VyaparSetu");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getDbConfigFile() {
        return new File(getConfigDir(), "db.properties");
    }
}
