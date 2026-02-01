package com.example.load_test_helper.scenario

import com.example.load_test_helper.group.Group
import org.springframework.stereotype.Service

@Service
class ScenarioService {
    private final ScenarioRepository scenarioRepository

    ScenarioService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository
    }

    // Добавить сценарий
    def addScenario(ScenarioDTO scenarioDto) {
        def scenario = new Scenario(
                name: scenarioDto.name,
                stand: scenarioDto.stand,
                url: scenarioDto.url,
                duration: scenarioDto.duration,
                draft: scenarioDto.draft
        )
        scenarioDto.group.each { groupDto ->
            def group = new Group(
                    scenario: scenario,
                    profile: groupDto.profile,
                    server: groupDto.server,
                    url: groupDto.url ?: null,
                    duration: groupDto.duration ?: null
            )
            scenario.group.add(group)
        }
        return scenarioRepository.save(scenario)
    }

    // Удалить все черновые сценарии
    def deleteDraftAll() {
        Iterable<Scenario> scenarios = scenarioRepository.findByDraft(true)
        def scenarioDelArr = []
        for (scenario in scenarios) {
            scenarioRepository.deleteById(scenario.name)
            scenarioDelArr.add(scenario.name)
        }
        return scenarioDelArr
    }
}
