import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void testGetPort_DefaultPort() {
        Config config = new Config("test-config.ini");
        assertEquals(8080, config.getPort(), "Default port should be 8080");
    }

    @Test
    public void testGetRoot_DefaultRoot() {
        Config config = new Config("test-config.ini");
        assertEquals(config.getRoot().contains("/www/lab/html/"), true, "Default root should be ~/www/lab/html/");
    }

    @Test
    public void testGetDefaultPage_DefaultPage() {
        Config config = new Config("test-config.ini");
        assertEquals("index.html", config.getDefaultPage(), "Default page should be index.html");
    }

    @Test
    public void testGetMaxThreads_DefaultMaxThreads() {
        Config config = new Config("test-config.ini");
        assertEquals(10, config.getMaxThreads(), "Default max threads should be 10");
    }
}
