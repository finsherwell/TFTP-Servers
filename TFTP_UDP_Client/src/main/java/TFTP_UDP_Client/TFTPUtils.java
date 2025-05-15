package TFTP_UDP_Client;

import java.io.*;
import java.net.*;

public class TFTPUtils {
    /**
     * Checks if the file with the given filename exists in the outgoing folder.
     * This method checks if the file exists and is a regular file in the outgoing folder.
     *
     * @param filename The name of the file to check.
     * @return True if the file exists and is a regular file, false otherwise.
     */
    public static boolean fileExists(String filename) {
        File folder = new File(Constants.OUTGOING);
        if (folder.exists() && folder.isDirectory()) {
            File file = new File(folder, filename);
            return file.exists() && file.isFile();
        } else {
            return false;
        }
    }

    /**
     * Checks if the received packet is an error packet.
     * This method checks if the opcode in the received packet is an error type.
     *
     * @param data The data received in the packet.
     * @return True if the packet is an error packet, false otherwise.
     */
    private static boolean isErrorPacket(byte[] data) {
        return data[1] == Constants.ERROR;
    }

    /**
     * Handles an error packet received from the server.
     * The method extracts the error code and message from the packet and prints an error message.
     *
     * @param packet Packet containing the error message.
     */
    private static void handleErrorPacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        int errorCode = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

        StringBuilder msg = new StringBuilder();
        for (int i = 4; i < packet.getLength() - 1; i++) {
            if (data[i] == 0) break;
            msg.append((char) data[i]);
        }

        String errorType = "Unknown Error";

        switch (errorCode) {
            case Constants.ERR_FILE_NOT_FOUND:
                errorType = "File Not Found";
                break;
        }

