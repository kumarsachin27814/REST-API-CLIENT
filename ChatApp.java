import java.io.*;
import java.net.*;
import java.util.*;

// === ChatApp: Combined Server & Client ===
public class ChatApp {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java ChatApp [server|client]");
            return;
        }

        if (args[0].equalsIgnoreCase("server")) {
            new ChatServer().startServer();
        } else if (args[0].equalsIgnoreCase("client")) {
            new ChatClient().startClient();
        } else {
            System.out.println("Invalid argument. Use 'server' or 'client'.");
        }
    }
}


class ChatServer {
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public void startServer() {
        System.out.println("Chat Server started on port " + ChatApp.PORT);

        try (ServerSocket serverSocket = new ServerSocket(ChatApp.PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                handler.start();
                System.out.println("New client connected.");
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Enter your name:");
                name = in.readLine();
                broadcast(name + " joined the chat!", this);

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("exit")) break;
                    System.out.println(name + ": " + msg);
                    broadcast(name + ": " + msg, this);
                }
            } catch (IOException e) {
                System.out.println("Connection error with " + name);
            } finally {
                try {
                    clients.remove(this);
                    socket.close();
                    broadcast(name + " left the chat.", this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void sendMessage(String message) {
            out.println(message);
        }
    }
}

class ChatClient {
    public void startClient() {
        try (
            Socket socket = new Socket("localhost", ChatApp.PORT);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
        
            Thread readerThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Server disconnected.");
                }
            });
            readerThread.start();

            String input;
            while ((input = userInput.readLine()) != null) {
                out.println(input);
                if (input.equalsIgnoreCase("exit")) break;
            }

        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
        }
    }
}

