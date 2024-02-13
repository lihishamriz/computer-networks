import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    public static void main(String[] args) {
        Config config = new Config("config.ini");
        int port = config.getPort();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port: " + port + "\n");
            ExecutorService executor = Executors.newFixedThreadPool(config.getMaxThreads());

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Create a Runnable and submit it to the executor
                Runnable worker = new EchoRunnable(clientSocket);
                executor.execute(worker);
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }
    }

    // EchoRunnable class to encapsulate the handling logic
    private static class EchoRunnable implements Runnable {
        private final Socket clientSocket;

        public EchoRunnable(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            handleClient(clientSocket);
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream output = clientSocket.getOutputStream();
        ) {
            // Read the HTTP request from the client
            StringBuilder requestBuilder = new StringBuilder();
            String line;

            // Read the HTTP request from the client line by line until an empty line is encountered
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }

            if (!requestBuilder.isEmpty()) {
                HTTPRequest httpRequest = new HTTPRequest(requestBuilder.toString(), reader);
                HTTPResponse httpResponse = new HTTPResponse(httpRequest, output);
                httpResponse.generateResponse();
            }

            // Close the connection
            clientSocket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
