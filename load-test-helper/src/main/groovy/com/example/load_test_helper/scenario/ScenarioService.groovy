package com.example.load_test_helper.scenario

import com.example.load_test_helper.group.Group
import com.example.load_test_helper.group.GroupDTO
import com.example.load_test_helper.group.GroupRepository
import org.springframework.stereotype.Service

@Service
class ScenarioService {
    private final ScenarioRepository scenarioRepository
    private final GroupRepository groupRepository

    ScenarioService(ScenarioRepository scenarioRepository, GroupRepository groupRepository) {
        this.scenarioRepository = scenarioRepository
        this.groupRepository = groupRepository
    }

    // Добавить сценарий
    def addScenario(ScenarioDTO scenarioDto) {
        def scenario = new Scenario(
                name: scenarioDto.name,
                stand: scenarioDto.stand,
                domain: scenarioDto.domain,
                duration: scenarioDto.duration,
                draft: scenarioDto.draft
        )
        scenarioDto.groups.each { groupDto ->
            def group = new Group(
                    scenario: scenario,
                    profile: groupDto.profile,
                    server: groupDto.server,
                    domain: groupDto.domain ?: null,
                    duration: groupDto.duration ?: null,
                    customParam: groupDto.customParam ?: null,
            )
            scenario.groups.add(group)
        }
        return scenarioRepository.save(scenario)
    }

    // Добавить группу
    def addGroup(GroupDTO groupDTO, Scenario scenario) {
        Group group = new Group(
                scenario: scenario,
                profile: groupDTO.profile,
                server: groupDTO.server,
                domain: groupDTO.domain,
                duration: groupDTO.duration,
                customParam: groupDTO.customParam ?: null,
        )
        scenario.groups.add(group)
        return scenarioRepository.save(scenario)
    }

    // Редактировать группу
    def editGroup(GroupDTO groupDTO ,Group group) {
        group.profile = groupDTO.profile
        group.server = groupDTO.server
        group.domain = groupDTO.domain
        group.duration = groupDTO.duration
        group.customParam = groupDTO.customParam
        return groupRepository.save(group)
    }
}
