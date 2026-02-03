package com.example.load_test_helper.domain

import com.example.load_test_helper.exception.NotFoundException
import com.example.load_test_helper.stand.StandDTO
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
@RequestMapping("/domain")
class DomainController {
    def logger = LoggerFactory.getLogger(getClass())
    private final DomainRepository domainRepository
    private final DomainService domainService

    DomainController(DomainRepository domainRepository, DomainService domainService) {
        this.domainRepository = domainRepository
        this.domainService = domainService
    }

    // Список доменов
    @GetMapping
    def getScenarios() {
        return domainRepository.findAll()
    }

    // Поиск домена по name
    @GetMapping("/{name}")
    def getDomainById(@PathVariable("name") String name) {
        def domain = domainRepository.findById(name) ?: false
        if (!domain)
            throw new NotFoundException("Домен не найден - ${name}")
        return domain
    }

    // Поиск url (domain full) по domainName и standName
    @GetMapping("/full")
    def getUrlByDomainAndStand(@RequestParam("domainName") String domainName, @RequestParam("standName") String standName) {
        def domainFull = domainRepository.findByDomainAndStand(domainName, standName) ?: false
        if (!domainFull)
            throw new NotFoundException("Полное имя домена ${domainName} для стенда ${standName} не найдено")
        return domainFull
    }

    // Добавить домен
    @PostMapping("/add")
    def addDomain(@Valid @RequestBody DomainDTO domainDTO, HttpServletRequest request) {
        Domain domain = domainService.addDomain(domainDTO)
        logger.info("${request.method} ${request.requestURI}; message: Домен добавлен - ${domain.name}")
        return domain
    }

    // Добавить стенд в домен
    @PostMapping("/{name}/add/stand")
    def addStandToDomain(@Valid @RequestBody StandDTO standDTO, @PathVariable("name") String name, HttpServletRequest request) {
        def domain = domainRepository.findById(name) ?: false
        if (!domain)
            throw new NotFoundException("Домен не найден - ${name}")
        domain = domainService.addStandToDomain(standDTO, domain.get())
        logger.info("${request.method} ${request.requestURI}; message: Стенд ${standDTO.name} добавлен в ${domain.name}")
        return domain
    }

    // Удалить домен
    @DeleteMapping("/{name}")
    def deleteDomain(@PathVariable("name") String name, HttpServletRequest request) {
        if (!domainRepository.findById(name))
            throw new NotFoundException("Домен не найден - ${name}")
        domainRepository.deleteById(name)
        logger.info("${request.method} ${request.requestURI}; message: Домен удален - ${name}")
        return ResponseEntity.noContent().build()
    }
}
