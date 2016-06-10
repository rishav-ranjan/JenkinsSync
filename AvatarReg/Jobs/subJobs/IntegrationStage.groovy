//incoming parameters - gitCredentials, serviceName, amiID, gitDeployURL, serviceConfigPath

def parentstageName = "Integration"
def deployAmiTargetNode = 'AMIBuilder'

stage "${parentstageName}::DeployIntegrationEnv"
subJob = build  job: '../../wppCommon/subJobs/PiDeploy',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
				[$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
                [$class: 'StringParameterValue', name: 'parentstageName', value: parentstageName ],
				[$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
				[$class: 'StringParameterValue', name: 'deployAmiTargetNode', value: deployAmiTargetNode ],
                ];