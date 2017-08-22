package bufflex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

class BufferInputStream extends InputStream {

    private ByteBuffer buffer;

    public BufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        if (buffer.remaining() < 1) {
            return -1;
        }
        return buffer.get();
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }

}
