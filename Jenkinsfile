pipeline {
    agent any

    options { disableConcurrentBuilds() }

    triggers {
        githubPush()
    }

    stages {
        stage('Build') {
            steps {
                sh 'make build-no-tests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'make unit-tests'
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'make integration-tests'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo "Branch ${env.BRANCH_NAME}"
                    if (env.BRANCH_NAME == 'master') {
                        echo 'Deploy to PROD'
                        sh 'make deploy'
                    }
                }
            }
        }
    }
}
