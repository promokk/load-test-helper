package com.example.load_test_helper.test

import com.example.load_test_helper.profile.ProfileService
import com.example.load_test_helper.server.ServerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class TestService {
    def logger = LoggerFactory.getLogger(getClass())
    private final TestRepository testRepository
    private final ProfileService profileService
    private final ServerService serverService

    TestService(TestRepository testRepository, ProfileService profileService, ServerService serverService) {
        this.testRepository = testRepository
        this.profileService = profileService
        this.serverService = serverService
    }

    // Создать тест
    @Transactional()
    def createTest(Object test) {
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
    }

    // Удалить тест
    @Transactional(isolation = Isolation.SERIALIZABLE)
    def deleteTest(Integer id) {
        try {
            def server = testRepository.findById(id).get().server
            serverService.serverBookingCancel(server)
            testRepository.deleteById(id)
            return true
        } catch (Exception ex) {
            logger.error("TestService: deleteTest; status: ERROR; message: Ошибка ${ex.getStackTrace()}")
            return false
        }
    }
}
