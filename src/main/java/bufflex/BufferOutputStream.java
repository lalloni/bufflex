package bufflex;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class BufferOutputStream extends OutputStream {

    private ByteBuffer buffer;

    public BufferOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int data) throws IOException {
        buffer.put((byte) data);
    }

}
