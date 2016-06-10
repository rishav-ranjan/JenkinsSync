//incoming parameters - gitCredentials, gitServiceURL, gitLoadTestURL, gitDeployURL, buildTrigger

def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"

def amiID
def serviceCommitID
def loadTestCommitID

stage "CommitStage"

parallel(firstTask: {
    subJob1 = build  job: '../subJobs/serviceCommit',
                parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'gitServiceURL', value: gitServiceURL ],
                        [$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                ];
	//returned values
    amiID = subJob1.description.tokenize('#')[0].tokenize('=')[1]
    serviceCommitID = subJob1.description.tokenize('#')[1].tokenize('=')[1]
    }, 
    secondTask: {
    subJob2 = build job: '../subJobs/microLoadCommit',
                parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'gitLoadTestURL', value: gitLoadTestURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                ];
	//returned values
    artifactURL = subJob2.description.tokenize('#')[0].tokenize('=')[1]
    loadTestCommitID = subJob2.description.tokenize('#')[2].tokenize('=')[1]
    })
stage "AcceptanceStage"
job2 = build  job: '../subJobs/microLoadAcceptance',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
				[$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
				[$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                ];

stage "IntegrationStage"
job3 = build  job: '../subJobs/IntegrationStage',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
				[$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
				[$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                ];
stage "ReleaseStage"
    
