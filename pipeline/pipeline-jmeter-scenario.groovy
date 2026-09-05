import groovy.transform.Field

// Статус задания
@Field
def statusJob = 'ERROR'
// Массив с результатами запуска теста
@Field
def testResultArr = []

node {
    // environment
    // Сredential для подключения по ssh
    def credentialSSH = 'credential-ssh'
    // Ссылка для подключения к репозиторию
    def gitRepository = 'git@github.com:username/jmeter-script.git'
    // Адрес сервера с приложением load-test-helper
    def hostTestHelper = '{host}:8088'
    // Шаблон для проверки http-запросов
    def checkHttpError = "|| echo ' --> ERROR'"

    try {
        properties([
            buildDiscarder(
                logRotator(
                    artifactDaysToKeepStr: '',
                    artifactNumToKeepStr: '',
                    daysToKeepStr: '14',
                    numToKeepStr: '30')
            ),
            parameters([
                string(
                    name: 'SCENARIONAME',
                    description: 'Название сценария. Пример: someName',
                    trim: true),
                choice(
                        name: 'STAND',
                        choices: ['Null', 'LT1', 'LT2'],
                        description: 'Выбор стенда. Влияет на выбор URL. Используется в названии jmx-файла'),
                string(
                        name: 'STAND_CUSTOM',
                        description: 'Свой вариант STAND',
                        trim: true),
                gitParameter(
                        name: 'BRANCH',
                        branch: '',
                        description: 'По умолчанию: master',
                        branchFilter: '^origin/(.*)$',
                        defaultValue: 'master',
                        quickFilterEnabled: true,
                        selectedValue: 'NONE',
                        sortMode: 'NONE',
                        tagFilter: '*',
                        listSize: '10',
                        useRepository: "${gitRepository}",
                        type: 'GitParameterDefinition')
                ])])
        stage('Start scenario') {
            sshagent(["${credentialSSH}"]) {
                // Очистка WORKSPACE
                echo '-----------------DELETING WORKSPACE-----------------'
                deleteDir()
                // Редактирование displayName задания
                currentBuild.displayName = "${env.BUILD_ID}--${SCENARIONAME}"

                // Старт сценария
                echo '-----------------START SCENARIO-----------------'
                def scenario = sh(returnStdout: true, script: "curl --fail-with-body -X GET ${hostTestHelper}/scenario/${SCENARIONAME} -H 'Content-Type: application/json' ${checkHttpError}")
                scenario = checkHttpStatus(scenario, "ERROR-START-SCENARIO", "Stopping early. Scenario start error.")

                def stand = scenario.stand
                def domainMain = scenario.domain
                def durationMain = scenario.duration
                def groupsArr = scenario.groups

                if (params.STAND != 'Null') stand = params.STAND
                if (params.STAND_CUSTOM) stand = params.STAND_CUSTOM

                def groupMap = [:]
                groupsArr.eachWithIndex { group ->
                    groupMap[group.id] = {
                        echo "Запуск теста для группы: ${group.id}"
                        startJMeterRun(
                                group.profile,
                                group.server ?: '',
                                group.masterRun,
                                group.duration ?: durationMain,
                                stand,
                                group.domain ?: domainMain,
                                group.certificate ?: '',
                                group.testParam ?: '',
                                group.serverParam ?: '',
                                BRANCH
                        )
                    }
                }
                parallel groupMap
                statusJob = 'SUCCESS'
            }
        }
    }  catch (e) {
        echo "Failed because of {$e}"
    } finally {
        sshagent(["${credentialSSH}"]) {
            echo '-----------------CHECK STATUS JOB-----------------'
            switch (statusJob) {
                case 'SUCCESS':
                    echo "Successful success!"
                    echo "Результат выполнения: ${testResultArr}"
                    currentBuild.description = "${testResultArr}"
                    break
                case 'ERROR-START-SCENARIO':
                    echo "Failed with an error. Status Job = ${statusJob}"
                    break
                default:
                    echo "The end."
                    echo "Результат выполнения: ${testResultArr}"
                    currentBuild.description = "${testResultArr}"
            }
        }
    }
}

// function
// Преобразование LazyMap
def deepConvert(obj) {
    if (obj instanceof Map) {
        return obj.collectEntries { k, v -> [(k): deepConvert(v)] }
    } else if (obj instanceof Collection) {
        return obj.collect { deepConvert(it) }
    } else {
        return obj
    }
}

// Проверка выполнения http-запроса
def checkHttpStatus(connection, status, message) {
    if (connection.contains('ERROR')) {
        echo "${connection}"
        statusJob = status
        error(message)
    } else {
        echo "Success response --> ${connection}"
        try {
            connection = new groovy.json.JsonSlurper().parseText(connection)
            return deepConvert(connection)
        } catch(e) {
            return connection
        }
    }
}

// Создание задачи в jmeter-run
def startJMeterRun(profile, server, masterRun, duration, stand, domain, certificate, customParamRun, customParamServer, branchGit) {
    // Наименование дочернего задания
    def childJobName = "jmeter-run"

    echo """Параметры:
        |profile: ${profile}
        |server: ${server}
        |masterRun: ${masterRun}
        |duration: ${duration}
        |stand: ${stand}
        |domain: ${domain}
        |certificate: ${certificate}
        |customParamRun: ${customParamRun}
        |customParamServer: ${customParamServer}
        |branchGit: ${branchGit}
    """.stripMargin()

    def childJob = build job: "${childJobName}", parameters: [
            string(name: 'PROFILE', value: profile),
            string(name: 'SERVER', value: server),
            booleanParam(name: 'MASTERRUN', value: masterRun),
            string(name: 'DURATION', value: duration.toString()),
            string(name: 'STAND', value: stand),
            string(name: 'DOMAIN', value: domain),
            string(name: 'CERTIFICATE', value: certificate),
            string(name: 'CUSTOMPARAMRUN', value: customParamRun),
            string(name: 'CUSTOMPARAMSERVER', value: customParamServer),
            gitParameter(name: 'BRANCH', value: branchGit)
    ],propagate: false, wait: true

    copyArtifacts(projectName: "${childJobName}", filter: 'resultJob.txt', selector: [$class: 'SpecificBuildSelector', buildNumber: "${childJob.number}"])
    def resultJob = readFile 'resultJob.txt'
    testResultArr << resultJob
    echo "Результат: ${resultJob}"
}
