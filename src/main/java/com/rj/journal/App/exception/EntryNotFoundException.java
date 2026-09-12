package com.rj.journal.App.exception;

/** Thrown when an entry doesn't exist OR doesn't belong to the requesting user - same message/status either way, so a caller can't tell the two cases apart. */
public class EntryNotFoundException extends RuntimeException {
    public EntryNotFoundException(String message) {
        super(message);
    }
}
