pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk 'JDK17'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                bat 'mvn clean'
            }
        }

        stage('Run Automation Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Generate Allure Results') {
            steps {
                bat 'allure generate target/allure-results --clean -o target/allure-report'
            }
        }
    }

    post {

        always {

            archiveArtifacts artifacts: '''
target/allure-report/**,
target/logs/**,
performance/results/**
''', fingerprint: true

            allure([
                includeProperties: false,
                jdk: '',
                results: [[path: 'target/allure-results']]
            ])
        }

        success {
            echo 'Automation Framework Execution Successful'
        }

        failure {
            echo 'Automation Framework Execution Failed'
        }
    }
}