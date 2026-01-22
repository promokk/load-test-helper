package com.example.load_test_helper.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {
    def logger = LoggerFactory.getLogger(getClass())

    @ExceptionHandler(MethodArgumentNotValidException)
    def handleValidationArgument(MethodArgumentNotValidException ex, HttpServletRequest request) {
        def result = ex.bindingResult
        def errors = result.fieldErrors.collectEntries { FieldError err ->
            [(err.field): err.defaultMessage]
        }
        logger.error("${request.method} ${request.requestURI}; message: ${errors}")
        new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingServletRequestParameterException)
    def handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        def parameterName = ex.parameterName
        logger.error("${request.method} ${request.requestURI}; message: Отсутствует параметр '${parameterName}'")
        new ResponseEntity<>("Отсутствует get-параметр: ${parameterName}", HttpStatus.BAD_REQUEST)
    }

    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    def handlerNotFound(NotFoundException ex, HttpServletRequest request) {
        logger.error("${request.method} ${request.requestURI}; message: ${ex.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND}; message: ${ex.message}")
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    def handlerBadRequest(BadRequestException ex, HttpServletRequest request) {
        logger.error("${request.method} ${request.requestURI}; message: ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: ${ex.message}")
    }
}
