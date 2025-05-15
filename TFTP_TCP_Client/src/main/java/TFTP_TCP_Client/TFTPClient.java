package TFTP_TCP_Client;

public class TFTPClient {
    /**
     * Main method that runs the client application, and spawns a main menu.
     *
     * @param args Command-line arguments - not used.
     */
    public static void main(String[] args) {
        ClientMenu menu = new ClientMenu();
        menu.mainMenu();
    }
}