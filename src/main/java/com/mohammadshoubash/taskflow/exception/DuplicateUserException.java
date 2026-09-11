package com.mohammadshoubash.taskflow.exception;

public class DuplicateUserException extends TaskFlowException {
    public DuplicateUserException(String email) {
        super("A user with the email address '" + email + "' already exists.");
    }
}
