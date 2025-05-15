package TFTP_UDP_Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReadRequest implements RequestHandler {
    /**
     * Handles read request from client. This method reads the requested file in blocks,
     * sends the blocks to the client, and waits for acknowledgments (ACK) for each block.
     *
     * @param data The data received in the request. This should include the filename and any other information.
     * @param addr The address of client requesting the file.
     * @param port The port on the client to send the response to.
     * @param serverSocket The DatagramSocket for sending and receiving packets.
     * @throws IOException If an I/O error occurs during the process, such as file reading errors.
     */
    @Override
    public void readRequest(byte[] data, InetAddress addr, int port, DatagramSocket serverSocket) throws IOException {
        String filename = PacketUtils.getFilename(data);
        System.out.println("Received read request for file: " + filename);
        try {
            int off = 0;
            boolean lastBlock = false;
            short blockNo = 1;

            while (!lastBlock) {
                byte[] fileData = FileUtils.readFile(filename, off, Constants.LEN);
                byte[] buf = new byte[Constants.MAX_LEN];
                int bytesRead = fileData.length;

                System.arraycopy(fileData, 0, buf, 4, bytesRead);
                PacketUtils.createData(buf, blockNo);

                DatagramPacket sendPacket = new DatagramPacket(buf, bytesRead + 4, addr, port);
                serverSocket.send(sendPacket);

                try {
                    PacketUtils.recvAck(blockNo, serverSocket);
                } catch (IOException e) {
                    continue;
                }

                blockNo++;
                off += bytesRead;

                if (bytesRead < Constants.LEN) {
                    lastBlock = true;
                }
            }
        } catch (IOException e) {
            PacketUtils.createErr(e.getMessage(), addr, port, serverSocket);
        }
    }

    @Override
    public void writeRequest(byte[] data, InetAddress addr, int port, DatagramSocket serverSocket) {
        // Not Implemented
    }
}