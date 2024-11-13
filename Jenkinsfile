pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'SonarQube'
        SONAR_TOKEN = credentials('SonarToken')
        DOCKER_CREDENTIALS_ID = 'DOCKER'

    }

    stages {

        stage('GIT') {
            steps {
                echo 'Pulling from Git...'
                git branch: 'AchrefChaabani-5SIM4-G2',
                    url: 'https://github.com/Anas-REBAI/5SIM4_G2_Projet.git'
            }
        }

        stage('COMPILING') {
            steps {
                script {
                    // Clean and install dependencies
                    sh 'mvn clean install'
                    // Uncomment these lines if you want to run tests and package the application
                    // sh 'mvn test'
                    // sh 'mvn package'
                }
            }
        }

        stage('SONARQUBE') {
            steps {
                script {
                    withSonarQubeEnv("${SONARQUBE_ENV}") {
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=/target/site/jacoco/jacoco.xml
                        """
                    }
                }
            }
        }
        stage('NEXUS') {
            steps {
                script {
                    echo "Deploying to Nexus..."

                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: "192.168.50.4:8081", // Updated Nexus URL based on previous info
                        groupId: 'tn.esprit.spring',
                        artifactId: 'gestion-station-ski',
                        version: '1.0',
                        repository: "maven-releases", // Based on previous Nexus repo
                        credentialsId: "NEXUS", // Using your stored Nexus credentials
                        artifacts: [
                            [
                                artifactId: 'gestion-station-ski',
                                classifier: '',
                                file: '/var/lib/jenkins/workspace/Achref pipeline/target/gestion-station-ski-1.0.jar', // Relative path from workspace
                                type: 'jar'
                            ]
                        ]
                    )

                    echo "Deployment to Nexus completed!"
                }
            }
        }
        stage('Building image') {
            steps {
                script {
                    echo 'Building Docker image...'
                    sh 'docker build -t chaabaniachref/gestion-station-ski:1.0 .'
                }
            }

        }
         stage('Verify Image') {
             steps {
                 script {
                     sh 'docker images | grep chaabaniachref/gestion-station-ski'
                 }
             }
         }

        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_CREDENTIALS', usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PASS')]) {
                        sh 'docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASS'
                        sh 'docker push chaabaniachref/gestion-station-ski:1.0'
                    }
                }
            }
        }

         stage('Docker Compose Up') {
                    steps {
                        script {
                            echo 'Starting services with Docker Compose...'
                            sh 'docker compose up -d'
                        }
                    }
         }


        stage("Mail Notification") {
            steps {
                script {
                    def sonarQubeUrl = 'http://192.168.50.4:9000/dashboard?id=tn.esprit.spring%3Agestion-station-ski'
                    def prometheusUrl = 'http://192.168.50.4:8082/api/actuator/prometheus'
                    def grafanaDashboardUrl = 'http://192.168.50.4:3000/d/de3h2nhuw3zswa/jvm-micrometer?from=now-24h&to=now&timezone=browser&var-application=&var-instance=192.168.50.4:8082&var-jvm_memory_pool_heap=$__all&var-jvm_memory_pool_nonheap=$__all&var-jvm_buffer_pool=$__all&refresh=5s'

                    def emailBody = """
                        <html>
                        <body style="background-color: #f4f4f9; font-family: Arial, sans-serif; padding: 20px;">
                            <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
                                <h2 style="color: #333;">Job Notification: ${env.JOB_NAME}</h2>
                                <p style="color: #555; font-size: 16px;">Hello,</p>
                                <p style="color: #555; font-size: 16px;">
                                    The Jenkins job <strong>${env.JOB_NAME}</strong> with build number <strong>${env.BUILD_NUMBER}</strong> has completed with status: <strong>${currentBuild.currentResult}</strong>.
                                </p>

                                <div style="text-align: center; margin-top: 20px;">
                                    <a href="${env.BUILD_URL}" style="text-decoration: none; color: #fff; background-color: #28a745; padding: 10px 20px; border-radius: 4px; display: inline-block; font-weight: bold;">View Build Details</a>
                                </div>

                                <div style="text-align: center; margin-top: 20px;">
                                    <a href="${sonarQubeUrl}" style="text-decoration: none; color: #fff; background-color: #007bff; padding: 10px 20px; border-radius: 4px; display: inline-block; font-weight: bold;">View SonarQube Report</a>
                                </div>

                                <div style="text-align: center; margin-top: 20px;">
                                    <a href="${prometheusUrl}" style="text-decoration: none; color: #fff; background-color: #ff5722; padding: 10px 20px; border-radius: 4px; display: inline-block; font-weight: bold;">View Prometheus Metrics</a>
                                </div>

                                <div style="text-align: center; margin-top: 20px;">
                                    <a href="${grafanaDashboardUrl}" style="text-decoration: none; color: #fff; background-color: #673ab7; padding: 10px 20px; border-radius: 4px; display: inline-block; font-weight: bold;">View Grafana Dashboard</a>
                                </div>

                                <p style="color: #555; font-size: 14px; margin-top: 20px;">Thank you for your attention!</p>
                                <p style="color: #555; font-size: 14px;">Best regards,<br>Your Jenkins CI/CD</p>
                            </div>
                        </body>
                        </html>
                    """

                    // Send the email notification
                    emailext(
                        to: 'achref.chaabani@esprit.tn',
                        subject: "Job Notification: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: emailBody,
                        mimeType: 'text/html'
                    )
                }
            }
        }
    }

    post {
        success {
            emailext(
                to: 'achref.chaabani@esprit.tn',
                subject: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' Succeeded",
                body: """
                    <html>
                    <body style="background-color: #e0f7e7; font-family: Arial, sans-serif; padding: 20px;">
                        <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
                            <h2 style="color: #28a745;">Success: ${env.JOB_NAME}</h2>
                            <p style="color: #555; font-size: 16px;">Hello,</p>
                            <p style="color: #555; font-size: 16px;">
                                The Jenkins job <strong>${env.JOB_NAME}</strong> has successfully completed.
                            </p>

                            <div style="text-align: center; margin-top: 20px;">
                                <a href="${env.BUILD_URL}" style="text-decoration: none; color: #fff; background-color: #28a745; padding: 10px 20px; border-radius: 4px; display: inline-block; font-weight: bold;">View Build Details</a>
                            </div>
                        </div>
                    </body>
                    </html>
                """,
                mimeType: 'text/html'
            )
        }

        failure {
            emailext(
                to: 'achref.chaabani@esprit.tn',
                subject: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' Failed",
                body: """
                    <html>
                    <body style="background-color: #f8d7da; font-family: Arial, sans-serif; padding: 20px;">
                        <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
                            <h2 style="color: #dc3545;">Failure: ${env.JOB_NAME}</h2>
                            <p style="color: #555; font-size: 16px;">Hello,</p>
                            <p style="color: #555; font-size: 16px;">
                                Unfortunately, the Jenkins job <strong>${env.JOB_NAME}</strong> has failed. Please check the logs.
                            </p>

                            <div style="text-align: center; margin-top: 20px;">
                                <a href="${env.BUILD_URL}" style="text-decoration: none; color: #fff; background-color: #dc3545; padding: 10px 20px; border-radius: 4px; display: inline-block; font-weight: bold;">View Build Details</a>
                            </div>
                        </div>
                    </body>
                    </html>
                """,
                mimeType: 'text/html'
            )
        }
    }
}