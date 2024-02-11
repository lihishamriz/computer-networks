import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {
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
        return properties.getProperty("root", "~/www/lab/html/");
    }

    public String getDefaultPage() {
        return properties.getProperty("defaultPage", "index.html");
    }

    public int getMaxThreads() {
        return Integer.parseInt(properties.getProperty("maxThreads", "10"));
    }
}
