package com.example.load_test_helper.profile

import org.springframework.stereotype.Service

@Service
class ProfileService {
    private final ProfileRepository profileRepository

    ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository
    }

    // Расчет профиля в зависимости от количества серверов
    def profileCalculation(String profiles, Integer serverCount = 1) {
        try {
            def profilesArr = profiles.split(",")
            profilesArr = profilesArr.collect { it.split(":") }
            for (i in 0..profilesArr.size()-1) {
                def params = profilesArr[i]
                if (params.size() == 1) {
                    def profile = profileRepository.findById(params[0]).get()
                    params += (profile.throughput / serverCount).round(5)
                    params += Math.ceil(profile.threads / serverCount).toInteger()
                    params += profile.rampUp
                    profilesArr[i] = params
                } else {
                    // проверка - существует профиль в БД
                    profileRepository.findById(params[0]).get()
                    params[1] = (params[1].toDouble() / serverCount).round(5)
                    params[2] = Math.ceil(params[2].toDouble() / serverCount).toInteger()
                    profilesArr[i] = params
                }
            }
            return profilesArr
        } catch (ex) {
            return ex
        }
    }
}
