package TFTP_TCP_Client;

public class TransferOperation {
    private final String filename;
    private final TransferType type;

    /**
     * Constructs with a filename and transfer type.
     *
     * @param filename The name of the file.
     * @param type The type of transfer.
     */
    public TransferOperation(String filename, TransferType type) {
        this.filename = filename;
        this.type = type;
    }

    /**
     * Gets the filename of the transfer operation.
     *
     * @return The name of the file.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the transfer type of the operation.
     *
     * @return The type of transfer.
     */
    public TransferType getType() {
        return type;
    }
}

