def parentstageName = "Integration"

stage "${parentstageName}::DeployIntegrationEnv"
subJob = build  job: '../../wppCommon/subJobs/PiDeploy',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
                [$class: 'StringParameterValue', name: 'parentstageName', value: parentstageName ],
                ];