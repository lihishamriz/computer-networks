import java.io.BufferedReader;
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
            System.out.println("Server is listening on port " + port);
            ExecutorService executor = Executors.newFixedThreadPool(config.getMaxThreads());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Create a Runnable and submit it to the executor
                Runnable worker = new EchoRunnable(clientSocket);
                executor.execute(worker);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            String request = reader.readLine();
            System.out.println("Received request: " + request);

            HTTPRequest httpRequest = new HTTPRequest(request);
            HTTPResponse httpResponse = new HTTPResponse(httpRequest);
            httpResponse.generateResponse(output);

            // Send a simple HTTP response back to the client
//            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHello, this is a simple web server!";
//            writer.println(response);

            // Close the connection
            clientSocket.close();
            System.out.println("Connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
