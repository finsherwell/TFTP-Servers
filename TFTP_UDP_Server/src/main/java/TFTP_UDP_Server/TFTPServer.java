package TFTP_UDP_Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TFTPServer {
    private DatagramSocket socket;

    /**
     * Constructor initialises the DatagramSocket to listen on the port defined in Constants.
     * The server socket is created, allowing the server to receive UDP packet.
     *
     * @throws IOException if socket cannot be created cannot be bound to the port.
     */
    public TFTPServer() throws IOException {
        this.socket = new DatagramSocket(Constants.PORT);
    }

    /**
     * Starts server, always running to listen for incoming requests.
     * When packet received, it checks the opcode and processes the request.
     * It handles read and write requests from the client.
     *
     * @throws IOException if an error occurs while receiving packets or processing requests.
     */
    public void start() throws IOException {
        System.out.println("TFTP UDP Server Started!");
        while (true) {
            byte[] buf = new byte[Constants.MAX_LEN];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            socket.receive(recv);
            InetAddress addr = recv.getAddress();
            int port = recv.getPort();
            byte[] data = recv.getData();

            System.out.println("Connection Established: " + addr);
            System.out.println("Port: " + port);

            byte op = data[1];

            if (op == 1) {
                new ReadRequest().readRequest(data, addr, port, socket);
            } else if (op == 2) {
                new WriteRequest().writeRequest(data, addr, port, socket);
            } else {
                System.out.println("Invalid Opcode: " + op);
            }
        }
    }

    /**
     * Main method that starts the server.
     * This method initialises the server and calls start method to start receiving requests.
     *
     * @param args command-line arguments - not used.
     * @throws IOException if there is an error while starting/init the server or receiving requests.
     */
    public static void main(String[] args) throws IOException {
        TFTPServer server = new TFTPServer();
        server.start();
    }
}
