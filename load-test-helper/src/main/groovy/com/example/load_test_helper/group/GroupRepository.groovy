package com.example.load_test_helper.group

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository extends CrudRepository<Group, Integer> {}
