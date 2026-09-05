import groovy.transform.Field

// Статус задания
@Field
def statusJob = 'ERROR'
// Массив не найденных testId
@Field
def testNotFoundArr = []
// Массив не удаленных testId
@Field
def testNotDeleteArr = []

node {
    // environment
    // Пользователь для подключения по ssh
    def userSSH = 'user'
    // Сredential для подключения по ssh
    def credentialSSH = 'credential-ssh'
    // Корневая директория jmeter
    def jmeterDir = '/opt/jmeter/bin'
    // Временная директория из которой будет запускаться тест
    def workDir = 'jmeterRun'
    // Адрес сервера с приложением load-test-helper
    def hostTestHelper = '{host}:8088'
    // Шаблон для проверки http-запросов
    def checkHttpError = "|| echo ' --> ERROR'"

    // Массив тестов
    def testIdArr = []

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
                    name: 'TEST_ID',
                    description: 'Id / список id тестов (через запятую). Пример: 10,20,30',
                    trim: true)
                ])])

        // environment
        testIdArr = TEST_ID.split(',')
        
        stage('Stop Test') {
            sshagent(["${credentialSSH}"]) {
                // Очистка WORKSPACE
                echo '-----------------DELETING WORKSPACE-----------------'
                deleteDir()

                // Удаление теста / Остановка процессов Jmeter
                echo '-----------------STOP TEST-----------------'
                for (testId in testIdArr) {
                    def test = sh(returnStdout: true, script: "curl --fail-with-body -X GET ${hostTestHelper}/test/info/${testId} -H 'Content-Type: application/json' ${checkHttpError}")
                    test = checkHttpStatus(test, 'INFO', testId)

                    if (test) {
                        // Проверка ssh-ключей
                        echo '-----------------CHECK SSH-KEYGEN-----------------'
                        def srvMap = [:]
                        for (s in test.server) {
                            def srv = s
                            srvMap[srv] = {
                                sh "ssh-keygen -R ${srv}"
                                sh "ssh-keyscan ${srv} >> ~/.ssh/known_hosts"
                            }
                        }
                        parallel srvMap

                        // Остановка процессов Jmeter / Удаление теста
                        echo '-----------------STOP TEST-----------------'
                        stopJmeter(userSSH, jmeterDir, test.profile.size(), test.server, test.server[0])

                        def deleteTest = sh(returnStdout: true, script: "curl --fail-with-body -X DELETE ${hostTestHelper}/test/delete/${testId} ${checkHttpError}")
                        deleteTest = checkHttpStatus(deleteTest, 'DELETE', testId)

                        echo "Test deleted: ${test.id}\nThe processes were stopped on the servers: ${test.server}"
                    }
                }
                if (testNotFoundArr || testNotDeleteArr) {
                    statusJob = 'WARN'
                    currentBuild.result = 'UNSTABLE'
                } else {
                    statusJob = 'SUCCESS'
                }
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
                    break
                case 'WARN':
                    echo "Warning!\nSome requests failed:\n\ttestNotFound: ${testNotFoundArr}\n\ttestNotDelete: ${testNotDeleteArr}"
                    break
                default:
                    echo "The end. There is no handler for the status. Status Job = ${statusJob ?: 'NULL'}"
            }
        }
    }
}


// function
// Проверка выполнения http-запроса
def checkHttpStatus(connection, stage, TEST_ID) {
    switch (stage) {
        case 'INFO':
            if (connection.contains('ERROR')) {
                echo "${connection}"
                testNotFoundArr.add(TEST_ID)
                return false
            } else {
                echo "Success response --> ${connection}"
                connection = new groovy.json.JsonSlurper().parseText(connection)
                return new HashMap(connection)
            }
            break
        case 'DELETE':
            if (connection.contains('ERROR')) {
                echo "${connection}"
                testNotDeleteArr.add(TEST_ID)
                return false
            } else {
                echo "Success response"
                return true
            }
            break
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
