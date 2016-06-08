def publishRepoURL = "http://52.1.56.86/nexus/content/repositories/" 
def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"

def amiID
def serviceCommitID
def loadTestCommitID
def cookbookVersion = "1.0.13"

stage "CommitStage"
parallel(firstTask: {
    subJob1 = build  job: '../subJobs/serviceCommit',
                parameters: [
                        [$class: 'StringParameterValue', name: 'gitServiceCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'gitServiceURL', value: gitServiceURL ],
                        [$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'publishRepoURL', value: publishRepoURL ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                ];
    amiID = subJob1.description.tokenize('#')[0].tokenize('=')[1]
    serviceCommitID = subJob1.description.tokenize('#')[1].tokenize('=')[1]
    }, 
    secondTask: {
    subJob2 = build job: '../subJobs/loadTestCommit',
                parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'gitLoadTestURL', value: gitLoadTestURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'publishRepoURL', value: publishRepoURL ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                ];
    artifactURL = subJob2.description.tokenize('#')[0].tokenize('=')[1]
    loadTestCommitID = subJob2.description.tokenize('#')[2].tokenize('=')[1]
    })
stage "AcceptanceStage"
subJob2 = build  job: '../subJobs/AcceptanceStage',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
                ];

stage "IntegrationStage"
subJob3 = build  job: '../subJobs/IntegrationStage',
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
                ];
stage "ReleaseStage"
    