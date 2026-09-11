package com.mohammadshoubash.taskflow.exception;

public class InvalidTaskStateException extends TaskFlowException {
    public InvalidTaskStateException(int taskId, String currentStatus, String targetStatus) {
        super("Task with ID '" + taskId + "' cannot be moved from status '" + currentStatus + "' to '" + targetStatus + "'.");
    }
}
