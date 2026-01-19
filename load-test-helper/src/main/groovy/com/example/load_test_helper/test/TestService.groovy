package com.example.load_test_helper.test

import com.example.load_test_helper.profile.ProfileService
import com.example.load_test_helper.server.ServerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class TestService {
    private final TestRepository testRepository
    private final ProfileService profileService
    private final ServerService serverService

    TestService(TestRepository testRepository, ProfileService profileService, ServerService serverService) {
        this.testRepository = testRepository
        this.profileService = profileService
        this.serverService = serverService
    }

    // Создать тест
    def createTest(Object test) {
        try {
            List<String> profile = []
            List<String> profileArr = profileService.profileCalculation(test["profile"])
            for (List<String> p : profileArr) {
                profile += p.join(":")
            }
            Test newTest = new Test(
                    test["stand"],
                    test["duration"],
                    test["server"],
                    profile
            )
            testRepository.save(newTest)
            return newTest
        } catch (ex) {
            return  ex
        }
    }

    // Удалить тест
    def deleteTest(Integer id) {
        if (testRepository.findById(id)) {
            def server = testRepository.findById(id).get().server
            serverService.serverBookingCancel(server)
            testRepository.deleteById(id)
            return true
        } else {
            return false
        }
    }
}
