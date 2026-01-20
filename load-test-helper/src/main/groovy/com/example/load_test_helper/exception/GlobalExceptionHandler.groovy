package com.example.load_test_helper.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {
    def logger = LoggerFactory.getLogger(getClass())

    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    def handlerNotFound(NotFoundException ex) {
        logger.error("${ex.method} ${ex.requestURI}; message: ${ex.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND}; message: ${ex.message}")
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    def handlerBadRequest(BadRequestException ex) {
        logger.error("${ex.method} ${ex.requestURI}; message: ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: ${ex.message}")
    }
}
