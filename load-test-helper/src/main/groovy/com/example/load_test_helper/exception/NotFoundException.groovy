package com.example.load_test_helper.exception

class NotFoundException extends RuntimeException {
    String method
    String requestURI

    NotFoundException(String message) {
        super(message)
    }

    NotFoundException(String message, String method, String requestURI) {
        super(message)
        this.method = method
        this.requestURI = requestURI
    }
}
