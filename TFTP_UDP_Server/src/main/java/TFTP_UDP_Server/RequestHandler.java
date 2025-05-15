package TFTP_UDP_Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public interface RequestHandler {
    void readRequest(byte[] data, InetAddress addr, int port, DatagramSocket serverSocket) throws IOException;
    void writeRequest(byte[] data, InetAddress addr, int port, DatagramSocket serverSocket) throws IOException;
}
