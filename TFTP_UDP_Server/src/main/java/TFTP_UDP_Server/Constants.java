package TFTP_UDP_Server;

public class Constants {
    // Opcodes
    public static final byte DATA = 3;
    public static final byte ACK = 4;
    public static final byte ERROR = 5;

    // Constants
    public static final int LEN = 512;
    public static final int MAX_LEN = LEN + 4;
    public static final int PORT = 1025;
    public static String DIRECTORY = "files/";
}
