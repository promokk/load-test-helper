package com.example.load_test_helper.server

import com.example.load_test_helper.profile.ProfileService
import org.springframework.stereotype.Service

@Service
class ServerService {
    private final ServerRepository serverRepository
    private final ProfileService profileService

    ServerService(ServerRepository serverRepository, ProfileService profileService) {
        this.serverRepository = serverRepository
        this.profileService = profileService
    }

    // Бронирование N серверов
    def serverBookingCnt(String cnt) {
        Iterable<Server> servers = serverRepository.findByFree(true)
        try {
            String strFree = ""
            Integer count = cnt.toInteger()
            for (server in servers[0..count-1]) {
                strFree += "${server.name},"
                server.free = false
                serverRepository.save(server)
            }
            return strFree.substring(0, strFree.length() - 1)
        } catch (ex) {
            return [cnt, servers.size()]
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
}
