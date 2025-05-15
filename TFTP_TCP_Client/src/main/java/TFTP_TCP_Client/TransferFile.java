package TFTP_TCP_Client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TransferFile {
    /**
     * Processes the transfer operation depending on the operation type.
     *
     * @param operation The transfer operation.
     * @return true if the transfer was successful, false otherwise.
     * @throws IOException If an IO error occurs during transfer.
     */
    public boolean processTransfer(TransferOperation operation) throws IOException {
        try (Socket connection = establishConnection()) {
            if (connection == null) {
                return false;
            }

            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            if (!serverConnect(in, out)) {
                return false;
            }

            if (operation.getType() == TransferType.UPLOAD) {
                return uploadFile(out, operation.getFilename());
            } else {
                return downloadFile(out, in, operation.getFilename());
            }
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Establishes a socket connection to the server using host and port constants.
     *
     * @return A socket connection to the server or null if connection fails.
     */
    private Socket establishConnection() {
        try {
            return new Socket(Constants.HOST, Constants.PORT);
        } catch (IOException e) {
            System.out.println("Connection Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Performs handshake with server by sending a "CONNECT" message and checks response.
     *
     * @param inputStream Input stream to read the response.
     * @param outputStream The output stream to send the handshake.
     * @return true if the handshake success and server responds, false otherwise.
     */
    private boolean serverConnect(DataInputStream inputStream, DataOutputStream outputStream) {
        try {
            outputStream.write(Constants.HANDSHAKE_MESSAGE.getBytes());
            outputStream.flush();

            byte[] responseBuffer = new byte[Constants.HANDSHAKE_LEN];
            int bytesRead = inputStream.read(responseBuffer);

            if (bytesRead != Constants.HANDSHAKE_LEN) {
                System.out.println("Error: Incomplete Handshake");
                return false;
            }

            String response = new String(responseBuffer);
            if (!response.equals(Constants.HANDSHAKE_MESSAGE)) {
                System.out.println("Error: " + response);
                return false;
            }

            return true;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles the process of uploading a file to the server.
     *
     * @param outputStream The output stream to send data.
     * @param filename The name of the file.
     * @return true if the upload was a success, false otherwise.
     * @throws IOException If an IO error occurs.
     */
    private boolean uploadFile(DataOutputStream outputStream, String filename) throws IOException {
        Path sourcePath = Paths.get(Constants.OUTGOING + filename);

        TFTPPacket packetBuilder = new TFTPPacket();
        outputStream.write(packetBuilder.writeReq(filename));
        outputStream.flush();

        try (FileInputStream fileStream = new FileInputStream(sourcePath.toString())) {
            int blockNumber = 1;
            byte[] buffer = new byte[Constants.LEN];
            int read;

            while ((read = fileStream.read(buffer)) != -1) {
                sendDataBlock(outputStream, packetBuilder, blockNumber, buffer, read);
                blockNumber++;
            }

            return true;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends data to the server during a file transfer.
     *
     * @param outputStream The output stream to send the data.
     * @param packet The object used to build the packet.
     * @param blockNum The block number.
     * @param data The data to send.
     * @param size The size of the data being sent.
     * @throws IOException If an IO error occurs while sending packet.
     */
    private void sendDataBlock(DataOutputStream outputStream, TFTPPacket packet, int blockNum, byte[] data, int size) throws IOException {
        byte[] p = packet.dataPacket(blockNum, data, size);
        outputStream.write(p);
        outputStream.flush();
    }

    /**
     * Handles the process of downloading a file from the server by sending a read request to the server
     * and getting the data in blocks.
     *
     * @param outputStream The stream to send data to the server.
     * @param inputStream Input stream to receive the file data from server.
     * @param filename The name of the file.
     * @return true if download was successful, false otherwise.
     * @throws IOException If an IO error occurs during this.
     */
    private boolean downloadFile(DataOutputStream outputStream, DataInputStream inputStream, String filename) throws IOException {
        TFTPPacket packetBuilder = new TFTPPacket();
        outputStream.write(packetBuilder.readReq(filename));
        outputStream.flush();

        Path destinationDir = Paths.get(Constants.INCOMING);
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }

        Path destinationPath = Paths.get(Constants.INCOMING + filename);
        boolean transferSuccessful = false;

        try (FileOutputStream fileOutputStream = new FileOutputStream(destinationPath.toString())) {
            short expectedBlockNumber = 1;
            boolean transferComplete = false;

            while (!transferComplete) {
                byte[] headerBytes = new byte[4];
                int headerSize = inputStream.read(headerBytes, 0, 4);

                if (headerSize != 4) {
                    throw new IOException("Invalid Header");
                }

                if (headerBytes[1] == Constants.ERROR) {
                    ByteArrayOutputStream errorMessage = new ByteArrayOutputStream();
                    int nextByte;
                    while ((nextByte = inputStream.read()) != 0 && nextByte != -1) {
                        errorMessage.write(nextByte);
                    }
                    throw new IOException("Error: " + new String(errorMessage.toByteArray()));
                }

                if (headerBytes[0] != 0 || headerBytes[1] != Constants.DATA) {
                    throw new IOException("Packet Format Invalid");
                }

                short blockNum = (short) (((headerBytes[2] & 0xFF) << 8) | (headerBytes[3] & 0xFF));

                if (blockNum == expectedBlockNumber) {
                    byte[] dataBuffer = new byte[Constants.LEN];
                    int bytesReceived = inputStream.read(dataBuffer, 0, Constants.LEN);

                    if (bytesReceived > 0) {
                        fileOutputStream.write(dataBuffer, 0, bytesReceived);
                    }

                    expectedBlockNumber++;

                    if (bytesReceived < Constants.LEN) {
                        transferComplete = true;
                        transferSuccessful = true;
                    }
                } else {
                    throw new IOException("Block Sequence Error!");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());

            if (!transferSuccessful && Files.exists(destinationPath)) {
                try {
                    Files.delete(destinationPath);
                } catch (IOException deleteError) {
                    System.out.println("Error: " + deleteError.getMessage());
                }
            }
        }
        return transferSuccessful;
    }
}
