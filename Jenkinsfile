pipeline {

    agent any

    stages {

        stage('Get code') {
            steps {
               // Get some code from a GitHub repository
               git 'https://github.com/grauds/clematis.skos.parser.git'
               sh 'chmod +x gradlew'
            }
        }

        stage('Gradle build') {
            steps {
              sh './gradlew clean build'
            }

        }

        stage ('Dependency-Check') {
            steps {
                dependencyCheck additionalArguments: '''
                    -o "./"
                    -s "./"
                    -f "ALL"
                    --prettyPrint''', odcInstallation: 'Dependency Checker'

                dependencyCheckPublisher pattern: 'dependency-check-report.xml'
            }
        }

        stage('Publish tests') {
            steps {
                recordCoverage(tools: [[parser: 'JACOCO']],
                        id: 'jacoco', name: 'JaCoCo Coverage',
                        sourceCodeRetention: 'EVERY_BUILD',
                        qualityGates: [
                                [threshold: 60.0, metric: 'LINE', baseline: 'PROJECT', unstable: true],
                                [threshold: 60.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: true]])
            }
        }

    }

    post {
        always {
            junit '**/build/**/test-results/test/*.xml'
        }
    }
}
