package optimized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class ServerPool {
    public final static int PORT = 18201;
    public static String ID;
    private volatile boolean terminateClient = false;

    private ServerSocket server;
    private volatile boolean terminated = false;

    private final Executor threadPool = newCachedThreadPool();

    public void start() {
        System.out.println("The server started");

        try {
            server = new ServerSocket(PORT);
            server.setSoTimeout(3000);

            while (!terminated) {
                try {
                    Socket clientSocket = server.accept();
                    System.out.println("Accappted Client");
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (SocketTimeoutException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println("The server terminate");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                System.out.println("Could not close the server \n" + e.getMessage());
            }
        }
    }

    
      
     

    private class ClientHandler implements Runnable {
        private final String ID;
        private final Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.ID = UUID.randomUUID().toString();
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                System.out.println("This is the new client " + ID);
                String message = receiveMessage(reader);
                while (!terminateClient) {
                    message = receiveMessage(reader);
                    System.out.println(message);
                    sendMessage(writer, String.valueOf(new Random().nextInt()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.out.println("Could not close reader \n" + e.getMessage());
                    }
                }
                if (writer != null) {
                    writer.close();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Could not close the socket of the client \n" + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        terminated = true;
        terminateClient = true;
    }

// Static Mthoden

    public static void sendMessage(PrintWriter writer, String message) {
        writer.print(message + "\r\n");
        writer.flush();
    }

    public static String receiveMessage(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

   
    private void terminate() {
        terminated = true;
    }
}


