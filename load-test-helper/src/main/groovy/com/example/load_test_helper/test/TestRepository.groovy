package com.example.load_test_helper.test

import org.springframework.data.repository.CrudRepository

interface TestRepository extends CrudRepository<Test, Integer> {}
