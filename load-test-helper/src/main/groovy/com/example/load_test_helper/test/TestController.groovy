package com.example.load_test_helper.test

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController {
    def logger = LoggerFactory.getLogger(getClass())
    private final TestRepository testRepository
    private final TestService testService

    TestController(TestRepository testRepository, TestService testService) {
        this.testRepository = testRepository
        this.testService = testService
    }

    // Список тестов
    @GetMapping("/info")
    def getStartups(HttpServletRequest request) {
        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
        return testRepository.findAll()
    }

    // Поиск теста по id
    @GetMapping("/info/{id}")
    def getTestById(@PathVariable("id") Integer id, HttpServletRequest request) {
        if (testRepository.findById(id)) {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
            return testRepository.findById(id)
        } else {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Тест не найден - ${id}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Тест не найден - ${id}")
        }
    }

    // Создать тест
    @PostMapping("/create")
    def postCreateTest(@RequestBody Object test, HttpServletRequest request) {
        Test newTest = testService.createTest(test)
        if (newTest instanceof Exception) {
            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Некорректный запрос / Тест не найден; stackTrace: ${newTest.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST }; message: Некорректный запрос / Тест не найден")
        }
        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Тест создан - ${newTest.id}")
        return newTest.id
    }

    // Удалить тест
    @DeleteMapping("/delete/{id}")
    def deleteTest(@PathVariable("id") Integer id, HttpServletRequest request) {
        if (testRepository.findById(id)) {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Тест удален - ${id}")
            testRepository.deleteById(id)
            return ResponseEntity.noContent().build()
        } else {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Тест не найден - ${id}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Тест не найден - ${id}")
        }
    }

    // Удалить все тесты
    @DeleteMapping("/delete/all")
    def deleteTestAll(HttpServletRequest request) {
        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Все тесты удалены")
        testRepository.deleteAll()
        return ResponseEntity.noContent().build()
    }
}
