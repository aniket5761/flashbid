package com.example.flashbid.common.exception;

public class BidAccessDeniedException extends RuntimeException {
    public BidAccessDeniedException(String message) {
        super(message);
    }
}
