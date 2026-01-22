package com.example.load_test_helper.profile

import com.example.load_test_helper.exception.BadRequestException
import com.example.load_test_helper.exception.NotFoundException
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
@RequestMapping("/profile")
class ProfileController {
    def logger = LoggerFactory.getLogger(getClass())
    private final ProfileRepository profileRepository
    private final ProfileService profileService

    ProfileController(ProfileRepository profileRepository, ProfileService profileService) {
        this.profileRepository = profileRepository
        this.profileService = profileService
    }

    // Список профилей
    @GetMapping
    def getProfiles() {
        return profileRepository.findAll()
    }

    // Поиск профиля по name
    @GetMapping("/{name}")
    def getProfileById(@PathVariable("name") String name, HttpServletRequest request) {
        if (!profileRepository.findById(name))
            throw new NotFoundException("Профиль не найден - ${name}", request.method, request.requestURI)
        return profileRepository.findById(name)
    }

    // Расчет профиля в зависимости от количества серверов
    @GetMapping("/profileCalculation")
    def getProfileCalculation(@RequestParam("profileName") String profileName, @RequestParam("serverCount") String cnt, HttpServletRequest request) {
        if (!cnt.isInteger())
            throw new BadRequestException("Неверный запрос. Get-параметр serverCount != Integer")
        Integer serverCount = cnt.toInteger()
        def profile = profileService.profileCalculation(profileName, serverCount)
        if (profile instanceof Exception)
            throw new BadRequestException(
                    "Неверный запрос. Get-параметр profileName указан неверно. Паттерн: {name} или {name}:{throughput}:{threads}:{rampUp}")
        logger.info("${request.method} ${request.requestURI}; message: ${profile}")
        return profile
    }

    // Добавить профиль
    @PostMapping("/add")
    def addProfile(@Valid @RequestBody ProfileDTO profileDto, HttpServletRequest request) {
        Profile profile = profileService.addProfile(profileDto)
        logger.info("${request.method} ${request.requestURI}; message: Профиль добавлен - ${profile.name}")
        return profile
    }

    // Удалить профиль
    @DeleteMapping("/{name}")
    def deleteProfile(@PathVariable("name") String name, HttpServletRequest request) {
        if (!profileRepository.findById(name))
            throw new NotFoundException("Профиль не найден - ${name}")
        profileRepository.deleteById(name)
        logger.info("${request.method} ${request.requestURI}; message: Профиль удален - ${name}")
        return ResponseEntity.noContent().build()
    }
}
