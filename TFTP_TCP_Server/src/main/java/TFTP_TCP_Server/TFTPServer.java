package TFTP_TCP_Server;

import java.io.*;
import java.net.*;

public class TFTPServer {
    // Main method: Init the server to listen on the port and accepts clients.
    public static void main(String[] args) {
        System.out.println("Listening on port: " + Constants.PORT);

        try (ServerSocket srvSocket = new ServerSocket(Constants.PORT)) {
            while (true) {
                try {
                    Socket cliSocket = srvSocket.accept();
                    String addr = cliSocket.getInetAddress().getHostAddress();
                    System.out.println("Connection: " + addr);

                    new Thread(() -> new ConnectionHandler(cliSocket).process()).start();
                } catch (IOException e) {
                    System.err.println("Connection Error: " + e.getMessage());
                }
            }
        } catch (IOException err) {
            System.err.println("Server Error: " + err.getMessage());
            System.exit(1);
        }
    }
}