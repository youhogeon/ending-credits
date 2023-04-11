package com.youhogeon.credits.receiver;

@FunctionalInterface
public interface MessageHandler<T> {
    
    void handle(T message);

}
