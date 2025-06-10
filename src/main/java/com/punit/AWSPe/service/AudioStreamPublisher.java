package com.punit.AWSPe.service;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.transcribestreaming.model.AudioEvent;
import software.amazon.awssdk.services.transcribestreaming.model.AudioStream;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AudioStreamPublisher implements Publisher<AudioStream>, AutoCloseable {
    private final ExecutorService executor;
    private static final int CHUNK_SIZE_IN_BYTES = 1024 * 1;
    private final AtomicLong demand = new AtomicLong(0);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final LinkedBlockingQueue<ByteBuffer> audioQueue;
    private Subscriber<? super AudioStream> subscriber;

    public AudioStreamPublisher() {
        this.executor = Executors.newFixedThreadPool(1);
        this.audioQueue = new LinkedBlockingQueue<>();
    }

    public AudioStreamPublisher(java.io.InputStream inputStream) {
        this();
        // Start a thread to read from the input stream
        executor.submit(() -> {
            try {
                byte[] buffer = new byte[CHUNK_SIZE_IN_BYTES];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1 && !closed.get()) {
                    if (bytesRead > 0) {
                        ByteBuffer audioBuffer = ByteBuffer.allocate(bytesRead);
                        audioBuffer.put(buffer, 0, bytesRead);
                        audioBuffer.flip();
                        addAudioChunk(audioBuffer);
                    }
                }
            } catch (Exception e) {
                if (subscriber != null) {
                    subscriber.onError(e);
                }
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
        });
    }

    public void addAudioChunk(ByteBuffer audioData) {
        if (!closed.get()) {
            audioQueue.offer(audioData.duplicate());
            if (subscriber != null && demand.get() > 0) {
                processNextChunk();
            }
        }
    }

    @Override
    public void subscribe(Subscriber<? super AudioStream> s) {
        if (closed.get()) {
            s.onError(new IllegalStateException("Publisher is closed"));
            return;
        }

        this.subscriber = s;
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
                processNextChunk();
            }

            @Override
            public void cancel() {
                close();
            }
        });
    }

    private void processNextChunk() {
        executor.submit(() -> {
            try {
                while (demand.get() > 0 && !closed.get()) {
                    ByteBuffer audioBuffer = audioQueue.poll();
                    if (audioBuffer != null && audioBuffer.remaining() > 0) {
                        AudioEvent audioEvent = AudioEvent.builder()
                                .audioChunk(SdkBytes.fromByteBuffer(audioBuffer))
                                .build();
                        subscriber.onNext(audioEvent);
                        demand.decrementAndGet();
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            audioQueue.clear();
            if (subscriber != null) {
                subscriber.onComplete();
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