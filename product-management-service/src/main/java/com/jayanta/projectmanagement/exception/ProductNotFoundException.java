package com.jayanta.projectmanagement.exception;

public class ProductNotFoundException extends RuntimeException {
    private final String code;
    private final String field;

    public ProductNotFoundException(String message) {
        super(message);
        this.code = "PRODUCT_NOT_FOUND";
        this.field = "id";
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}
