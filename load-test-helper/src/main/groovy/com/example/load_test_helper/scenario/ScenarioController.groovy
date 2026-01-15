package com.example.load_test_helper.scenario

import com.example.load_test_helper.group.GroupRepository
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/scenario")
class ScenarioController {
    def logger = LoggerFactory.getLogger(getClass())
    private final ScenarioRepository scenarioRepository
    private final GroupRepository groupRepository

    ScenarioController(ScenarioRepository scenarioRepository, GroupRepository groupRepository) {
        this.scenarioRepository = scenarioRepository
        this.groupRepository = groupRepository
    }

    // Список сценариев
    @GetMapping
    def getScenarios(HttpServletRequest request) {
        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
        return scenarioRepository.findAll()
    }

    // Добавить профиль
    @PostMapping("/add")
    def addScenario(@RequestBody Scenario scenario, HttpServletRequest request) {
//        logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Сценарий добавлен - ${scenario.name}")
//        for (g in scenario.group) {
//            groupRepository.save(g)
//        }
//        scenario.group[0].scenario = scenario
        return scenarioRepository.save(scenario)


//        try {
//            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Сценарий добавлен - ${scenario.name}")
//            for (g in scenario.group) {
//                groupRepository.save(g)
//            }
//            return scenarioRepository.save(scenario)
//        } catch (ex) {
//            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
//        }
    }
}
