package com.example.load_test_helper.server

import com.example.load_test_helper.profile.ProfileService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Isolation

@Service
class ServerService {
    private final ServerRepository serverRepository
    private final ProfileService profileService

    ServerService(ServerRepository serverRepository, ProfileService profileService) {
        this.serverRepository = serverRepository
        this.profileService = profileService
    }

    // Добавить сервер
    @Transactional(isolation = Isolation.READ_COMMITTED)
    def addProfile(ServerDTO serverDto) {
        Server server = new Server(
                name: serverDto.name,
                free: serverDto.free
        )
        serverRepository.save(server)
    }

    // Бронирование N серверов
    @Transactional(isolation = Isolation.SERIALIZABLE)
    def serverBookingCnt(String cnt) {
        Iterable<Server> servers = serverRepository.findByFree(true)
        try {
            ArrayList serverArr = []
            Integer count = cnt.toInteger()
            for (server in servers[0..count-1]) {
                serverArr += server.name
                server.free = false
                serverRepository.save(server)
            }
            return serverArr
        } catch (ex) {
            return [false, cnt, servers.size()]
        }
    }

    // Автоматическое бронирование серверов на основе профилей
    def serverBookingAuto(String profiles) {
        def threadMax = 0
        def profilesArr = profileService.profileCalculation(profiles)
        if (profilesArr instanceof Exception) {
            return profilesArr
        }
        for (profile in profilesArr) {
            def threadCount = profile[2].toInteger()
            threadMax = threadCount > threadMax ? threadCount : threadMax
        }
        return serverBookingCnt(Math.ceil(threadMax / 2000).toInteger().toString())
    }

    // Бронирование списка серверов
    @Transactional(isolation = Isolation.SERIALIZABLE)
    def serverBooking(String servers) {
        def serverBusy = ""
        def serversArr = servers.split(",")
        for (s in serversArr) {
            def server = serverRepository.findById(s)
            if (!server) {
                serverBusy += "${s}-nf,"
            } else if (!server.get().free) {
                serverBusy += "${s}-b,"
            }
        }
        if (serverBusy) {
            return serverBusy.substring(0, serverBusy.length() - 1)
        }
        for (s in serversArr) {
            Server server = serverRepository.findById(s).get()
            server.free = false
            serverRepository.save(server)
        }
        return serverBusy
    }

    // Отмена бронирования N серверов
    @Transactional(isolation = Isolation.SERIALIZABLE)
    def serverBookingCancel(List<String> serverArr) {
        for (serverName in serverArr) {
            Server server = serverRepository.findById(serverName).get()
            server.free = true
            serverRepository.save(server)
        }
    }

    // Отмена бронирования всех серверов
    @Transactional(isolation = Isolation.SERIALIZABLE)
    def serverBookingCancelAll() {
        Iterable<Server> servers = serverRepository.findByFree(false)
        for (server in servers) {
            server.free = true
            serverRepository.save(server)
        }
        return servers.size()
    }
}
