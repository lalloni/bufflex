package bufflex;

import java.io.IOException;
import java.io.InputStream;

class SequenceInputStream extends InputStream {

    private InputStream[] inputs;

    private int current = 0;

    public SequenceInputStream(InputStream... inputs) {
        this.inputs = inputs;
    }

    @Override
    public int read() throws IOException {
        int r = -1;
        if (inputs.length > 0) {
            r = inputs[current].read();
            if (r == -1 && current < inputs.length - 1) {
                current = current + 1;
                r = read();
            }
        }
        return r;
    }

    @Override
    public int available() throws IOException {
        int r = 0;
        if (inputs.length > 0) {
            r = inputs[current].available();
        }
        return r;
    }

    @Override
    public void close() throws IOException {
        for (InputStream input : inputs) {
            input.close();
        }
    }

}
