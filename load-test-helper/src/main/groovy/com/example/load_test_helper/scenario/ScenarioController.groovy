package com.example.load_test_helper.scenario

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
@RequestMapping("/scenario")
class ScenarioController {
    def logger = LoggerFactory.getLogger(getClass())
    private final ScenarioRepository scenarioRepository
    private final ScenarioService scenarioService

    ScenarioController(ScenarioRepository scenarioRepository, ScenarioService scenarioService) {
        this.scenarioRepository = scenarioRepository
        this.scenarioService = scenarioService
    }

    // Список сценариев
    @GetMapping
    def getScenarios(HttpServletRequest request) {
        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
        return scenarioRepository.findAll()
    }

    // Поиск сценария по name
    @GetMapping("/{name}")
    def getTestById(@PathVariable("name") String name, HttpServletRequest request) {
        if (scenarioRepository.findById(name)) {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
            return scenarioRepository.findById(name)
        } else {
            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Сценарий не найден - ${name}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Сценарий не найден - ${name}")
        }
    }

    // Добавить профиль
    @PostMapping("/add")
    def addScenario(@RequestBody Scenario scenario, HttpServletRequest request) {
        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Сценарий добавлен - ${scenario.name}")
        return scenarioRepository.save(scenario)
    }

    // Удалить сценарий
    @DeleteMapping("/{name}")
    def deleteScenario(@PathVariable("name") String name, HttpServletRequest request) {
        if (scenarioRepository.findById(name)) {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Сценарий удален - ${name}")
            scenarioRepository.deleteById(name)
            return ResponseEntity.noContent().build()
        } else {
            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Сценарий не найден - ${name}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Сценарий не найден - ${name}")
        }
    }

    // Удалить все черновые сценарии
    @DeleteMapping("/draft/deleteAll")
    def deleteDraftAll(HttpServletRequest request) {
        try {
            List<String> scenarioDelArr = scenarioService.deleteDraftAll()
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Удалено ${scenarioDelArr.size()}: ${scenarioDelArr}")
            return ResponseEntity.status(HttpStatus.OK).body("message: Удалено ${scenarioDelArr.size()}: ${scenarioDelArr}")
        } catch (ex) {
            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }
}
