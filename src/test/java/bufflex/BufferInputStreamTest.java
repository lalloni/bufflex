package bufflex;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.Test;

import bufflex.BufferInputStream;

public class BufferInputStreamTest {

    @Test
    public void testRead() throws IOException {
        byte[] source = "test".getBytes();
        ByteBuffer b = ByteBuffer.wrap(source);
        try (InputStream i = new BufferInputStream(b)) {
            byte[] target = new byte[source.length];
            int read = i.read(target);
            assertThat(read, equalTo(source.length));
            assertThat(target, equalTo(source));
            read = i.read(target);
            assertThat(read, equalTo(-1));
        }
    }

    @Test
    public void testAvailable() throws IOException {
        byte[] source = "test".getBytes();
        ByteBuffer b = ByteBuffer.wrap(source);
        try (InputStream i = new BufferInputStream(b)) {
            assertEquals(source.length, i.available());
            byte[] target = new byte[source.length];
            i.read(target);
            assertEquals(0, i.available());
        }
    }

}
