package com.example.load_test_helper.test

import com.example.load_test_helper.exception.NotFoundException
import com.example.load_test_helper.server.ServerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/test")
class TestController {
    def logger = LoggerFactory.getLogger(getClass())
    private final TestRepository testRepository
    private final TestService testService
    private final ServerService serverService

    TestController(TestRepository testRepository, TestService testService, ServerService serverService) {
        this.testRepository = testRepository
        this.testService = testService
        this.serverService = serverService
    }

    // Список тестов
    @GetMapping("/info")
    def getStartups() {
        return testRepository.findAll()
    }

    // Поиск теста по id
    @GetMapping("/info/{id}")
    def getTestById(@PathVariable("id") Integer id) {
        if (!testRepository.findById(id))
            throw new NotFoundException("Тест не найден - ${id}")
        return testRepository.findById(id)
    }

    // Создать тест
    @PostMapping("/create")
    def postCreateTest(@Valid @RequestBody TestDTO test, HttpServletRequest request) {
        Test newTest = testService.createTest(test)
        logger.info("${request.method} ${request.requestURI}; message: Тест создан - ${newTest.id}")
        return newTest.id
    }

    // Удалить тест
    @DeleteMapping("/delete/{id}")
    def deleteTest(@PathVariable("id") Integer id, HttpServletRequest request) {
        if (!testRepository.findById(id))
            throw new NotFoundException("Тест не найден - ${id}")
        testService.deleteTest(id)
        logger.info("${request.method} ${request.requestURI}; message: Тест удален - ${id}")
        return ResponseEntity.noContent().build()
    }

    // Удалить все тесты
    @DeleteMapping("/delete/all")
    def deleteTestAll(HttpServletRequest request) {
        serverService.serverBookingCancelAll()
        testRepository.deleteAll()
        logger.info("${request.method} ${request.requestURI}; message: Все тесты удалены")
        return ResponseEntity.noContent().build()
    }
}
