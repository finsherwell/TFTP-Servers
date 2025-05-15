package TFTP_TCP_Client;

public class Constants {
    // Opcodes
    public static final byte RRQ = 1;
    public static final byte WRQ = 2;
    public static final byte DATA = 3;
    public static final byte ERROR = 5;

    // Constants
    public static final int PORT = 1025;
    public static final String HOST = "localhost";
    public static final int LEN = 512;
    public static final String INCOMING = "incoming/";
    public static final String OUTGOING = "outgoing/";

    // Handshake
    public static final String HANDSHAKE_MESSAGE = "CONNECT";
    public static final int HANDSHAKE_LEN = 7;
}