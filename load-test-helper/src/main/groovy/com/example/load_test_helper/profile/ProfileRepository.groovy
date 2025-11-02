package com.example.load_test_helper.profile


import org.springframework.data.repository.CrudRepository

interface ProfileRepository extends CrudRepository<Profile, String> {}
