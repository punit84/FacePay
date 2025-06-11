package com.punit.AWSPe.nova.utility;

public interface InteractObserver<T> {

    void onNext(T msg);

    void onComplete();

    //TODO: Create a new class for Error Status
    void onError(Exception error);

    /**
     * Sets the input observer for bidirectional communication
     * @param inputObserver The input observer to set
     */
    default void setInputObserver(InteractObserver<T> inputObserver) {
        // Default empty implementation
    }
}

