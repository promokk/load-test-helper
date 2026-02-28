package com.example.load_test_helper.stand

import com.example.load_test_helper.domain.Domain
import com.example.load_test_helper.domain.DomainDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class StandService {
    def logger = LoggerFactory.getLogger(getClass())
    private final StandRepository standRepository

    StandService(StandRepository standRepository) {
        this.standRepository = standRepository
    }

    // Добавить стенд
    @Transactional()
    def addDomain(StandDTO standDTO) {
        def stand = new Stand(
                name: standDTO.name
        )
        for (domainDTO in standDTO.domains)  {
            def existingStand = stand.domains.find {it.name == domainDTO.name}
            if (existingStand) {
                logger.warn(
                        "DomainService: addStand; status: WARN; message: Домен ${domainDTO.name} существует в ${stand.name}")
                continue
            }
            def domain = new Domain(
                    stand: stand,
                    name: domainDTO.name,
                    url: domainDTO.url
            )
            stand.domains.add(domain)
        }
        return standRepository.save(stand)
    }

    // Добавить / Редактировать домен
    @Transactional()
    def addDomain(DomainDTO domainDTO, Stand stand) {
        def domian = standRepository.findStandByStandNameAndDomainName(stand.name, domainDTO.name) ?: null
        if (domian) {
            domian = domian.get()
            domian.url = domainDTO.url
            return standRepository.save(stand)
        }
        domian = new Domain(
                stand: stand,
                name: domainDTO.name,
                url: domainDTO.url
        )
        stand.domains.add(domian)
        return standRepository.save(stand)
    }
}
