package com.example.load_test_helper.scenario

import com.example.load_test_helper.exception.NotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
    def getScenarios() {
        return scenarioRepository.findAll()
    }

    // Поиск сценария по name
    @GetMapping("/{name}")
    def getScenarioById(@PathVariable("name") String name) {
        if (!scenarioRepository.findById(name))
            throw new NotFoundException("Сценарий не найден - ${name}")
        return scenarioRepository.findById(name)
    }

    // Добавить сценарий
    @PostMapping("/add")
    def addScenario(@Valid @RequestBody ScenarioDTO scenarioDto, HttpServletRequest request) {
        Scenario scenario = scenarioService.addScenario(scenarioDto)
        logger.info("${request.method} ${request.requestURI}; message: Сценарий добавлен - ${scenario.name}")
        return scenario
    }

    // Удалить сценарий
    @DeleteMapping("/{name}")
    def deleteScenario(@PathVariable("name") String name, HttpServletRequest request) {
        if (!scenarioRepository.findById(name))
            throw new NotFoundException("Сценарий не найден - ${name}")
        scenarioRepository.deleteById(name)
        logger.info("${request.method} ${request.requestURI}; message: Сценарий удален - ${name}")
        return ResponseEntity.noContent().build()
    }

    // Удалить все черновые сценарии
    @DeleteMapping("/draft/deleteAll")
    def deleteDraftAll(HttpServletRequest request) {
        List<String> scenarioDelArr = scenarioService.deleteDraftAll()
        logger.info("${request.method} ${request.requestURI}; message: Удалено ${scenarioDelArr.size()}: ${scenarioDelArr}")
        return ResponseEntity.status(HttpStatus.OK).body("message: Удалено ${scenarioDelArr.size()}: ${scenarioDelArr}")
    }
}
