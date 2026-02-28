package com.example.load_test_helper.stand

import com.example.load_test_helper.exception.NotFoundException
import com.example.load_test_helper.domain.Domain
import com.example.load_test_helper.domain.DomainDTO
import com.example.load_test_helper.domain.DomainRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/stand")
class StandController {
    def logger = LoggerFactory.getLogger(getClass())
    private final StandRepository standRepository
    private final DomainRepository domainRepository
    private final StandService standService

    StandController(StandRepository standRepository, DomainRepository domainRepository, StandService standService) {
        this.standRepository = standRepository
        this.domainRepository = domainRepository
        this.standService = standService
    }

    // Список стендов
    @GetMapping
    def getStands() {
        return standRepository.findAll()
    }

    // Поиск стенда по name
    @GetMapping("/{name}")
    def getStandById(@PathVariable("name") String name) {
        def domain = standRepository.findById(name) ?: false
        if (!domain)
            throw new NotFoundException("Стенд не найден - ${name}")
        return domain
    }

    // Поиск url по standName и domainName
    @GetMapping("/get/url")
    def getUrlByStandAndDomain(@RequestParam("standName") String standName, @RequestParam("domainName") String domainName) {
        Optional<Domain> domain = standRepository.findStandByStandNameAndDomainName(standName, domainName) ?: null
        if (!domain)
            throw new NotFoundException("Url для стенда ${standName} и домена ${domainName} отсутствует")
        return domain.get().url
    }

    // Добавить стенд
    @PostMapping("/add")
    def addStand(@Valid @RequestBody StandDTO standDTO, HttpServletRequest request) {
        Stand stand = standService.addDomain(standDTO)
        logger.info("${request.method} ${request.requestURI}; message: Стенд добавлен - ${stand.name}")
        return stand
    }

    // Добавить / Редактировать домен
    @PostMapping("/{name}/add/domain")
    def addDomain(@Valid @RequestBody DomainDTO domainDTO, @PathVariable("name") String name, HttpServletRequest request) {
        def stand = standRepository.findById(name) ?: null
        if (!stand)
            throw new NotFoundException("Стенд не найден - ${name}")
        stand = standService.addDomain(domainDTO, stand.get())
        logger.info("${request.method} ${request.requestURI}; message: Домен ${domainDTO.name} добавлен в ${stand.name}")
        return stand
    }

    // Удалить стенд
    @DeleteMapping("/{name}")
    def deleteStand(@PathVariable("name") String name, HttpServletRequest request) {
        if (!standRepository.findById(name))
            throw new NotFoundException("Стенд не найден - ${name}")
        standRepository.deleteById(name)
        logger.info("${request.method} ${request.requestURI}; message: Стенд удален - ${name}")
        return ResponseEntity.noContent().build()
    }

    // Удалить домен
    @DeleteMapping("/{name}/domain/{domainName}")
    def deleteDomain(@PathVariable("name") String standName, @PathVariable("domainName") String domainName, HttpServletRequest request) {
        Optional<Domain> domain = standRepository.findStandByStandNameAndDomainName(standName, domainName) ?: null
        if (!domain)
            throw new NotFoundException("Домен ${domainName} для стенда ${standName} не найден")
        domainRepository.deleteById(domain.get().id)
        logger.info("${request.method} ${request.requestURI}; message: Домен удален - ${standName}")
        return ResponseEntity.noContent().build()
    }
}
