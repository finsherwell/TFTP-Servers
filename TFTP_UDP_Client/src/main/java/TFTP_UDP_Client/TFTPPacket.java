package TFTP_UDP_Client;

public class TFTPPacket {
    /**
     * Creates a read request packet for the client to send to server.
     * The packet contains the filename and the request type to retrieve file from the server.
     *
     * @param fileName The filename that the client wants to read from the server.
     * @return A byte array of the read request packet being sent to the server.
     */
    public static byte[] readPacket(String fileName) {
        return createPacket(fileName, Constants.RRQ);
    }

    /**
     * Creates a write request packet for the client to send to server.
     * The packet contains the filename and the type of request to write the file to server.
     *
     * @param fileName The file that the client wants to write to the server.
     * @return A byte array of the write request packet being sent to the server.
     */
    public static byte[] writePacket(String fileName) {
        return createPacket(fileName, Constants.WRQ);
    }

    /**
     * A method that creates a packet for a read or write request.
     * The packet consists of a header (0, opcode) and the filename.
     *
     * @param filename The name of the file for the request.
     * @param opcode The opcode indicating the type of request.
     * @return A byte array representing the packet.
     */
    private static byte[] createPacket(String filename, byte opcode) {
        byte[] fileNameBytes = filename.getBytes();
        byte[] packet = new byte[fileNameBytes.length + 4];

        packet[0] = 0;
        packet[1] = opcode;

        System.arraycopy(fileNameBytes, 0, packet, 2, fileNameBytes.length);
        packet[packet.length - 1] = 0;
        return packet;
    }

    /**
     * Creates a data packet to be sent to the server during file transfer.
     * The packet contains block number, the data to be transferred, and the size of the block.
     *
     * @param data The data to be sent to the server.
     * @param size The size of the data being sent.
     * @param blockNum The block number for the current packet.
     * @return A byte array representing the data packet being sent.
     */
    public static byte[] dataPacket(byte[] data, int size, short blockNum) {
        byte[] packet = new byte[size + 4];
        packet[1] = Constants.DATA;

        packet[2] = convertBlock(blockNum, 2);
        packet[3] = convertBlock(blockNum, 3);

        packet[0] = 0;
        System.arraycopy(data, 0, packet, 4, size);
        return packet;
    }

    /**
     * Creates an acknowledgment packet for a block number.
     * The packet is sent by the client to acknowledge a block of data being sent during a file transfer.
     *
     * @param blockNum The block number being acknowledged.
     * @return A byte array representing the acknowledgment packet.
     */
    public static byte[] ackPacket(short blockNum) {
        byte[] packet = new byte[4];
        packet[1] = Constants.ACK;
        packet[2] = convertBlock(blockNum, 2);
        packet[3] = convertBlock(blockNum, 3);

        packet[0] = 0;
        return packet;
    }

    /**
     * Converts a block number to a high of low byte.
     *
     * @param block The block number.
     * @param index The index of either a high byte (2) or low byte (3).
     * @return The converted byte.
     */
    public static byte convertBlock(int block, int index) {
        if (index == 2) {
            return (byte) (block >> 8);
        } else {
            return (byte) (block & 0xFF);
        }
    }
}