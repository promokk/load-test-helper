package com.example.load_test_helper.scenario

import com.example.load_test_helper.exception.BadRequestException
import com.example.load_test_helper.exception.NotFoundException
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
        return scenarioRepository.findAll()
    }

    // Поиск сценария по name
    @GetMapping("/{name}")
    def getTestById(@PathVariable("name") String name, HttpServletRequest request) {
        if (!scenarioRepository.findById(name))
            throw new NotFoundException("Сценарий не найден - ${name}", request.method, request.requestURI)
        return scenarioRepository.findById(name)
    }

    // Добавить профиль
    @PostMapping("/add")
    def addScenario(@RequestBody Scenario scenario, HttpServletRequest request) {
        if (scenario.name == null || scenario.stand == null || scenario.draft == null || scenario.group == null)
            throw new BadRequestException("Неверное тело запроса. Обязательные поля: name, stand, draft, group",
                    request.method, request.requestURI)
        if (scenario.group.any{it.profile == null || it.server == null || it.url == null})
            throw new BadRequestException("Неверное тело запроса. Обязательные поля group: profile, server, url",
                    request.method, request.requestURI)
        def newScenario = scenarioRepository.save(scenario)
        logger.info("${request.method} ${request.requestURI}; message: Сценарий добавлен - ${scenario.name}")
        return newScenario
    }

    // Удалить сценарий
    @DeleteMapping("/{name}")
    def deleteScenario(@PathVariable("name") String name, HttpServletRequest request) {
        if (!scenarioRepository.findById(name))
            throw new NotFoundException("Сценарий не найден - ${name}", request.method, request.requestURI)
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
