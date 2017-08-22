package bufflex;

import static java.lang.System.arraycopy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import bufflex.SequenceInputStream;

public class SequenceInputStreamTest {

    @Test
    public void testRead() throws IOException {
        byte[] source = "bla".getBytes();
        try (SequenceInputStream stream = new SequenceInputStream(new ByteArrayInputStream(source),
            new ByteArrayInputStream(source), new ByteArrayInputStream(source))) {
            byte[] target = new byte[source.length * 3];
            stream.read(target);
            byte[] expect = new byte[source.length * 3];
            arraycopy(source, 0, expect, 0, source.length);
            arraycopy(source, 0, expect, source.length, source.length);
            arraycopy(source, 0, expect, 2 * source.length, source.length);
            assertThat(target, equalTo(expect));
            assertThat(new String(expect), equalTo("blablabla"));
        }
    }

    @Test
    public void testAvailable() throws IOException {
        byte[] source = "bla".getBytes();
        try (SequenceInputStream stream = new SequenceInputStream(new ByteArrayInputStream(source),
            new ByteArrayInputStream(source), new ByteArrayInputStream(source))) {
            for (int i = 0; i < 3; i++) {
                for (int a = i > 0 ? 2 : 3; a >= 0; a--) {
                    assertThat(stream.available(), equalTo(a));
                    stream.read();
                }
            }
        }
    }

}
