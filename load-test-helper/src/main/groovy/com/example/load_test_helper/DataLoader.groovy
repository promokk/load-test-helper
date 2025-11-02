package com.example.load_test_helper

import com.example.load_test_helper.profile.Profile
import com.example.load_test_helper.profile.ProfileRepository
import com.example.load_test_helper.server.Server
import com.example.load_test_helper.server.ServerRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DataLoader {
    def logger = LoggerFactory.getLogger(getClass())
    private final ServerRepository serverRepository
    private final ProfileRepository profileRepository

    @Value('${serverListFile:serverList.dat}')
    private String serverListFile

    @Value('${profileListFile:profileList.dat}')
    private String profileListFile

    @Value('${dbName:db_server}')
    private String dbName

    @Value('${userDir}')
    private String userDir

    DataLoader(ServerRepository serverRepository, ProfileRepository profileRepository) {
        this.serverRepository = serverRepository
        this.profileRepository = profileRepository
    }

    @PostConstruct
    private void dataLoader() {
        if (!serverRepository.count()) {
            String[] serverArr = new File("${serverListFile}").text.split("\n")
            serverRepository.deleteAll()
            for (server in serverArr) {
                serverRepository.save(new Server(server))
            }
            logger.info("DataLoader - serverRepository --> Выполнено наполнение БД после инициализации")
        } else {
            logger.info("DataLoader - serverRepository --> Наполнение БД не выполнено. БД преднаполнена")
        }

        if (!profileRepository.count()) {
            String[] profileArr = new File("${profileListFile}").text.split("\n")
            profileRepository.deleteAll()
            for (profile in profileArr) {
                profile = profile.split(":")
                profileRepository.save(new Profile(profile[0],profile[1].toDouble(),profile[2].toInteger(),profile[3].toInteger()))
            }
            logger.info("DataLoader - profileRepository --> Выполнено наполнение БД после инициализации")
        } else {
            logger.info("DataLoader - profileRepository --> Наполнение БД не выполнено. БД преднаполнена")
        }
    }
}
