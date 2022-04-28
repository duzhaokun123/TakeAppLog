package io.github.duzhaokun123.takeapplog;

import java.io.Closeable;

public class CloseableProcess implements Closeable {
    private final Process process;

    CloseableProcess(final Process process) {
        this.process = process;
    }

    @Override
    public void close() {
        process.destroy();
    }

    public Process process() {
        return process;
    }
}
