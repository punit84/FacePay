package com.punit.AWSPe.nova.observer;

/**
 * Asynchronous observer for message stream interactions with Amazon Nova Sonic.
 * @param <T> The type of message.
 */
public interface InteractObserver<T> {
    void onNext(T msg);
    void onComplete();
    void onError(Exception error);
}