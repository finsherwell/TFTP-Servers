package TFTP_TCP_Server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConnectionHandler {
    private final Socket connectedSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ConnectionHandler(Socket socket) {
        this.connectedSocket = socket;
    }

    /**
     * Processes the client session including handshake, reply, and request.
     */
    public void process() {
        try {
            inputStream = new DataInputStream(connectedSocket.getInputStream());
            outputStream = new DataOutputStream(connectedSocket.getOutputStream());

            checkHandshake();
            replyHandshake();
            handleRequest();

        } catch (IOException err) {
            System.err.println("Client Error: " + err.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                connectedSocket.close();
                System.out.println("Client Disconnected");
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Sends "CONNECT" message to the client to acknowledge connection.
     *
     * @throws IOException If an IO error occurs while sending the message.
     */
    private void replyHandshake() throws IOException {
        outputStream.write(Constants.HANDSHAKE_MESSAGE.getBytes());
        outputStream.flush();
    }

    /**
     * Checks the handshake message from the client to ensure is valid.
     *
     * @throws IOException If the handshake message is invalid or an IO error occurs.
     */
    private void checkHandshake() throws IOException {
        byte[] buf = new byte[Constants.HANDSHAKE_LEN];
        int readBytes = inputStream.read(buf);

        if (readBytes != Constants.HANDSHAKE_LEN) {
            throw new IOException("Invalid Handshake!");
        }

        String msg = new String(buf);

        if (!msg.equals(Constants.HANDSHAKE_MESSAGE)) {
            throw new IOException("Invalid Handshake: " + msg);
        }
    }

    /**
     * Handles the request from the client, processes either read or write.
     *
     * @throws IOException If there is issue with the format or an IO error.
     */
    private void handleRequest() throws IOException {
        byte[] reqType = new byte[2];
        int readBytes = inputStream.read(reqType, 0, 2);

        if (readBytes != 2) {
            throw new IOException("Invalid Header");
        }

        if (reqType[0] != 0) {
            throw new IOException("Invalid Packet");
        }

        byte opType = reqType[1];
        if (opType == Constants.WRQ) {
            handleUpload();
        } else if (opType == Constants.RRQ) {
            handleDownload();
        } else {
            sendError("Unsupported Opcode: " + opType);
        }
    }

    /**
     * Handles an upload request from the client by receiving and saving data.
     *
     * @throws IOException If there is an issue during uploading file or IO error.
     */
    private void handleUpload() throws IOException {
        String fname = FileUtils.readNullString(inputStream);
        if (fname == null || fname.isEmpty()) {
            sendError("Invalid filename in request");
            return;
        }

        FileUtils.readNullString(inputStream);

        fname = new File(fname).getName();
        Path fpath = Paths.get(Constants.FILE_STORE, fname);

        System.out.println("Processing Upload: " + fname);

        try (FileOutputStream fos = new FileOutputStream(fpath.toString())) {
            short nxtBlock = 1;
            boolean done = false;

            while (!done) {
                byte[] hdr = new byte[4];
                int hdrSize = inputStream.read(hdr, 0, 4);

                if (hdrSize != 4) {
                    throw new IOException("Incomplete Header");
                }

                if (hdr[0] != 0 || hdr[1] != Constants.DATA) {
                    throw new IOException("Invalid Packet");
                }

                short blockId = (short) (((hdr[2] & 0xFF) << 8) | (hdr[3] & 0xFF));

                if (blockId == nxtBlock) {
                    byte[] buf = new byte[Constants.LEN];
                    int dataLen = 0;
                    int bytesRead = inputStream.read(buf, 0, Constants.LEN);

                    if (bytesRead > 0) {
                        dataLen = bytesRead;
                        fos.write(buf, 0, dataLen);
                    }

                    nxtBlock++;

                    if (dataLen < Constants.LEN) {
                        done = true;
                    }
                } else {
                    throw new IOException("Block Sequence Error");
                }
            }

            System.out.println("Uploaded File Successfully: " + fname);

        } catch (IOException err) {
            System.err.println("Upload Error: " + err.getMessage());
            sendError("Upload Error: " + err.getMessage());
            Files.deleteIfExists(fpath);
        }
    }

    /**
     * Handles a download request from the client by sending the data.
     *
     * @throws IOException If there is an issue during file download or an IO error.
     */
    private void handleDownload() throws IOException {
        String fname = FileUtils.readNullString(inputStream);
        if (fname == null || fname.isEmpty()) {
            sendError("Invalid filename in request");
            return;
        }

        FileUtils.readNullString(inputStream);

        fname = new File(fname).getName();
        Path fpath = Paths.get(Constants.FILE_STORE, fname);

        System.out.println("Processing Download: " + fname);

        if (!Files.exists(fpath)) {
            sendError("File not found: " + fname);
            return;
        }

        try (FileInputStream fis = new FileInputStream(fpath.toString())) {
            short blkId = 1;
            byte[] buffer = new byte[Constants.LEN];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] dataPkt = new byte[bytesRead + 4];
                dataPkt[1] = Constants.DATA;
                dataPkt[2] = FileUtils.convertBlock(blkId, 2);
                dataPkt[3] = FileUtils.convertBlock(blkId, 3);

                dataPkt[0] = 0;
                System.arraycopy(buffer, 0, dataPkt, 4, bytesRead);

                outputStream.write(dataPkt);
                outputStream.flush();

                blkId++;

                if (bytesRead < Constants.LEN) {
                    break;
                }
            }

            if (bytesRead == Constants.LEN) {
                byte[] endPkt = new byte[4];
                endPkt[1] = Constants.DATA;
                endPkt[2] = FileUtils.convertBlock(blkId, 2);
                endPkt[3] = FileUtils.convertBlock(blkId, 3);

                endPkt[0] = 0;
                outputStream.write(endPkt);
                outputStream.flush();
            }

            System.out.println("Downloaded File Successfully: " + fname);

        } catch (IOException e) {
            sendError("File download failed: " + e.getMessage());
        }
    }

    /**
     * Sends an error message to the client from a request failure.
     *
     * @param errMsg The error message to be sent.
     * @throws IOException If an error occurs.
     */
    private void sendError(String errMsg) throws IOException {
        byte[] errPkt = new byte[errMsg.getBytes().length + 5];
        errPkt[1] = Constants.ERROR;
        errPkt[2] = 0;
        errPkt[3] = 0;

        errPkt[0] = 0;
        System.arraycopy(errMsg.getBytes(), 0, errPkt, 4, (errMsg.getBytes()).length);
        errPkt[errPkt.length - 1] = 0;

        outputStream.write(errPkt);
        outputStream.flush();
    }
}