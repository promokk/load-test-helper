package com.example.load_test_helper.server

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
import org.springframework.web.bind.annotation.ResponseStatus
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
    def getServers(HttpServletRequest request) {
        logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
        return serverRepository.findAll()
    }

    // Поиск сервера по name
    @GetMapping("/{name}")
    def getServerById(@PathVariable("name") String name, HttpServletRequest request) {
        if (serverRepository.findById(name)) {
            logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
            return serverRepository.findById(name)
        } else {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Профиль не найден - ${name}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Профиль не найден - ${name}")
        }
    }

    // Список свободных серверов
    @GetMapping("/free")
    def getServerFree(@RequestParam("state") String state, HttpServletRequest request) {
        try {
            if (state == "true") {
                logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
                return serverRepository.findByFree(true)
            } else if (state == "false") {
                logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
                return serverRepository.findByFree(false)
            } else {
                logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: неверное состояние сервера - ${state}")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: неверное состояние сервера - ${state}")
            }
        } catch (ex) {
            logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Количество серверов
    @GetMapping("/count")
    def getServerCount(@RequestParam("state") String state, HttpServletRequest request) {
        try {
            if (state == "true") {
                logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
                return serverRepository.findByFree(true).findAll().size()
            } else if (state == "false") {
                logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Успех")
                return serverRepository.findByFree(false).findAll().size()
            } else {
                logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: неверное состояние сервера - ${state}")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: неверное состояние сервера - ${state}")
            }
        } catch (ex) {
            logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Добавить сервер
    @PostMapping("/add")
    def addServer(@RequestBody Server server, HttpServletRequest request) {
        try {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Сервер добавлен - ${server.name}")
            return serverRepository.save(server)
        } catch (ex) {
            if (ex.class.name == "org.springframework.dao.DataIntegrityViolationException") {
                logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Некорректный запрос")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST }; message: Некорректный запрос")
            }
            logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Бронирование N серверов
    @PostMapping("/serverBooking/{cnt}")
    def postServerBookingCnt(@PathVariable("cnt") String cnt, HttpServletRequest request) {
        try {
            if (cnt.isInteger()) {
                def servers = serverService.serverBookingCnt(cnt)
                if (servers instanceof ArrayList) {
                    logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Нужное количество свободных серверов не найдено - ${servers[0]}. Доступно: ${servers[1]}")
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Нужное количество свободных серверов не найдено - ${servers[0]}. Доступно: ${servers[1]}")
                }
                logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: ${servers}")
                return servers
            } else {
                def serverBusy = serverService.serverBooking(cnt)
                if (!serverBusy) {
                    logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: ${cnt}")
                    return cnt
                }
                logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Сервера забронированы(b) / не найдены(nf): ${serverBusy}")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Сервера забронированы(b) / не найдены(nf): ${serverBusy}")
            }
        } catch (ex) {
            logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Автоматическое бронирование нужного кол-ва серверов на основе профилей
    @PostMapping("/serverBookingAuto")
    def postServerBookingAuto(@RequestParam("profiles") String profiles, HttpServletRequest request) {
        try {
            def servers = serverService.serverBookingAuto(profiles)
            if (servers instanceof ArrayList) {
                logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Нужное количество свободных серверов не найдено - ${servers[0]}. Доступно: ${servers[1]}")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Нужное количество свободных серверов не найдено - ${servers[0]}. Доступно: ${servers[1]}")
            }
            if (servers instanceof Exception) {
                logger.error("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Некорректный запрос; stackTrace: ${servers.getStackTrace()}")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST }; message: Некорректный запрос")
            }
            logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: ${servers}")
            return servers
        } catch (ex) {
            logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Отмена бронирования N серверов
    @PostMapping("/serverBooking/cancel")
    def postServerBookingCancel(@RequestParam("server") String serverStr, HttpServletRequest request) {
        try {
            String[] serverArr = serverStr.split(",")
            for (serverName in serverArr) {
                try {
                    serverRepository.findById(serverName).get()
                } catch (ex) {
                    logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.BAD_REQUEST}; message: Сервер ${serverName} не найден")
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("${HttpStatus.BAD_REQUEST}; message: Сервер ${serverName} не найден")
                }
            }
            for (serverName in serverArr) {
                Server server = serverRepository.findById(serverName).get()
                server.free = true
                serverRepository.save(server)
            }
            logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: операция выполнена для ${serverArr}")
            return ResponseEntity.status(HttpStatus.OK).body("message: операция выполнена для ${serverArr}")
        } catch (ex) {
            logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Отмена бронирования всех серверов
    @PostMapping("/serverBooking/cancel/all")
    def postServerBookingCancelAll(HttpServletRequest request) {
        try {
            Iterable<Server> servers = serverRepository.findByFree(false)
            Integer serversCnt = servers.size()
            for (server in servers) {
                server.free = true
                serverRepository.save(server)
            }
            logger.info("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Операция выполнена для всех забронированных серверов, кол-во: ${serversCnt}")
            return ResponseEntity.status(HttpStatus.OK).body("message: Операция выполнена для всех забронированных серверов, кол-во: ${serversCnt}")
        } catch (ex) {
            logger.error("path: ${request.getRequestURI()}; statusCode: ${HttpStatus.INTERNAL_SERVER_ERROR}; message: ${ex.getMessage()}; stackTrace: ${ex.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("${HttpStatus.INTERNAL_SERVER_ERROR }; message: Непредвиденная ошибка")
        }
    }

    // Удалить сервер
    @DeleteMapping("/{name}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    def deleteServer(@PathVariable("name") String name, HttpServletRequest request) {
        if (serverRepository.findById(name)) {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.OK}; message: Сервер удален - ${name}")
            serverRepository.deleteById(name)
            return ResponseEntity.noContent().build()
        } else {
            logger.info("method: ${request.method}; path: ${request.getRequestURI()}; statusCode: ${HttpStatus.NOT_FOUND}; message: Сервер не найден - ${name}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("${HttpStatus.NOT_FOUND }; message: Сервер не найден - ${name}")
        }
    }
}
