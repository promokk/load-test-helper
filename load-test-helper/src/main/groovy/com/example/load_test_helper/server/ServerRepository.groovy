package com.example.load_test_helper.server


import org.springframework.data.repository.CrudRepository

interface ServerRepository extends CrudRepository<Server, String> {
    Iterable<Server> findByFree(Boolean free)
}
