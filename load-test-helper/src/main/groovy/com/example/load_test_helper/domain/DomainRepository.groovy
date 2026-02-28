package com.example.load_test_helper.domain

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DomainRepository extends CrudRepository<Domain, Integer> {}
