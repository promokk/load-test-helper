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
    def addScenario(ScenarioDTO scenarioDTO) {
        def scenario = new Scenario(
                name: scenarioDTO.name,
                stand: scenarioDTO.stand ?: null,
                domain: scenarioDTO.domain,
                duration: scenarioDTO.duration,
                draft: scenarioDTO.draft
        )
        scenarioDTO.groups.each { groupDTO ->
            def group = new Group(
                    scenario: scenario,
                    profile: groupDTO.profile,
                    server: groupDTO.server,
                    masterRun: groupDTO.masterRun ?: true,
                    domain: groupDTO.domain ?: null,
                    duration: groupDTO.duration ?: null,
                    certificate: groupDTO.certificate ?: null,
                    testParam: groupDTO.testParam ?: null,
                    serverParam: groupDTO.serverParam ?: null
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
                certificate: groupDTO.certificate ?: null,
                testParam: groupDTO.testParam ?: null,
                serverParam: groupDTO.serverParam ?: null
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
        group.certificate = groupDTO.certificate
        group.testParam = groupDTO.testParam
        group.serverParam = groupDTO.serverParam
        return groupRepository.save(group)
    }
}
