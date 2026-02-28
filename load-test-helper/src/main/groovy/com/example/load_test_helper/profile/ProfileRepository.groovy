package com.example.load_test_helper.profile

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository extends CrudRepository<Profile, String> {}
