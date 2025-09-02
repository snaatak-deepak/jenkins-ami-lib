/**
 * Reusable step to build an AMI with Packer.
 *
 * Params (all optional except template/region/credsId depending on your setup):
 *  repo        - Git repo to checkout (omit to use current workspace)
 *  branch      - Branch pattern, default '*/main'
 *  template    - Packer template file, default 'template.json'
 *  region      - AWS region, default 'ap-south-1'
 *  credsId     - Jenkins AWS credentialsId, default 'aws-cred'
 *  packerPluginPath - Override PACKER_PLUGIN_PATH if needed
 *  vars        - Map of additional -var key/values to pass to packer
 *
 * Returns: AMI ID as String, sets env.BUILT_AMI_ID and archives packer.out + built-ami-id.txt
 */
def call(Map cfg = [:]) {
  def repo    = cfg.get('repo', null)
  def branch  = cfg.get('branch', '*/main')
  def template= cfg.get('template', 'template.json')
  def region  = cfg.get('region', 'ap-south-1')
  def credsId = cfg.get('credsId', 'aws-cred')
  def pluginP = cfg.get('packerPluginPath', "${env.HOME}/.packer.d/plugins")
  def extra   = (cfg.get('vars', [:]) ?: [:]) as Map

  if (repo) {
    echo "Checking out ${repo} (${branch})..."
    checkout([$class: 'GitSCM',
      branches: [[name: branch]],
      userRemoteConfigs: [[url: repo]]
    ])
  } else {
    echo "Using existing workspace (no repo param provided)."
  }

  echo "Validating packer template: ${template}"
  sh "packer validate ${template}"

  // Build the -var args from the map
  def varArgs = extra.collect { k, v -> "-var '${k}=${v}'" }.join(' ')

  withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: credsId]]) {
    sh """
      set -euo pipefail
      export AWS_DEFAULT_REGION='${region}'
      export PACKER_PLUGIN_PATH='${pluginP}'
      # machine-readable makes it easier to parse AMI ID reliably
      packer build -machine-readable -color=false \
        -var 'aws_region=${region}' \
        ${varArgs} \
        ${template} | tee packer.out
    """
  }

  // Parse AMI ID from machine-readable output: artifact,...,id,REGION:ami-xxxxxxxx
  def amiId = sh(
    returnStdout: true,
    script: "awk -F, '/artifact,.*id/ {split(\$NF,a,\":\"); print a[2]}' packer.out | tail -1"
  ).trim()

  if (!amiId) {
    error "AMI ID not found in packer.out. Check the build logs."
  }

  writeFile file: 'built-ami-id.txt', text: amiId
  archiveArtifacts artifacts: 'packer.out,built-ami-id.txt', fingerprint: true

  env.BUILT_AMI_ID = amiId
  currentBuild.description = "AMI: ${amiId}"
  echo "✅ Built AMI: ${amiId}"
  return amiId
}
