package com.example.load_test_helper.scenario

import org.springframework.stereotype.Service

@Service
class ScenarioService {
    private final ScenarioRepository scenarioRepository

    ScenarioService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository
    }

    // Удаление всех черновых сценариев
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
