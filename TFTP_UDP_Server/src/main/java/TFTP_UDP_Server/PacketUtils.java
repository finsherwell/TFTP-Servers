package TFTP_UDP_Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PacketUtils {
    /**
     * Extracts filename from the provided request data.
     * The filename is located in the data starting from the 3rd byte and continues
     * until a null byte is encountered, which is the end of the filename.
     *
     * @param data The byte array containing the request data.
     * @return The extracted filename as a String.
     */
    public static String getFilename(byte[] data) {
        int len = 0;
        byte[] name = new byte[Constants.LEN];
        int dataLen = data.length;

        for (int i = 2; i < dataLen; i++) {
            if (data[i] == 0) {
                int pos = i - 2;
                len = pos;
                break;
            }

            byte d = data[i];
            name[i - 2] = d;
        }

        return new String(name, 0, len);
    }

    /**
     * Extracts block number from a given DatagramPacket.
     * The block number is stored in bytes 2 and 3 of the packet data.
     *
     * @param packet The packet containing the data.
     * @return The block number as a short.
     */
    public static short blockNum(DatagramPacket packet) {
        return (short) (((packet.getData()[2] & 0xFF) << 8) | (packet.getData()[3] & 0xFF));
    }

    /**
     * Sends an acknowledgment packet to the address and port.
     * If the init flag is true, it sends the initial ACK with block number 0.
     * Otherwise, it sends an ACK with the specified block number.
     *
     * @param init If true, send an initial ACK, otherwise send an ACK with specified block.
     * @param addr The address of the client to send the ACK.
     * @param port The port of the client to send the ACK.
     * @param serverSocket The socket used to send the ACK.
     * @param blockNum The block number to acknowledge.
     * @throws IOException If an IO error occurs while sending the ACK.
     */
    public static void sendAck(boolean init, InetAddress addr, int port, DatagramSocket serverSocket, short blockNum) throws IOException {
        if (init) {
            byte[] packet = createAck((short) 0);
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, addr, port);
            serverSocket.send(sendPacket);
        } else {
            byte[] ackPacket = createAck(blockNum);
            DatagramPacket sendPacket = new DatagramPacket(ackPacket, ackPacket.length, addr, port);
            serverSocket.send(sendPacket);
        }
    }

    /**
     * Waits for an acknowledgment packet from the client.
     * The server checks if the block number in the received packet matches what is expected.
     *
     * @param blockNum The expected block number to be acknowledged.
     * @param serverSocket The socket used to receive the ACK packet.
     * @throws IOException If an IO error occurs while receiving the ACK.
     */
    public static void recvAck(short blockNum, DatagramSocket serverSocket) throws IOException {
        byte[] buf = new byte[4];

        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        serverSocket.receive(packet);

        short recvBlock = blockNum(packet);
        if (recvBlock != blockNum) {
            System.out.println("Invalid Block!");
        }
    }

    /**
     * Creates and sends an error packet with the error message.
     * The error message is included in the packet and terminated with a null byte.
     *
     * @param msg The error message to be included in the ERR packet.
     * @param addr The address of the client to send the error packet.
     * @param port The client port to send the error packet.
     * @param serverSocket The socket used to send the packet.
     * @throws IOException If an IO error occurs while sending the error packet.
     */
    public static void createErr(String msg, InetAddress addr, int port, DatagramSocket serverSocket) throws IOException {
        int len = 4 + msg.length() + 1;

        byte[] msgBytes = msg.getBytes();
        byte[] packet = new byte[len];
        packet[1] = Constants.ERROR;
        packet[2] = 0;
        packet[3] = 1;

        packet[0] = 0;
        System.arraycopy(msgBytes, 0, packet, 4, msgBytes.length);

        packet[packet.length - 1] = 0;

        DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, addr, port);
        serverSocket.send(sendPacket);
    }

    /**
     * Creates a DATA packet with the block number and data.
     * The DATA packet is used to send file data from the server to the client.
     *
     * @param data The byte array containing the file data.
     * @param blockNum The block number of the data being sent.
     */
    public static void createData(byte[] data, short blockNum) {
        data[1] = Constants.DATA;
        data[2] = FileUtils.convertBlock(blockNum, 2);
        data[3] = FileUtils.convertBlock(blockNum, 3);
        data[0] = 0;
    }

    /**
     * Creates an acknowledgment packet with the block number.
     * The ACK packet is sent to the client to acknowledge the receipt of data blocks.
     *
     * @param blockNum The block number to acknowledge.
     * @return A byte array representing the ACK packet.
     */
    public static byte[] createAck(short blockNum) {
        byte[] packet = new byte[4];
        packet[1] = Constants.ACK;
        packet[2] = FileUtils.convertBlock(blockNum, 2);
        packet[3] = FileUtils.convertBlock(blockNum, 3);

        packet[0] = 0;
        return packet;
    }


}
