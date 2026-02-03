package com.example.load_test_helper.domain

import com.example.load_test_helper.stand.Stand
import com.example.load_test_helper.stand.StandDTO
import org.springframework.stereotype.Service

@Service
class DomainService {
    private final DomainRepository domainRepository

    DomainService(DomainRepository domainRepository) {
        this.domainRepository = domainRepository
    }

    // Добавить домен
    def addDomain(DomainDTO domainDTO) {
        def domain = new Domain(
                name: domainDTO.name
        )
        domainDTO.stands.each { standDTO ->
            def stand = new Stand(
                    domain: domain,
                    name: standDTO.name,
                    url: standDTO.url
            )
            domain.stands.add(stand)
        }
        return domainRepository.save(domain)
    }

    // Добавить стенд в домен
    def addStandToDomain(StandDTO standDTO, Domain domain) {
        def stand = domain.stands.find {it.name == standDTO.name}
        if (stand) {
            stand.name = standDTO.name
            stand.url = standDTO.url
            return domainRepository.save(domain)
        }
        stand = new Stand(
                domain: domain,
                name: standDTO.name,
                url: standDTO.url
        )
        domain.stands.add(stand)
        return domainRepository.save(domain)
    }
}
