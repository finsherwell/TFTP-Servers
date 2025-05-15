package TFTP_TCP_Server;

import java.io.*;

public class FileUtils {
    /**
     * Reads a null-terminated string from the input stream.
     *
     * @param in The input stream to read.
     * @return The string read from the stream.
     * @throws IOException If an error occurs while reading the string.
     */
    public static String readNullString(DataInputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != 0 && b != -1) {
            buf.write(b);
        }
        if (b == -1) {
            throw new IOException("Unexpected Stream End");
        }
        return buf.toString();
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
            return (byte) ((block >> 8) & 0xFF);
        } else {
            return (byte) (block & 0xFF);
        }
    }
}