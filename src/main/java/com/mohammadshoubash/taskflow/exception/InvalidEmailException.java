package com.mohammadshoubash.taskflow.exception;

public class InvalidEmailException extends TaskFlowException {
    public InvalidEmailException(String email) {
        super("The email address '" + email + "' is not valid.");
    }
}
