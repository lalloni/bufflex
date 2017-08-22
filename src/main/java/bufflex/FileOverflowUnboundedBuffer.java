package bufflex;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileOverflowUnboundedBuffer implements AutoCloseable {

    private static final int DEFAULT_MEMORY_BUFFER_BYTES = 256 * 1024;

    private static final int FILE_BUFFER_BUFFER_BYTES = 4 * 4 * 1024; // 4 "AF" sectors

    private ByteBuffer memoryBuffer;

    private Path fileBufferPath;

    private long size;

    private boolean closed;

    private boolean compressFileBuffer;

    public FileOverflowUnboundedBuffer(InputStream inputStream) throws IOException {
        this(inputStream, DEFAULT_MEMORY_BUFFER_BYTES);
    }

    public FileOverflowUnboundedBuffer(InputStream inputStream, int memoryBufferSize) throws IOException {
        this(inputStream, memoryBufferSize, true);
    }

    public FileOverflowUnboundedBuffer(InputStream inputStream, int memoryBufferSize, boolean compressFileBuffer)
        throws IOException {
        this.compressFileBuffer = compressFileBuffer;
        closed = false;
        memoryBuffer = ByteBuffer.allocate(memoryBufferSize);
        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        size = NIOUtils.readBytes(inputChannel, memoryBuffer);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int read = inputChannel.read(buffer);
        if (read >= 0) { // not EOF
            buffer.flip();
            fileBufferPath = Files.createTempFile("buffer-", ".tmp");
            OutputStream outputStream = new FileOutputStream(fileBufferPath.toFile());
            try {
                if (compressFileBuffer) {
                    outputStream = new GZIPOutputStream(outputStream, FILE_BUFFER_BUFFER_BYTES, true);
                }
                WritableByteChannel outputChannel = Channels.newChannel(outputStream);
                size += NIOUtils.writeBytes(outputChannel, buffer);
                size += NIOUtils.copyBytes(inputChannel, outputChannel);
                outputChannel.close();
            } finally {
                outputStream.close();
            }
        }
        memoryBuffer.flip();
    }

    public InputStream inputStream() throws IOException {
        if (closed) {
            throw new IllegalStateException("Buffer is closed");
        }
        InputStream inputStream = new BufferInputStream(memoryBuffer.duplicate());
        if (fileBufferPath != null) {
            InputStream diskInputStream = new FileInputStream(fileBufferPath.toFile());
            if (compressFileBuffer) {
                diskInputStream = new GZIPInputStream(diskInputStream, FILE_BUFFER_BUFFER_BYTES);
            }
            inputStream = new SequenceInputStream(inputStream, diskInputStream);
        }
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (fileBufferPath != null && Files.exists(fileBufferPath)) {
            Files.delete(fileBufferPath);
        }
    }

    public long getSize() {
        return size;
    }

    public Path getDiskBufferPath() {
        return fileBufferPath;
    }

}
