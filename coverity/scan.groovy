pipeline {
    agent {
        docker {
            image 'plavjanik/coverity-oss'
            label 'docker'
            args '-v "$HOME"/.gradle:/root/.gradle'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    }

    stages {
        stage('Coverity') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'coverityScanZoweSampleSpringBootApiService',
                        usernameVariable: 'COVERITY_EMAIL',
                        passwordVariable: 'COVERITY_TOKEN')]) {
                    sh 'scripts/coverity-scan.sh'
                }
            }
        }
    }
}