        System.out.println("Error: " + errorType);
    }

    /**
     * Sends a write request to the server to upload a file.
     * It sends the WRQ packet, waits for the first ACK, and then sends the file in blocks.
     *
     * @param filename The name of the file to be uploaded.
     * @param addr The address of the server.
     * @param socket The socket used to send and receive data.
     * @param port The port on the server.
     * @throws IOException If an IO error occurs when talking with the server.
     */
    public static void writeRequest(String filename, InetAddress addr, DatagramSocket socket, int port) throws IOException {
        byte[] packet = TFTPPacket.writePacket(filename);
        DatagramPacket send = new DatagramPacket(packet, packet.length, addr, port);
        socket.send(send);
        firstAck(socket);
        sendFile(addr, port, socket, filename);
    }

    /**
     * Sends a read request to the server to download a file.
     * It sends the RRQ packet and waits to receive the file in blocks from server.
     *
     * @param filename The name of the file to be downloaded.
     * @param addr The server address.
     * @param socket The socket used to send and receive data.
     * @param port The port on the server.
     * @throws IOException If an IO error occurs during talking with the server.
     */
    public static void readRequest(String filename, InetAddress addr, DatagramSocket socket, int port) throws IOException {
        byte[] packet = TFTPPacket.readPacket(filename);
        DatagramPacket send = new DatagramPacket(packet, packet.length, addr, port);
        socket.send(send);
        recvFile(socket, filename);
    }

    /**
     * Waits for an acknowledgment packet from the server after sending data.
     * Checks the received packet for errors and valid acknowledgment.
     *
     * @param socket The socket used to receive the acknowledgment.
     * @throws IOException If an IO error occurs or an error packet is received.
     */
    private static void recvAck(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[Constants.MAX_LEN];
        socket.setSoTimeout(Constants.TIMEOUT);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        socket.receive(packet);
        byte[] data = packet.getData();

        if (isErrorPacket(data)) {
            handleErrorPacket(packet);
            throw new IOException("Server Error");
        }

        if (data[1] != Constants.ACK) {
            System.out.println("Invalid Acknowledgement: " + data[1]);
        }
    }

    /**
     * Receives file from the server and writes it to incoming folder.
     * The method waits for data packets from the server, writes data to a file, and sends ACK for each block.
     *
     * @param socket The socket used to receive data.
     * @param fileName The name of file to save the received data.
     * @throws IOException If an IO error occurs while receiving or writing the file.
     */
    private static void recvFile(DatagramSocket socket, String fileName) throws IOException {
        File incomingDir = new File(Constants.INCOMING);
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileOutputStream fileOutput = null;

        try {
            short block = 1;
            boolean finished = false;
            boolean receivedData = false;

            while (!finished) {
                byte[] buf = new byte[Constants.MAX_LEN];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                if (isErrorPacket(packet.getData())) {
                    handleErrorPacket(packet);
                    return;
                }

                if (!receivedData) {
                    receivedData = true;
                }

                short recvBlock = (short) (((packet.getData()[2] & 0xFF) << 8) | (packet.getData()[3] & 0xFF));
                if (recvBlock == block) {
                    int size = packet.getLength() - 4;
                    outputStream.write(packet.getData(), 4, size);
                    sendAck(socket, packet.getAddress(), packet.getPort(), block);
                    block++;

                    if (size < Constants.LEN) {
                        finished = true;
                    }
                }
            }

            if (receivedData) {
                fileOutput = new FileOutputStream(Constants.INCOMING + fileName);
                fileOutput.write(outputStream.toByteArray());
                System.out.println("File Received: " + fileName);
            }
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (fileOutput != null) {
                    fileOutput.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing streams: " + e.getMessage());
            }
        }
    }

    /**
     * Sends an acknowledgment packet for a specific block number to the server.
     * This method acknowledges receiving a data block.
     *
     * @param socket The socket used to send the acknowledgment.
     * @param addr The address of the server.
     * @param port The port of the server.
     * @param blockNumber The block number to acknowledge.
     * @throws IOException If an IO error occurs while sending the ACK packet.
     */
    private static void sendAck(DatagramSocket socket, InetAddress addr, int port, short blockNumber) throws IOException {
        byte[] packet = TFTPPacket.ackPacket(blockNumber);
        DatagramPacket sendP = new DatagramPacket(packet, packet.length, addr, port);
        socket.send(sendP);
    }

    /**
     * Waits for the first acknowledgment after sending a request to the server.
     * This ACK is used to begin the file transferring.
     *
     * @param socket The socket used to receive the first ACK.
     * @throws IOException If an IO error occurs or if an invalid ACK is received.
     */
    private static void firstAck(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[4];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        byte[] data = packet.getData();
        if (data[1] != Constants.ACK) {
            System.out.println("Invalid ACK");
        }
    }

    /**
     * Sends the file to the server in blocks.
     * This method reads the file, creates data packets, sends them to server, and waits for ACK.
     *
     * @param addr The address of the server.
     * @param port The port of the server.
     * @param socket The socket used to send data.
     * @param filename The name of the file to be sent.
     */
    private static void sendFile(InetAddress addr, int port, DatagramSocket socket, String filename) {
        String path = Constants.OUTGOING + filename;
        try (FileInputStream input = new FileInputStream(path)) {
            short block = 1;
            byte[] buf = new byte[Constants.LEN];
            int read;

            while ((read = input.read(buf, 0, Constants.LEN)) != -1) {
                sendData(addr, port, block, buf, read, socket);
                recvAck(socket);
                block++;

                if (read < Constants.LEN) {
                    break;
                }
            }

            if (read == Constants.LEN) {
                sendData(addr, port, block, new byte[0], 0, socket);
                recvAck(socket);
            }

            System.out.println("File Sent: " + filename);
        } catch (IOException e) {
            System.out.println("Error Reading File: " + e.getMessage());
        }
    }

    /**
     * Sends data as a data packet to the server.
     * This packages the data and sends it as a DATA packet with the correct block number.
     *
     * @param addr The address of the server.
     * @param port The port of the server.
     * @param blockNum The block number of the data being sent.
     * @param data The data to be sent.
     * @param dataSize The size of the data being sent.
     * @param socket The socket used to send the data.
     * @throws IOException If an IO error occurs while sending the data packet.
     */
    private static void sendData(InetAddress addr, int port, short blockNum, byte[] data, int dataSize, DatagramSocket socket) throws IOException {
        byte[] packet = TFTPPacket.dataPacket(data, dataSize, blockNum);
        DatagramPacket send = new DatagramPacket(packet, packet.length, addr, port);
        socket.send(send);
    }
}