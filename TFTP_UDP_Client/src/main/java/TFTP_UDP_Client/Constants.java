package TFTP_UDP_Client;

public class Constants {
    // Opcodes
    public static final byte RRQ = 1;
    public static final byte WRQ = 2;
    public static final byte DATA = 3;
    public static final byte ACK = 4;
    public static final byte ERROR = 5;

    // Error codes
    public static final short ERR_FILE_NOT_FOUND = 1;

    // Constants
    public static final int PORT = 1025;
    public static final String HOST = "localhost";
    public static final int TIMEOUT = 5000;
    public static final int LEN = 512;
    public static final int MAX_LEN = LEN + 4;
    public static final String INCOMING = "incoming/";
    public static final String OUTGOING = "outgoing/";
}