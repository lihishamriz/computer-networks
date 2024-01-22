import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 9922;
    private static final List<ClientHandler> clients = new ArrayList<>();

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String clientIP;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.writer = new PrintWriter(socket.getOutputStream(), true);
                this.clientIP = socket.getInetAddress().getHostAddress();
            } catch (IOException e) {
                System.err.println("ERROR " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                writer.println("Welcome to RUNI Computer Networks 2024 chat server! There are " + (clients.size() - 1) + " users connected.");

                broadcast(clientIP + " joined");

                String message;
                while ((message = reader.readLine()) != null) {
                    broadcast("(" + clientIP + ":" + clientSocket.getPort() + "): " + message);
                }
            } catch (IOException e) {
                System.err.println("ERROR " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    clients.remove(this);
                    broadcast(clientIP + " disconnected");
                } catch (IOException e) {
                    System.err.println("ERROR " + e.getMessage());
                }
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.writer.println(message);
                }
            }
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("ERROR " + e.getMessage());
        }
    }
}