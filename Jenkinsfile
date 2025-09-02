@Library('ami-lib') _

node {
  stage('Build AMI') {
    def amiId = buildAmi(
      // Omit repo/branch if this Jenkinsfile sits in the same repo as template.json
      // repo: 'https://github.com/your-org/your-app-repo.git',
      // branch: '*/main',
      template: 'template.json',
      region: 'ap-south-1',
      credsId: 'aws-cred'
      // vars: [ any_extra_var: 'value' ] // if your template expects more -var
    )
    echo "AMI built: ${amiId}"
  }
}
