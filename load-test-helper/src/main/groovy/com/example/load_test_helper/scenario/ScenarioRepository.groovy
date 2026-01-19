package com.example.load_test_helper.scenario

import com.example.load_test_helper.server.Server
import org.springframework.data.repository.CrudRepository

interface ScenarioRepository extends CrudRepository<Scenario, String> {
    Iterable<Scenario> findByDraft(Boolean free)
}
