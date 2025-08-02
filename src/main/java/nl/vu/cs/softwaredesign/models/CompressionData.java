package nl.vu.cs.softwaredesign.models;

public class CompressionData {
    private long originalSize;
    private long compressedSize;
    private long compressionTime;

    public final static int KB = 1024;

    public long getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(long originalSize) {
        this.originalSize = originalSize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(long compressedSize) {
        this.compressedSize = compressedSize;
    }

    public long getCompressionTime() {
        return compressionTime;
    }

    public void setCompressionTime(long compressionTime) {
        this.compressionTime = compressionTime;
    }
}
