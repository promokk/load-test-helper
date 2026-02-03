package com.example.load_test_helper.server

import com.example.load_test_helper.exception.BadRequestException
import com.example.load_test_helper.exception.NotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/server")
class ServerController {
    def logger = LoggerFactory.getLogger(getClass())
    private final ServerRepository serverRepository
    private final ServerService serverService

    ServerController(ServerRepository serverRepository, ServerService serverService) {
        this.serverRepository = serverRepository
        this.serverService = serverService
    }

    // Список всех серверов
    @GetMapping
    def getServers() {
        return serverRepository.findAll()
    }

    // Поиск сервера по name
    @GetMapping("/{name}")
    def getServerById(@PathVariable("name") String name) {
        def server = serverRepository.findById(name) ?: false
        if (!server)
            throw new NotFoundException("Сервер не найден - ${name}")
        return server
    }

    // Список свободных серверов
    @GetMapping("/free")
    def getServerFree(@RequestParam("state") String state) {
        if (state == "true") {
            return serverRepository.findByFree(true)
        } else if (state == "false") {
            return serverRepository.findByFree(false)
        } else {
            throw new BadRequestException("Неверный запрос. Get-параметр state != Boolen")
        }
    }

    // Количество серверов
    @GetMapping("/count")
    def getServerCount(@RequestParam("state") String state) {
        if (state == "true") {
            return serverRepository.findByFree(true).findAll().size()
        } else if (state == "false") {
            return serverRepository.findByFree(false).findAll().size()
        } else {
            throw new BadRequestException("Неверный запрос. Get-параметр state != Boolen")
        }
    }

    // Добавить сервер
    @PostMapping("/add")
    def addServer(@RequestBody ServerDTO serverDto, HttpServletRequest request) {
        Server server = serverService.addProfile(serverDto)
        logger.info("${request.method} ${request.requestURI}; message: Сервер добавлен - ${server.name}")
        return server
    }

    // Бронирование N серверов / список серверов
    @PostMapping("/serverBooking/{cnt}")
    def postServerBookingCnt(@PathVariable("cnt") String cnt, HttpServletRequest request) {
        if (cnt.isInteger()) {
            def servers = serverService.serverBookingCnt(cnt)
            if (servers[0] == false) {
                logger.error("${request.method} ${request.requestURI}; message: Нужное количество свободных серверов не найдено - ${servers[1]}. Доступно: ${servers[2]}")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Нужное количество свободных серверов не найдено - ${servers[1]}. Доступно: ${servers[2]}")
            }
            logger.info("${request.method} ${request.requestURI}; message: ${servers}")
            return servers
        } else {
            def serverBusy = serverService.serverBooking(cnt)
            if (!serverBusy) {
                logger.info("${request.method} ${request.requestURI}; message: ${cnt}")
                return cnt.split(",")
            }
            logger.error("${request.method} ${request.requestURI}; message: Сервера забронированы(b) / не найдены(nf): ${serverBusy}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Сервера забронированы(b) / не найдены(nf): ${serverBusy}")
        }
    }

    // Автоматическое бронирование нужного кол-ва серверов на основе профилей
    @PostMapping("/serverBookingAuto")
    def postServerBookingAuto(@RequestParam("profiles") String profiles, HttpServletRequest request) {
        def servers = serverService.serverBookingAuto(profiles)
        if (servers instanceof Exception) {
            throw new BadRequestException(
                    "Неверный запрос. Get-параметр profiles указан неверно. Паттерн: {name} или {name}:{throughput}:{threads}:{rampUp}")
        }
        if (servers[0] == false) {
            logger.error("${request.method} ${request.requestURI}; message: Нужное количество свободных серверов не найдено - ${servers[1]}. Доступно: ${servers[2]}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Нужное количество свободных серверов не найдено - ${servers[1]}. Доступно: ${servers[2]}")
        }
        logger.info("${request.method} ${request.requestURI}; message: ${servers}")
        return servers
    }

    // Отмена бронирования N серверов
    @PostMapping("/serverBooking/cancel")
    def postServerBookingCancel(@RequestParam("server") String serverStr, HttpServletRequest request) {
        List<String> serverArr = serverStr.split(",")
        for (serverName in serverArr) {
            if (!serverRepository.findById(serverName))
                throw new NotFoundException("Сервер не найден - ${serverName}")
        }
        serverService.serverBookingCancel(serverArr)
        logger.info("${request.method} ${request.requestURI}; message: Операция выполнена для: ${serverArr}")
        return ResponseEntity.status(HttpStatus.OK).body("message: Операция выполнена для: ${serverArr}")
    }

    // Отмена бронирования всех серверов
    @PostMapping("/serverBooking/cancel/all")
    def postServerBookingCancelAll(HttpServletRequest request) {
        Integer serversCnt = serverService.serverBookingCancelAll()
        logger.info("${request.method} ${request.requestURI}; message: Операция выполнена для всех забронированных серверов, кол-во: ${serversCnt}")
        return ResponseEntity.status(HttpStatus.OK).body("message: Операция выполнена для всех забронированных серверов, кол-во: ${serversCnt}")
    }

    // Удалить сервер
    @DeleteMapping("/{name}")
    def deleteServer(@PathVariable("name") String name, HttpServletRequest request) {
        if (!serverRepository.findById(name))
            throw new NotFoundException("Сервер не найден - ${name}")
        serverRepository.deleteById(name)
        logger.info("${request.method} ${request.requestURI}; message: Сервер удален - ${name}")
        return ResponseEntity.noContent().build()
    }
}
