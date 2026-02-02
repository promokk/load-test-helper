package com.example.load_test_helper.domain

import org.springframework.data.repository.CrudRepository

interface DomainRepository extends CrudRepository<Domain, String> {}
