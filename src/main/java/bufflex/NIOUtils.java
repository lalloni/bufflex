package bufflex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;

abstract class NIOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    public static final long copyBytes(ReadableByteChannel input, WritableByteChannel output) throws IOException {
        return copyBytes(input, output, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    public static final long copyBytes(ReadableByteChannel input, WritableByteChannel output, ByteBuffer buffer)
        throws IOException {
        long copied = 0;
        while (input.read(buffer) >= 0 || buffer.position() > 0) {
            buffer.flip();
            copied += output.write(buffer);
            buffer.compact();
        }
        return copied;
    }

    public static int writeBytes(WritableByteChannel output, ByteBuffer buffer) throws IOException {
        int w = 0;
        while (buffer.hasRemaining()) {
            w += output.write(buffer);
        }
        return w;
    }

    public static int readBytes(ReadableByteChannel input, ByteBuffer buffer) throws IOException {
        int r = 0,
            rr = 0;
        while (buffer.hasRemaining() && r >= 0) {
            r = input.read(buffer);
            rr += r;
        }
        return rr;
    }

    public static long readBytes(ScatteringByteChannel input, ByteBuffer[] buffers) throws IOException {
        long r = 0,
            rr = 0;
        if (buffers.length > 0) {
            while (buffers[buffers.length - 1].hasRemaining() && r >= 0) {
                r = input.read(buffers);
                rr += r;
            }
        }
        return rr;
    }

    public static long writeBytes(GatheringByteChannel output, ByteBuffer[] buffers) throws IOException {
        long r = 0;
        if (buffers.length > 0) {
            while (buffers[buffers.length - 1].hasRemaining()) {
                r += output.write(buffers);
            }
        }
        return r;
    }

}
