def call(Map config = [:]) {
    node {
        stage('Checkout') {
            checkout([$class: 'GitSCM',
                branches: [[name: config.branch ?: '*/main']],
                userRemoteConfigs: [[url: config.repo ?: 'https://github.com/snaatak-deepak/Scripted-Pipeline-AMI.git']]
            ])
        }

        stage('Validate Packer Template') {
            sh "packer validate template.json"
        }

        stage('Build AMI') {
            script {
                def region = config.region ?: 'us-east-1'
                def creds  = config.credentialsId ?: 'aws-cred'

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
