package com.jayanta.projectmanagement.exception;


public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(String message) { super(message); }
}