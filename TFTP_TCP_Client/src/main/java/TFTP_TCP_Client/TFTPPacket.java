package TFTP_TCP_Client;

public class TFTPPacket {
    /**
     * Creates a data packet with the specified block number, data, and size.
     *
     * @param blockNum The block number for this packet.
     * @param data The necessary data.
     * @param size The size of the data.
     * @return A byte array of the data packet.
     */
    public byte[] dataPacket(int blockNum, byte[] data, int size) {
        byte[] packet = new byte[size + 4];
        packet[2] = convertBlock(blockNum, 2);
        packet[3] = convertBlock(blockNum, 3);

        packet[0] = 0;
        packet[1] = Constants.DATA;
        if (size > 0) {
            System.arraycopy(data, 0, packet, 4, size);
        }

        return packet;
    }

    /**
     * Creates a read request packet for a file.
     *
     * @param filename The name of the file.
     * @return A byte array of the read request.
     */
    public byte[] readReq(String filename) {
        return basePacket(Constants.RRQ, filename);
    }

    /**
     * Creates a write request packet for a file.
     *
     * @param filename The name of the file.
     * @return A byte array of the write request.
     */
    public byte[] writeReq(String filename) {
        return basePacket(Constants.WRQ, filename);
    }

    /**
     * Creates a base packet for either a read or write request.
     *
     * @param opcode The operation code.
     * @param filename The name of the file.
     * @return A byte array of the request packet.
     */
    public byte[] basePacket(byte opcode, String filename) {
        byte[] request = new byte[(filename.getBytes()).length + 4];
        request[1] = opcode;

        request[0] = 0;
        System.arraycopy(filename.getBytes(), 0, request, 2, (filename.getBytes()).length);

        request[request.length - 2] = 0;
        request[request.length - 1] = 0;
        return request;
    }

    /**
     * Converts a block number to a high of low byte.
     *
     * @param block The block number.
     * @param index The index of either a high byte (2) or low byte (3).
     * @return The converted byte.
     */
    public byte convertBlock(int block, int index) {
        if (index == 2) {
            return (byte) ((block >> 8) & 0xFF);
        } else {
            return (byte) (block & 0xFF);
        }
    }
}