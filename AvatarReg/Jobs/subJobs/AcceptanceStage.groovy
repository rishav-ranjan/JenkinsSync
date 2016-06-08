def parentstageName = "Acceptance"

stage "${parentstageName}::DeployLoadTestEnv"
subJob = build  job: '../../wppCommon/subJobs/PiDeploy',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'parentstageName', value: parentstageName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
                ];
