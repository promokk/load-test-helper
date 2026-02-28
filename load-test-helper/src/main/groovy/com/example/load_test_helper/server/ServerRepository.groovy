package com.example.load_test_helper.server

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ServerRepository extends CrudRepository<Server, String> {
    Iterable<Server> findByFree(Boolean free)
}
