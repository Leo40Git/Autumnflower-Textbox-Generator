package adudecalledleo.aftbg.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

final class WriterThread extends Thread {
    private final ConcurrentLinkedDeque<String> messageStack;
    private BufferedWriter writer;

    public WriterThread(BufferedWriter writer) {
        super("LogFileWriter");
        setDaemon(true);
        this.messageStack = new ConcurrentLinkedDeque<>();
        this.writer = writer;
    }

    public void offerMessage(String message) {
        messageStack.offer(message);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            boolean didWrite = false;
            while (!messageStack.isEmpty()) {
                try {
                    writer.write(messageStack.removeFirst());
                    writer.newLine();
                    didWrite = true;
                } catch (IOException ignored) { }
            }
            if (didWrite) {
                try {
                    writer.flush();
                } catch (IOException ignored) { }
            }
        }

        // make last-ditch effort to write all messages
        while (!messageStack.isEmpty()) {
            try {
                writer.write(messageStack.removeFirst());
                writer.newLine();
            } catch (IOException ignored) { }
        }

        try {
            writer.close();
        } catch (IOException ignored) { }
        writer = null;
    }
}
