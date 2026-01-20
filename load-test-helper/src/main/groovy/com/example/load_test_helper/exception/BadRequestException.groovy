package com.example.load_test_helper.exception

class BadRequestException extends RuntimeException {
    String method
    String requestURI

    BadRequestException(String message) {
        super(message)
    }

    BadRequestException(String message, String method, String requestURI) {
        super(message)
        this.method = method
        this.requestURI = requestURI
    }
}
