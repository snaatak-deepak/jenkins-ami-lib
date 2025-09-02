def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Checkout') {
                steps {
                    git branch: (config.branch ?: 'main'),
                        url: (config.repo ?: 'https://github.com/snaatak-deepak/Scripted-Pipeline-AMI.git')
                }
            }

            stage('Validate Packer Template') {
                steps {
                    sh "packer validate template.json"
                }
            }

            stage('Build AMI') {
                steps {
                    script {
                        def region = config.region ?: 'us-east-1'
                        def creds = config.credentialsId ?: 'aws-cred'

                        withEnv(["AWS_REGION=${region}"]) {
                            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                              credentialsId: creds]]) {
                                sh "packer build -var 'aws_region=${region}' template.json"
                            }
                        }
                    }
                }
            }
        }
    }
}
