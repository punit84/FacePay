package com.punit.AWSPe.nova.utility;

public interface InteractObserver<T> {

    void onNext(T msg);

    void onComplete();

    //TODO: Create a new class for Error Status
    void onError(Exception error);

}

