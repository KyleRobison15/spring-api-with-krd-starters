package com.codewithmosh.store.users;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("A user with this email already exists.");
    }

    public DuplicateUserException(String message) {
        super(message);
    }
}
