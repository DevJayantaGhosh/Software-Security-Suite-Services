package com.jayanta.projectmanagement.exception;


public class RepoNotFoundException extends RuntimeException {
    public RepoNotFoundException(String message) { super(message); }
}