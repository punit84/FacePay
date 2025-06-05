package com.punit.facepay.service;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.transcribestreaming.model.AudioEvent;
import software.amazon.awssdk.services.transcribestreaming.model.AudioStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AudioStreamPublisher implements Publisher<AudioStream>, AutoCloseable {
    private final InputStream inputStream;
    private final ExecutorService executor;
    private static final int CHUNK_SIZE_IN_BYTES = 1024 * 1;
    private final AtomicLong demand = new AtomicLong(0);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public AudioStreamPublisher(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        this.inputStream = inputStream;
        this.executor = Executors.newFixedThreadPool(1);
    }

    @Override
    public void subscribe(Subscriber<? super AudioStream> s) {
        if (closed.get()) {
            s.onError(new IllegalStateException("Publisher is closed"));
            return;
        }

        s.onSubscribe(new org.reactivestreams.Subscription() {
            @Override
            public void request(long n) {
                if (closed.get()) {
                    s.onError(new IllegalStateException("Publisher is closed"));
                    return;
                }

                if (n <= 0) {
                    s.onError(new IllegalArgumentException("Demand must be positive"));
                    return;
                }

                demand.addAndGet(n);
                executor.submit(() -> {
                    try {
                        while (demand.get() > 0 && !closed.get()) {
                            ByteBuffer audioBuffer = readNextChunk();
                            if (audioBuffer.remaining() > 0) {
                                AudioEvent audioEvent = AudioEvent.builder()
                                        .audioChunk(SdkBytes.fromByteBuffer(audioBuffer))
                                        .build();
                                s.onNext(audioEvent);
                                demand.decrementAndGet();
                            } else {
                                s.onComplete();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        s.onError(e);
                    }
                });
            }

            @Override
            public void cancel() {
                close();
            }
        });
    }

    private ByteBuffer readNextChunk() throws IOException {
        if (closed.get()) {
            throw new IOException("Stream is closed");
        }

        byte[] audioChunk = new byte[CHUNK_SIZE_IN_BYTES];
        int len;
        try {
            len = inputStream.read(audioChunk);
        } catch (IOException e) {
            close();
            throw e;
        }

        if (len <= 0) {
            return ByteBuffer.allocate(0);
        }
        return ByteBuffer.wrap(audioChunk, 0, len);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Log error but continue with cleanup
            }
            
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}