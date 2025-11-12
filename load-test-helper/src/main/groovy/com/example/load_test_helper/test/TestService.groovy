package com.example.load_test_helper.test

import com.example.load_test_helper.profile.ProfileService
import org.springframework.stereotype.Service

@Service
class TestService {
    private final TestRepository startupRepository
    private final ProfileService profileService

    TestService(TestRepository startupRepository, ProfileService profileService) {
        this.startupRepository = startupRepository
        this.profileService = profileService
    }

    // Создать тест
    def createTest(Object test) {
        try {
            List<String> profile = []
            List<String> profileArr = profileService.profileCalculation(test["profile"])
            for (List<String> p : profileArr) {
                profile += p.join(":")
            }
            ArrayList server = test["server"].split(",")
            Test newTest = new Test(
                    test["stand"],
                    test["duration"],
                    server,
                    profile
            )
            startupRepository.save(newTest)
            return newTest
        } catch (ex) {
            return  ex
        }
    }
}
