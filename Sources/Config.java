import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static final String username = "admin";
    public static final String password = "1234";

    private Properties properties;

    public Config(String configFile) {
        properties = new Properties();
        try {
            properties.load(new FileReader(configFile));
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port", "8080"));
    }

    public String getRoot() {
        String userHome = System.getProperty("user.home");
        String root = properties.getProperty("root", "~/www/lab/html/");

        return root.replaceFirst("^~", userHome);
    }

    public String getDefaultPage() {
        return properties.getProperty("defaultPage", "index.html");
    }

    public int getMaxThreads() {
        return Integer.parseInt(properties.getProperty("maxThreads", "10"));
    }

    public String getServerUsername() {
        return username;
    }

    public String getServerPassword() {
        return password;
    }
}
