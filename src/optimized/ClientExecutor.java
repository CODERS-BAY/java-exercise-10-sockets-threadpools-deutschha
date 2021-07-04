package optimized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/*
Fügt eine Mummer ein Wenn Modulo = 2
 */
public class ClientExecutor {
    public final static int PORT = 18201;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean terminated = false;
    private final ScheduledExecutorService executorService = newSingleThreadScheduledExecutor();

    public void start() throws IOException {
        Socket socket = new Socket("localhost", PORT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream());

        sendMessage(writer, "Es läuft");
        int SLEEP_TIME = 1;
        executorService.scheduleAtFixedRate(()->sendMessage(writer, "Some message"), 0, SLEEP_TIME, TimeUnit.SECONDS);

        newCachedThreadPool().submit(this::receive);
     
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        sendMessage(writer, "END");
     
    }

    private void receive() {
        while (!terminated) {
            try {
                System.out.println(receiveMessage(reader));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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