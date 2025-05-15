package TFTP_TCP_Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientMenu {
    private boolean isActive = true;
    private final Scanner userInput = new Scanner(System.in);
    private final TransferFile transferFile = new TransferFile();

    /**
     * The main menu lets client interact with server. It prompts the user to choose an operation
     * until the user exits.
     */
    public void mainMenu() {
        while (isActive) {
            try {
                TransferOperation operation = operation();
                if (operation != null) {
                    executeOperation(operation);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            promptContinue();
        }
        userInput.close();
    }

    /**
     * Asks the user to enter filename and choose a transfer type.
     * Validates user input and makes sure the file exists.
     *
     * @return A TransferOperation object or null.
     */
    private TransferOperation operation() {
        System.out.print("Enter Filename: ");
        String filename = userInput.next();

        TransferType type = transferType();
        if (type == null) {
            return null;
        }

        if (type == TransferType.UPLOAD) {
            Path filePath = Paths.get(Constants.OUTGOING + filename);
            if (!Files.exists(filePath)) {
                System.out.println("Error: File not found");
                return null;
            }
        }
        return new TransferOperation(filename, type);
    }

    /**
     * Asks the user to select a valid transfer type, either uploading or downloading.
     *
     * @return The chosen TransferType, or null if no selection made.
     */
    private TransferType transferType() {
        int choice = -1;

        while (choice != 1 && choice != 2) {
            System.out.print("Upload File to server (1) or Download File from server (2): ");

            if (userInput.hasNextInt()) {
                choice = userInput.nextInt();

                switch (choice) {
                    case 1:
                        return TransferType.UPLOAD;
                    case 2:
                        return TransferType.DOWNLOAD;
                    default:
                        System.out.println("Please select 1 or 2!");
                }
            } else {
                System.out.println("Please select 1 or 2!");
                userInput.next();
            }
        }

        return null;
    }

    /**
     * Executes the transfer operation, displays either success or error message.
     *
     * @param operation The operation to execute.
     */
    private void executeOperation(TransferOperation operation) {
        try {
            boolean success = transferFile.processTransfer(operation);

            if (success) {
                System.out.println("File " + (operation.getType() == TransferType.UPLOAD ? "upload" : "download") + " completed!");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Asks the user if they want to continue or exit.
     */
    private void promptContinue() {
        String response = "";

        while (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("n")) {
            System.out.print("Continue? (y/n): ");
            response = userInput.next();

            if (response.equalsIgnoreCase("n")) {
                isActive = false;
            } else if (!response.equalsIgnoreCase("y")) {
                System.out.println("Invalid Option!");
            }
        }
    }
}