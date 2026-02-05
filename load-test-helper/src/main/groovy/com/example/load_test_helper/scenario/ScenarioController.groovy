package com.example.load_test_helper.scenario

import com.example.load_test_helper.exception.BadRequestException
import com.example.load_test_helper.exception.NotFoundException
import com.example.load_test_helper.group.Group
import com.example.load_test_helper.group.GroupDTO
import com.example.load_test_helper.group.GroupRepository
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/scenario")
class ScenarioController {
    def logger = LoggerFactory.getLogger(getClass())
    private final ScenarioRepository scenarioRepository
    private final GroupRepository groupRepository
    private final ScenarioService scenarioService

    ScenarioController(ScenarioRepository scenarioRepository, ScenarioService scenarioService, GroupRepository groupRepository) {
        this.scenarioRepository = scenarioRepository
        this.groupRepository = groupRepository
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
        def scenario = scenarioRepository.findById(name) ?: false
        if (!scenario)
            throw new NotFoundException("Сценарий не найден - ${name}")
        return scenario
    }

    // Добавить сценарий
    @PostMapping("/add")
    def addScenario(@Valid @RequestBody ScenarioDTO scenarioDto, HttpServletRequest request) {
        Scenario scenario = scenarioService.addScenario(scenarioDto)
        logger.info("${request.method} ${request.requestURI}; message: Сценарий добавлен - ${scenario.name}")
        return scenario
    }

    // Добавить группу
    @PostMapping("/{name}/add/group")
    def addGroup(@Valid @RequestBody GroupDTO groupDTO, @PathVariable("name") String name, HttpServletRequest request) {
        def scenario = scenarioRepository.findById(name) ?: null
        if (!scenario)
            throw new NotFoundException("Сценарий не найден - ${name}")
        scenario = scenarioService.addGroup(groupDTO, scenario.get())
        logger.info("${request.method} ${request.requestURI}; message: Группа ${scenario.groups.last().id} добавлена в ${scenario.name}")
        return scenario
    }

    // Редактировать группу
    @PutMapping("/{name}/group/{groupId}")
    def editGroup(@Valid @RequestBody GroupDTO groupDTO, @PathVariable("name") String name, @PathVariable("groupId") String groupId, HttpServletRequest request) {
        if (!groupId.isInteger())
            throw new BadRequestException("Неверный запрос. Path-параметр groupId != Integer")
        if (!scenarioRepository.findById(name))
            throw new NotFoundException("Сценарий не найден - ${name}")
        def group = scenarioRepository.findGroupByScenarioNameAndGroupId(name, groupId.toInteger()) ?: null
        if (!group)
            throw new NotFoundException("Группа ${groupId} для сценария ${name} не найдена")
        group = scenarioService.editGroup(groupDTO, group.get())
        logger.info("${request.method} ${request.requestURI}; message: Группа ${groupId} отредактирована")
        return group
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
        Iterable<Scenario> scenarios = scenarioRepository.findByDraft(true)
        scenarioRepository.deleteAll(scenarios)
        logger.info("${request.method} ${request.requestURI}; message: Удалено ${scenarios.size()}: ${scenarios*.name}")
        return ResponseEntity.status(HttpStatus.OK).body("message: Удалено ${scenarios.size()}: ${scenarios*.name}")
    }

    // Удалить группу
    @DeleteMapping("/{name}/group/{groupId}")
    def deleteGroup(@PathVariable("name") String name, @PathVariable("groupId") String groupId, HttpServletRequest request) {
        if (!groupId.isInteger())
            throw new BadRequestException("Неверный запрос. Path-параметр groupId != Integer")
        Optional<Group> group = scenarioRepository.findGroupByScenarioNameAndGroupId(name, groupId.toInteger()) ?: null
        if (!group)
            throw new NotFoundException("Группа ${groupId} для сценария ${name} не найдена")
        groupRepository.deleteById(group.get().id)
        logger.info("${request.method} ${request.requestURI}; message: Группа удалена - ${groupId}")
        return ResponseEntity.noContent().build()
    }
}
