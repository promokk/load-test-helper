package com.example.load_test_helper.exception

class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message)
    }
}
