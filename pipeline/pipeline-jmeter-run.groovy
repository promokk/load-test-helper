import groovy.transform.Field

// Статус задания
@Field
def statusJob = 'ERROR'
// Результат задания (статус / id теста)
@Field
def resultJob = statusJob

node {
    // environment
    // Пользователь для подключения по ssh
    def userSSH = 'user'
    // Сredential для подключения по ssh
    def credentialSSH = 'credential-ssh'
    // Ссылка для подключения к репозиторию
    def gitRepository = 'git@github.com:username/jmeter-script.git'
    // Сredential для подключения к репозиторию
    def gitCredentialsId = 'credential-git'
    // Путь в репозитории до директории с архивами jmx-скриптов
    def gitPathScript = 'jmeter-script'
    // Путь в репозитории до директории с сертификатами
    def gitPathCertificate = 'certificate'
    // Корневая директория jmeter
    def jmeterDir = '/opt/jmeter/bin'
    // Директория в которой будет создаваться временная директория для запуска теста (workDir)
    def homeDir = '/dir'
    // Временная директория из которой будет запускаться тест
    def workDir = 'jmeterRun'
    // Наименование файла для запуска jmeter
    def jmeter = 'jmeter'
    // Наименование файла для запуска jmeter-server
    def jmeterServer = 'jmeter-server'
    // Адрес БД для Backend Listener
    def dataSourceBL = '{host}:8086/write?db=jmeter'
    // Адрес SD сервера для Prometheus / VictoriaMetrics
    def hostSDServer = '{host}:8089'
    // Порт jmx-exporter для запуска процесса jmeter (стартовое значение)
    def jmxJmeterStartPort = 49200
    // Порт jmx-exporter для запуска процесса jmeter-server (стартовое значение)
    def jmxJmeterStartServerPort = 49250
    // Массив занятых серверов (создается пустым)
    def serverBusyArr = []
    // Адрес сервера с приложением load-test-helper
    def hostTestHelper = '{host}:8088'
    // Шаблон для проверки http-запросов
    def checkHttpError = "|| echo ' --> ERROR'"

    // Массив серверов
    def serverArr = []
    // Массив серверов для rest-запроса
    def serverArrRest = []
    // Master-сервер
    def masterServer = ''
    // Кол-во серверов
    def serverCnt = 0
    // Массив профилей
    def profileArr = []
    // Кол-во серверов
    def profileCnt = 0
    // Индификатор теста
    def testId = 0
    // URL для запуска теста
    def urlRun = ''

    try {
        properties([
            buildDiscarder(
                logRotator(
                    artifactDaysToKeepStr: '',
                    artifactNumToKeepStr: '',
                    daysToKeepStr: '14',
                    numToKeepStr: '30')
            ),
            copyArtifactPermission('jmeter-scenario'),
            parameters([
                string(
                        name: 'PROFILE',
                        defaultValue: 'scriptMonitoring:10:20:60,scriptMonitoring-02:20:40:60',
                        description: 'Название профиля / jmx-файла без расширения + параметры запуска. Пример: script01:throughput:threads:rampUp,script02:throughput:threads:rampUp',
                        trim: true),
                string(
                        name: 'SERVER',
                        description: 'Количество генераторов нагрузки / Список серверов через запятую / null - автоматический выбор кол-ва генераторов',
                        trim: true),
                booleanParam(
                        name: 'MASTER_RUN',
                        defaultValue: true,
                        description: 'Должен ли master-сервер генерировать нагрузку'),
                string(
                        name: 'DURATION',
                        defaultValue: '300',
                        description: 'Продолжительность запуска, сек',
                        trim: true),
                string(
                        name: 'STAND',
                        description: 'Наименование стенда. Влияет на выбор URL. Используется в названии jmx-файла',
                        trim: true),
                string(
                        name: 'DOMAIN',
                        description: 'Наименование домена. Влияет на выбор URL',
                        trim: true),
                string(
                        name: 'CUSTOM_URL',
                        description: 'Свой вариант URL для запуска jmx-файла',
                        trim: true),
                string(
                        name: 'CERTIFICATE',
                        description: 'Выбор сертификата (p12 / keystore). Полное название с расширением',
                        trim: true),
                string(
                        name: 'CERTIFICATE_P',
                        description: 'Пароль от сертификата',
                        trim: true),
                string(
                        name: 'CUSTOM_PARAM_RUN',
                        description: 'Доп. параметры запуска jmeter (-G/-D/-J/-L и тд.). Пример: -Gparam01=hello -Gparam02=12345',
                        trim: true),
                string(
                        name: 'CUSTOM_PARAM_SERVER',
                        description: 'Доп. параметры запуска jmeter-server. Пример: -Dparam01=hello -Dparam02=12345',
                        trim: true),
                booleanParam(
                        name: 'JMX_EXPORTER_RUN',
                        defaultValue: false,
                        description: 'Вкл / Выкл мониторинг JMX-метрик'),
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

        // environment
        /*
        Используется если при выборе серверов используются не ip, а сокращенное название.
        Добавляет постфикс, получается полное название сервера.
        serverArr = SERVER.replace(',', '.suffix.ru,')  + '.suffix.ru'
        serverArr = serverArr.split(',').collect{ it.toString() }
         */

        urlRun = CUSTOM_URL
        
        stage('Configuration Settings') {
            // Очистка WORKSPACE
            echo '-----------------DELETING WORKSPACE-----------------'
            deleteDir()

            // Проверка обязательных параметров
            echo '-----------------CHECK PARAMS-----------------'
            if (!params.PROFILE) error('Stopping early. Profile is not selected.')
            if (!params.STAND) error('Stopping early. Stand is not selected.')
            if (!params.DOMAIN && !params.CUSTOM_URL) error('Stopping early. Domain or custom_url is not selected.')

            // Для мониторинга jmx-метрик используются другой файл jmeter и jmeter-server
            if (params.JMX_EXPORTER_RUN) {
                jmeter = 'jmeter-jmx'
                jmeterServer = 'jmeter-server-jmx'
            }

            // Бронирование серверов
            echo '-----------------SERVER BOOKING-----------------'
            if (SERVER) {
                serverArr = sh(returnStdout: true, script: "curl --fail-with-body -X POST ${hostTestHelper}/server/serverBooking/${SERVER} ${checkHttpError}")
            } else {
                serverArr = sh(returnStdout: true, script: "curl --fail-with-body -X POST ${hostTestHelper}/server/serverBookingAuto?profiles=${PROFILE} ${checkHttpError}")
            }
            serverArrRest = serverArr
            serverArr = checkHttpStatus(serverArr, "ERROR-SERVER-BOOKING", "Stopping early. There are no available servers.")
            masterServer = serverArr.first()
            serverCnt = serverArr.size()
            if (!params.MASTER_RUN) serverCnt--

            // Расчет профиля нагрузки
            echo '-----------------CALCULATION PROFILE-----------------'
            profileArr = sh(returnStdout: true, script: "curl --fail-with-body -X GET ${hostTestHelper}/profile/profileCalculation?profileName=${PROFILE}\\&serverCount=${serverCnt} ${checkHttpError}")
            profileArr = checkHttpStatus(profileArr, "ERROR-PROFILE", "Stopping early. Error calculating the profile.")
            profileCnt = profileArr.size()

            // Выбор URL
            echo '-----------------URL SELECTING-----------------'
            if (!CUSTOM_URL) {
                urlRun = sh(returnStdout: true, script: "curl --fail-with-body -X GET ${hostTestHelper}/stand/get/url?standName=${STAND}\\&domainName=${DOMAIN} -H 'Content-Type: application/json' ${checkHttpError}")
                urlRun = checkHttpStatus(urlRun, "ERROR-URL", "Stopping early. Error selecting the URL.")
            } else {
                echo "Custom URL is selected: ${CUSTOM_URL}"
            }

            // Проверка ssh-ключей
            echo '-----------------CHECK SSH-KEYGEN-----------------'
            def srvMap = [:]
            for (s in serverArr) {
                def srv = s
                srvMap[srv] = {
                    sh "ssh-keygen -R ${srv}"
                    sh "ssh-keyscan ${srv} >> ~/.ssh/known_hosts"
                }
            }
            parallel srvMap
        }
        stage('Info About Test') {
            // Вывод информации о запуске
            echo '-----------------INFORMATION ABOUT TEST-----------------'
            def messageInfo = "Server:"
            def profileInfo = ""
            def profileInfoExporter = ""
            def jmxJmeterPort = jmxJmeterStartPort
            def jmxJmeterServerPort = jmxJmeterStartServerPort
            for (srv in serverArr) {
                if (srv == serverArr.first()) {
                    messageInfo += "\n\t${srv} - master"
                } else {
                    messageInfo += "\n\t${srv}"
                }
            }
            messageInfo += "\nProfile:"
            for (profile in profileArr) {
                messageInfo += "\n\t${profile}"
                profileInfo += "${profile[0]}--"
                if (params.JMX_EXPORTER_RUN) {
                    profileInfoExporter += "\n\t${profile}:\n\t\tjPort - ${jmxJmeterPort}\n\t\tjServerPort - ${jmxJmeterServerPort}"
                    jmxJmeterPort++
                    jmxJmeterServerPort++
                } else {
                    profileInfoExporter = "\n\tnot used"
                }
            }
            messageInfo = """
                |${messageInfo}
                |Parameters:
                |\tstand: ${STAND}
                |\turl: ${urlRun}
                |\tduration: ${DURATION}
                |\tmasterRun: ${MASTER_RUN}
                |\tcertificate: ${CERTIFICATE}
                |\tcustomParamRun: ${CUSTOM_PARAM_RUN}
                |\tcustomParamServer: ${CUSTOM_PARAM_SERVER}
                |\tgitBranch: ${BRANCH}
                |JxmExporterInfo: ${profileInfoExporter}
            """.stripMargin()

            echo "${messageInfo}"

            currentBuild.displayName = "${env.BUILD_ID}--${STAND}--${profileInfo}"
            currentBuild.description = "${messageInfo}"
        }
        stage('Check Server Is Busy') {
            // Проверка что сервер свободен для запуска теста
			echo '-----------------CHECK SERVER IS BUSY-----------------'
			sshagent(["${credentialSSH}"]) {
				for (srv in serverArr) {
					def checkServer = sh(returnStatus: true, script: "ssh ${userSSH}@${srv} 'ps -ef | grep ApacheJMeter.jar | grep -v grep > /dev/null'") == 0
					if (checkServer) {
						serverBusyArr += srv
					}
				}
				if (serverBusyArr) {
					def serverFree = serverArr - serverBusyArr
					serverFree = serverFree.join('\n\t')
					serverBusyArr = serverBusyArr.join('\n\t')
					echo """
						|Busy server: \n\t${serverBusyArr}
						|Free server: \n\t${serverFree}
					""".stripMargin()

                    statusJob = 'ERROR-SERVER-BUSY'
					error('Stopping early. The server on which Jmeter is already running is selected.')
				}
			}
        }
        stage('Checkout') {
            echo '-----------------GIT CHECKOUT-----------------'
            checkout changelog: false, poll: false, scm: scmGit(
                branches: [[name: "${BRANCH}"]],
                    extensions: [cloneOption(depth: 1, noTags: true, reference: '', shallow: true)],
                    userRemoteConfigs: [[
                    credentialsId: "${gitCredentialsId}",
                        url: "${gitRepository}"
                        ]])
        }
        stage('Copying Files') {
            sshagent(["${credentialSSH}"]) {
                // Копирование и редактирование файлов на сервера
                echo '-----------------COPYING FILES-----------------'
                def srvMap = [:]
                for (s in serverArr) {
                    def srv = s
                    srvMap[srv] = {
                        sh "ssh ${userSSH}@${srv} 'cd ${homeDir}; rm -r ${workDir}; mkdir ${workDir}'"
                        for (profile in profileArr) {
                            def profileName = profile[0]
                            sh "ssh ${userSSH}@${srv} 'mkdir ${homeDir}/${workDir}/${profileName}'"
                            sh "scp ./${gitPathScript}/${profileName}.zip ${userSSH}@${srv}:${homeDir}/${workDir}/${profileName}"
                            sh "ssh ${userSSH}@${srv} 'cd ${homeDir}/${workDir}/${profileName}; unzip ${profileName}.zip; mv ${profileName}.jmx ${profileName}-${STAND}.jmx'"
                            if (CERTIFICATE) {
                                sh "scp ./${gitPathCertificate}/${CERTIFICATE} ${userSSH}@${srv}:${homeDir}/${workDir}/${profileName}"
                            }
                        }
                    }
                }
                parallel srvMap
            }
        }
        stage('Start Jmeter Server') {
            sshagent(["${credentialSSH}"]) {
                // Запуск jmeter-server на серверах
                echo '-----------------START JMETER-SERVER-----------------'
                statusJob = 'ERROR-START-JMETER-SERVER'
                def portJmeter = 1099
                def paramServerRun = ''
                def portJmxExporter = ''
                if (CERTIFICATE) paramServerRun += "-Djavax.net.ssl.keyStore=${CERTIFICATE} "
                if (CERTIFICATE_P) paramServerRun += "-Djavax.net.ssl.keyStorePassword=${CERTIFICATE_P} "
                if (CUSTOM_PARAM_SERVER) paramServerRun += "${CUSTOM_PARAM_SERVER}"

                for (profile in profileArr) {
                    def profileName = profile[0]
                    def srvMap = [:]
                    if (params.JMX_EXPORTER_RUN) {
                        portJmxExporter = jmxJmeterStartServerPort + " ${hostSDServer}" + " -t ${profileName}-${STAND}.jmx"
                    }
                    for (s in serverArr) {
                        def srv = s
                        srvMap[srv] = {
                            sh "ssh ${userSSH}@${srv} 'cd ${homeDir}/${workDir}/${profileName}; SERVER_PORT=${portJmeter} nohup ${jmeterDir}/${jmeterServer} ${portJmxExporter} ${paramServerRun} > /dev/null 2>&1&'"
                        }
                    }
                    parallel srvMap

                    portJmeter++
                    jmxJmeterStartServerPort++
                }
                sleep 10
            }
        }
        stage('Start Test') {
            sshagent(["${credentialSSH}"]) {
                // Запуск jmeter на серверах
                echo '-----------------START TEST-----------------'
                statusJob = 'ERROR-START-TEST'
                def portJmeter = 1099
                def paramRun = ''
                def portJmxExporter = ''
                def serverStr = serverArr
                if (CUSTOM_PARAM_RUN) paramRun += "${CUSTOM_PARAM_RUN}"
                if (!params.MASTER_RUN) serverStr.remove(0)
                serverStr = serverStr.join(',')
                for (profile in profileArr) {
                    if (params.JMX_EXPORTER_RUN) {
                        portJmxExporter = jmxJmeterStartPort + " ${hostSDServer}"
                    }
                    def serverStart = serverStr.replace(',', ":${portJmeter},") + ":${portJmeter}"
                    def profileName = profile[0]
                    def throughput = profile[1]
                    def threads = profile[2]
                    def rampUp = profile[3]
                    sh "ssh ${userSSH}@${masterServer} 'cd ${homeDir}/${workDir}/${profileName}; nohup ${jmeterDir}/${jmeter} ${portJmxExporter} -X -n -t ${profileName}-${STAND}.jmx -R ${serverStart} -Dmode=StrippedAsynch -DdataSourceBL=${dataSourceBL} -Gthroughput=${throughput} -Gthreads=${threads} -GrampUp=${rampUp} -Gduration=${DURATION} -Gurl=${urlRun} ${paramRun} > /dev/null 2>&1&'"
                    portJmeter++
                    jmxJmeterStartPort++
                }
                sleep 60
            }
        }
        stage('Check Test') {
            sshagent(["${credentialSSH}"]) {
                echo '-----------------CHECK TEST-----------------'
                // Проверка что на master-сервере процесс не упал с ошибкой
                def countProcessJmeter = sh(returnStdout: true, script: "ssh ${userSSH}@${masterServer} 'ps -ef | grep ApacheJMeter.jar | grep -v grep | wc -l'").trim()
                if (countProcessJmeter.toInteger() != profileCnt * 2) {
                    statusJob = 'ERROR-JMETER-FAILED'
                    error("Stopping early. One or more jmeter processes failed with an error. Number of jmeter processes: ${countProcessJmeter} != ${profileCnt * 2}.")
                }
                
                // Создание теста
                echo '-----------------CREATING TEST-----------------'
                def body = """{
\t"stand": "${STAND}",
\t"duration": ${DURATION},
\t"server": ${serverArrRest},
\t"profile": "${PROFILE}"
}
"""
                testId = sh(returnStdout: true, script: "curl --fail-with-body -X POST ${hostTestHelper}/test/create -H 'Content-Type: application/json' -d '${body}' ${checkHttpError}")
                testId = checkHttpStatus(testId, "ERROR-CREATING-TEST", "Stopping early. Error creating the test.")

                // Информация о тесте
                echo '-----------------TEST INFO-----------------'
                def testInfo = sh(returnStdout: true, script: "curl --fail-with-body -X GET ${hostTestHelper}/test/info/${testId} -H 'Content-Type: application/json' ${checkHttpError}")
                testInfo = checkHttpStatus(testInfo, "ERROR-TEST-INFO", "Stopping early. Error creating the test.")
                resultJob = testId
                statusJob = 'RUN'
            }
        }
    }  catch (e) {
        echo "Failed because of {$e}"
    } finally {
        sshagent(["${credentialSSH}"]) {
            echo '-----------------CHECK STATUS JOB-----------------'
            writeFile file: 'resultJob.txt', text: "${resultJob}"
            archiveArtifacts artifacts: 'resultJob.txt'
            switch (statusJob) {
                case 'RUN':
                    echo "The test was successfully launched!\nTest id - ${testId}"
                    break
                case 'ERROR-SERVER-BOOKING':
                    echo "The test failed with an error. Status Job = ${statusJob}"
                    break
                case ['ERROR', 'ERROR-PROFILE', 'ERROR-URL', 'ERROR-SERVER-BUSY']:
                    echo "The test failed with an error. Status Job = ${statusJob}"
                    def servers = serverArr.join(',')
                    sh(returnStdout: true, script: "curl --fail-with-body -X POST ${hostTestHelper}/server/serverBooking/cancel?server=${servers} ${checkHttpError}")
                    break
                case ['ERROR-START-JMETER-SERVER', 'ERROR-START-TEST', 'ERROR-JMETER-FAILED']:
                    echo "The test failed with an error. Status Job = ${statusJob}"
                    def servers = serverArr.join(',')
                    sh(returnStdout: true, script: "curl --fail-with-body -X POST ${hostTestHelper}/server/serverBooking/cancel?server=${servers} ${checkHttpError}")
                    stopJmeter(userSSH, jmeterDir, profileCnt, serverArr, masterServer)
                    break
                case 'ERROR-CREATING-TEST':
                    echo "The test failed with an error. Status Job = ${statusJob}"
                    def servers = serverArr.join(',')
                    sh(returnStdout: true, script: "curl --fail-with-body -X POST ${hostTestHelper}/server/serverBooking/cancel?server=${servers} ${checkHttpError}")
                    stopJmeter(userSSH, jmeterDir, profileCnt, serverArr, masterServer)
                    break
                case 'ERROR-TEST-INFO':
                    echo "The test failed with an error. Status Job = ${statusJob}"
                    sh(returnStdout: true, script: "curl --fail-with-body -X DELETE ${hostTestHelper}/test/delete/${testId} ${checkHttpError}")
                    stopJmeter(userSSH, jmeterDir, profileCnt, serverArr, masterServer)
                    break
                default:
                    echo "The end. There is no handler for the status. Status Job = ${statusJob ?: 'NULL'}"
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
        resultJob = status
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

// Остановка процессов Jmeter
def stopJmeter(userSSH, jmeterDir, profileCnt, serverArr, masterServer) {
    // Завершение теста
    echo '-----------------STOP TEST-----------------'
    def portJmeter = 4445
    for (def i=0; i < profileCnt; i++) {
        sh "ssh ${userSSH}@${masterServer} '${jmeterDir}/stoptest.sh ${portJmeter}'"
        portJmeter += 1
    }
    def tryCount = 0
    while (tryCount < 5) {
        sleep 10
        def countProcessJmeter = sh(returnStdout: true, script: "ssh ${userSSH}@${masterServer} 'ps -ef | grep ApacheJMeter.jar | grep -v grep | wc -l'").trim()
        if (countProcessJmeter.toInteger() <= 0) {
            echo "Jmeter has successfully completed its work. Number of jmeter processes: ${countProcessJmeter}."
            break
        }
        tryCount++

        if (tryCount >= 5) {
            // Остановка процессов связанных с запуском теста
            echo '-----------------KILL JMETER-----------------'
            def srvMap = [:]
            for (s in serverArr) {
                def srv = s
                srvMap[srv] = {
                    sh returnStatus: true, script: "ssh ${userSSH}@${srv} 'pkill -9 -f ApacheJMeter.jar'"
                }
            }
            parallel srvMap

            echo "The number of retries has ended. Forced termination of processes has been performed!"
            break
        }
    }
}
