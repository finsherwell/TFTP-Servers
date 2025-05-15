package TFTP_UDP_Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WriteRequest implements RequestHandler {
    @Override
    public void readRequest(byte[] data, InetAddress addr, int port, DatagramSocket serverSocket) throws IOException {
        // Not Implemented
    }

    /**
     * Handles write request from a client. Receives file data in blocks and accumulates,
     * acknowledging each block after it's received. Once all blocks are received, writes the complete
     * file to disk.
     *
     * @param data The data received in the request.
     * @param addr The client's address.
     * @param port The client's port.
     * @param serverSocket The socket for sending and receiving packets.
     * @throws IOException If an IO error occurs.
     */
    @Override
    public void writeRequest(byte[] data, InetAddress addr, int port, DatagramSocket serverSocket) throws IOException {
        String filename = PacketUtils.getFilename(data);
        System.out.println("Received write request for file: " + filename);
        PacketUtils.sendAck(true, addr, port, serverSocket, (short) 0);

        try {
            short blockNum = 1;
            ByteArrayOutputStream accum = new ByteArrayOutputStream();

            while (true) {
                byte[] buffer = new byte[Constants.MAX_LEN];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                if (addr.equals(packet.getAddress()) && port == packet.getPort()) {
                    short recvBlock = PacketUtils.blockNum(packet);
                    if (recvBlock == blockNum) {
                        int size = packet.getLength() - 4;
                        byte[] blockData = new byte[size];
                        System.arraycopy(packet.getData(), 4, blockData, 0, size);

                        accum.write(blockData);

                        PacketUtils.sendAck(false, addr, port, serverSocket, blockNum);
                        blockNum++;

                        if (size < Constants.LEN) {
                            FileUtils.writeFile(filename, accum.toByteArray());
                            System.out.println("File Transferred!");
                            break;
                        }
                    } else {
                        System.out.println("Block Number Incorrect!");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}