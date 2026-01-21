package com.example.load_test_helper.profile

import com.example.load_test_helper.exception.BadRequestException
import com.example.load_test_helper.exception.NotFoundException
import com.example.load_test_helper.scenario.Scenario
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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

//        if (profileRepository.findById(name)) {
//            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
//            return profileRepository.findById(name)
//        } else {
//            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Профиль не найден - ${name}")
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Профиль не найден - ${name}")
//        }
    }

    // Расчет профиля в зависимости от количества серверов
    @GetMapping("/profileCalculation")
    def getProfileCalculation(@RequestParam("profileName") String profileName, @RequestParam("serverCount") String cnt, HttpServletRequest request) {
        if (profileName == null || cnt == null)
            throw new BadRequestException("Неверный запрос. Обязательные get-параметры: profileName, cnt")
        if (!cnt.toInteger())
            throw new BadRequestException("Неверный запрос. Get-параметр cnt != Integer")

        Integer serverCount = cnt.toInteger()
        def profile = profileService.profileCalculation(profileName, serverCount)
        if (profile instanceof Exception)
            throw new BadRequestException(
                    "Неверный запрос. Get-параметр profileName указан неверно. Паттерн: {name} или {name}:{throughput}:{threads}:{rampUp}")
        logger.info("${request.method} ${request.requestURI}; message: ${profile}")
        return profile

//        if (profile instanceof Exception) {
//            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Некорректный запрос; stackTrace: ${profile.getStackTrace()}")
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST }; message: Некорректный запрос")
//        }
//        logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: ${profile}")
//        return profile
    }

    // Добавить профиль
    @PostMapping("/add")
    def addProfile(@RequestBody Profile profile, HttpServletRequest request) {
        if (profile.name == null || profile.throughput == null || profile.threads == null || profile.rampUp == null)
            throw new BadRequestException("Неверное тело запроса. Обязательные поля: name, throughput, threads, rampUp")
        Profile newProfile = profileRepository.save(profile)
        logger.info("${request.method} ${request.requestURI}; message: Профиль добавлен - ${profile.name}")
        return newProfile

//        try {
//            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Профиль добавлен - ${profile.name}")
//            return profileRepository.save(profile)
//        } catch (ex) {
//            if (ex.class.name == "org.springframework.dao.DataIntegrityViolationException") {
//                logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Некорректный запрос")
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST }; message: Некорректный запрос")
//            }
//            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
//        }
    }

    // Удалить профиль
    @DeleteMapping("/{name}")
//    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    def deleteProfile(@PathVariable("name") String name, HttpServletRequest request) {
        if (!profileRepository.findById(name))
            throw new NotFoundException("Профиль не найден - ${name}")
        profileRepository.deleteById(name)
        logger.info("${request.method} ${request.requestURI}; message: Профиль удален - ${name}")
        return ResponseEntity.noContent().build()

//        if (profileRepository.findById(name)) {
//            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Профиль удален - ${name}")
//            profileRepository.deleteById(name)
//            return ResponseEntity.noContent().build()
//        } else {
//            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Профиль не найден - ${name}")
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Профиль не найден - ${name}")
//        }
    }
}
