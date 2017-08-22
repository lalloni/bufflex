package bufflex;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.copyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import bufflex.FileOverflowUnboundedBuffer;
import bufflex.NIOUtils;

@RunWith(Parameterized.class)
public class FileOverflowUnboundedBufferTest {

    private int dataSize;
    private int memoryBufferSize;

    public FileOverflowUnboundedBufferTest(int dataSize, int memoryBufferSize) {
        this.dataSize = dataSize;
        this.memoryBufferSize = memoryBufferSize;
    }

    @Parameters(name = "Test {index}: data={0} memoryBuffer={1}")
    public static Collection<? extends Object> streamSize() {
        return Arrays.asList(new Object[][] {
            { 100, 50 },
            { 100, 99 },
            { 100, 100 },
            { 50, 100 },
            { 99, 100 },
            { 0, 100 },
            { 0, 0 },
            { 100, 0 },
            { 1, 0 },
            { 1, 10 },
        });
    }

    @Test
    public void testInputStream() throws IOException {
        byte[] bytes = new byte[dataSize];
        randomize(bytes);
        Path diskBufferPath;
        try (FileOverflowUnboundedBuffer buffer = new FileOverflowUnboundedBuffer(new ByteArrayInputStream(bytes), memoryBufferSize)) {
            boolean overflowedToDisk = buffer.getDiskBufferPath() != null && Files.exists(buffer.getDiskBufferPath());
            assertThat("should have overflowed to disk", overflowedToDisk, is(dataSize > memoryBufferSize));
            ByteBuffer target = ByteBuffer.allocate(dataSize + 1);
            ReadableByteChannel channel = Channels.newChannel(buffer.inputStream());
            NIOUtils.readBytes(channel, target);
            assertThat("buffer is not full", target.hasRemaining(), is(true));
            assertThat("one byte remains in buffer", target.remaining(), is(1));
            assertThat("read bytes equals written", copyOf(target.array(), dataSize), is(bytes));
            diskBufferPath = buffer.getDiskBufferPath();
        }
        assertThat("disk buffer file is deleted", diskBufferPath != null && Files.exists(diskBufferPath), is(false));
    }

    private void randomize(byte[] arr) {
        Random random = new Random(currentTimeMillis());
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) (random.nextInt(0xff + 1) & 0x7f);
        }
    }

}
