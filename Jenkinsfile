@Library('ami-lib') _

pipeline {
  agent any
  stages {
    stage('Build AMI') {
      steps {
        script {
          def amiId = buildAmi(
            template: 'template.json',
            region: 'ap-south-1',
            credsId: 'aws-cred'
          )
          echo "AMI built: ${amiId}"
        }
      }
    }
  }
}
