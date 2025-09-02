@Library('ami-lib@main') _

pipeline {
    agent any
    stages {
        stage('Build AMI') {
            steps {
                script {
                    buildAmi(
                        repo: 'https://github.com/snaatak-deepak/Scripted-Pipeline-AMI.git',
                        branch: 'main',
                        credentialsId: 'aws-cred',
                        region: 'us-east-1'
                    )
                }
            }
        }
    }
}
