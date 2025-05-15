package TFTP_UDP_Server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class FileUtils {
    /**
     * Writes the data to a file with the given filename.
     * This method opens the file in write mode and writes the byte array data to the file.
     * If the file does not exist, it will be created.
     *
     * @param fileName The name of the file which the data will be written.
     * @param data The byte array containing the data to be written to the file.
     * @throws IOException If an error occurs while writing to the file.
     */
    public static void writeFile(String fileName, byte[] data) throws IOException {
        try (FileOutputStream output = new FileOutputStream(Constants.DIRECTORY + fileName)) {
            output.write(data);
        }
    }

    /**
     * Reads part of a file starting from the offset and returns up to the specified length of data.
     * If file does not exist, it throws an IO exception.
     * If the offset is bigger than the file's size, it will skip the requested number
     * of bytes and return an empty byte array.
     *
     * @param filename Name of the file to be read.
     * @param offset The position from which the reading should start in the file.
     * @param length The number of bytes to read from the file.
     * @return A byte array containing the read data from the file.
     * @throws IOException For if error occurs while reading from the file or if the file does not exist.
     */
    public static byte[] readFile(String filename, int offset, int length) throws IOException {
        File file = new File(Constants.DIRECTORY + filename);
        if (!file.exists()) {
            throw new IOException("File not found: " + filename);
        }

        try (FileInputStream input = new FileInputStream(file)) {
            long skipped = input.skip(offset);
            if (skipped < offset) {
                return new byte[0];
            }

            byte[] data = new byte[length];
            int read = input.read(data, 0, length);

            if (read == -1) {
                return new byte[0];
            }

            if (read < length) {
                byte[] actualData = new byte[read];
                System.arraycopy(data, 0, actualData, 0, read);
                return actualData;
            }
            return data;
        }
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