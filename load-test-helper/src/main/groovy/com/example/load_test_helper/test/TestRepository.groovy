package com.example.load_test_helper.test

import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface TestRepository extends CrudRepository<Test, Integer> {
    Iterable<Test> findAllByEndedAtBefore(LocalDateTime dateAfter)
}
