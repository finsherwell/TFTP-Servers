package TFTP_UDP_Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TFTPClient {
    // Controls main loop
    private static boolean run = true;

    public static void main(String[] args) {
        // Scanner for input
        Scanner scanner = new Scanner(System.in);

        // Keeps client running whilst they still want to manage files
        while (run) {
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                // Socket timeout
                clientSocket.setSoTimeout(Constants.TIMEOUT);
                InetAddress serverAddr = InetAddress.getByName(Constants.HOST);

                // Ask user to enter filename
                System.out.print("Enter Filename: ");
                String filename = scanner.next();

                int choice = -1;

                // Gets user to make a valid choice option
                while (choice != 1 && choice != 2) {
                    System.out.print("Send File to server (1) or Retrieve File from server (2): ");
                    if (scanner.hasNextInt()) {
                        choice = scanner.nextInt();
                        if (choice != 1 && choice != 2) {
                            System.out.println("Invalid Option!");
                        }
                    } else {
                        System.out.println("Invalid Option!");
                        scanner.next();
                    }
                }

                // If they choose to send to server
                if (choice == 1) {
                    // Makes sure it exists
                    if (!TFTPUtils.fileExists(filename)) {
                        System.out.println("File does not exist.");
                        continue;
                    }
                    // Send request
                    TFTPUtils.writeRequest(filename, serverAddr, clientSocket, Constants.PORT);
                    // If they choose to download from server
                } else if (choice == 2) {
                    // Send request
                    TFTPUtils.readRequest(filename, serverAddr, clientSocket, Constants.PORT);
                }
            } catch (IOException e) {
                // Error
                System.out.println("Error: " + e.getMessage());
            }

            // Ask user if they want to continue
            String continueInput = "";
            while (!continueInput.equalsIgnoreCase("y") && !continueInput.equalsIgnoreCase("n")) {
                System.out.print("Continue? (y/n): ");
                continueInput = scanner.next();
                if (continueInput.equalsIgnoreCase("n")) {
                    run = false;
                } else if (!continueInput.equalsIgnoreCase("y")) {
                    System.out.println("Invalid Option!");
                }
            }
        }
        // Close scanner
        scanner.close();
    }
}
