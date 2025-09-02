def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            AWS_REGION = config.region ?: 'us-east-1'
        }

        stages {
            stage('Checkout') {
                steps {
                    git branch: config.branch ?: 'main',
                        url: config.repo ?: 'https://github.com/snaatak-deepak/Scripted-Pipeline-AMI.git'
                }
            }

            stage('Validate Packer Template') {
                steps {
                    sh "packer validate template.json"
                }
            }

            stage('Build AMI') {
                steps {
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                      credentialsId: config.credentialsId ?: 'aws-cred']]) {
                        sh "packer build -var 'aws_region=${AWS_REGION}' template.json"
                    }
                }
            }
        }
    }
}
