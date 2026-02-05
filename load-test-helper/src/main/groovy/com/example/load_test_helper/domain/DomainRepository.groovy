package com.example.load_test_helper.domain

import com.example.load_test_helper.stand.Stand
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface DomainRepository extends CrudRepository<Domain, String> {
    @Query('SELECT s FROM Domain d JOIN d.stands s WHERE d.name = :domainName AND s.name = :standName')
    Optional<Stand> findStandByDomainNameAndStandName(@Param('domainName') String domainName, @Param('standName') String standName)
}
