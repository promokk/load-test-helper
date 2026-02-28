package com.example.load_test_helper.profile

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService {
    private final ProfileRepository profileRepository

    ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository
    }

    // Добавить профиль
    @Transactional()
    def addProfile(ProfileDTO profileDto) {
        Profile profile = new Profile(
                name: profileDto.name,
                throughput: profileDto.throughput,
                threads: profileDto.threads,
                rampUp: profileDto.rampUp
        )
        profileRepository.save(profile)
    }

    // Расчет профиля в зависимости от количества серверов
    def profileCalculation(String profiles, Integer serverCount = 1) {
        try {
            def profilesArr = profiles.split(",")
            profilesArr = profilesArr.collect { it.split(":") }
            for (i in 0..profilesArr.size()-1) {
                def params = profilesArr[i]
                // Проверка что передан профиль без параметров
                // 0 - name; 1 - throughput; 2 - threads; 3 - rampUp
                if (params.size() == 1) {
                    def profile = profileRepository.findById(params[0]).get()
                    params += (profile.throughput / serverCount).round(5)
                    params += Math.ceil(profile.threads / serverCount).toInteger()
                    params += profile.rampUp
                    profilesArr[i] = params
                } else {
                    params[1] = (params[1].toDouble() / serverCount).round(5)
                    params[2] = Math.ceil(params[2].toDouble() / serverCount).toInteger()
                    // Проверка что передан rampUp
                    params[3] = params[3]
                    profilesArr[i] = params
                }
            }
            return profilesArr
        } catch (ex) {
            return ex
        }
    }
}
