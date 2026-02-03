package com.example.load_test_helper.domain

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface DomainRepository extends CrudRepository<Domain, String> {
    @Query('SELECT s.url FROM Domain d JOIN d.stands s WHERE d.name = :domainName AND s.name = :standName')
    String findByDomainAndStand(@Param('domainName') String domainName, @Param('standName') String standName)
}
