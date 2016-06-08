//global variables

//def publishRepoURL = "http://52.1.56.86/nexus/content/repositories/"
//def gitDeployURL = "https://github.com/WPPg2/DevOps-Deployment"
def parentstageName = "Commit"
//service variables
//def serviceName = "avreg"
//def gitServiceURL = "https://github.com/WPPg2/avatar-reg"
//def gitServiceCredentials = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
//def buildTrigger = "NIGHTLY"
//def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"
//def cookbookVersion = "1.0.13"

//userjob variables
def buildPublishTargetNode = 'Morpheus'
def deployAmiTargetNode = 'AMIBuilder'



//stage 
stage "${parentstageName}::BuildAndPublish"

    subJob1 = build  job: '../../wppCommon/subJobs/buildAndPublish',
                    parameters: [
                        [$class: 'StringParameterValue', name: 'credentialsId', value: gitServiceCredentials ],
                        [$class: 'StringParameterValue', name: 'url', value: gitServiceURL ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'repoURL', value: publishRepoURL ],
                        [$class: 'StringParameterValue', name: 'targetNode', value: buildPublishTargetNode ],
                    ] ;
    
    // Return Values
    def artifactURL = subJob1.description.tokenize('#')[0].tokenize('=')[1]
    def artifactVersion = subJob1.description.tokenize('#')[1].tokenize('=')[1]
    def commitID = subJob1.description.tokenize('#')[2].tokenize('=')[1]

    
stage "${parentstageName}::Deploy"
    subJob2 = build  job: '../../wppCommon/subJobs/deploy',
                    parameters: [
                        [$class: 'StringParameterValue', name: 'credentialsId', value: gitServiceCredentials ],
                        [$class: 'StringParameterValue', name: 'url', value: gitDeployURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                        [$class: 'StringParameterValue', name: 'artifactURL', value: artifactURL ],
                        [$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
                    ] ;
                    
    // Return Values                
    def instanceID = subJob2.description.tokenize('#')[0].tokenize('=')[1]
    println "the values are ${artifactURL}\t${artifactVersion}\t${instanceID}"
    
    
    
stage "${parentstageName}::CreateAMI"
    subJob3 = build  job: '../../wppCommon/subJobs/createAMI',
                     parameters: [
                        [$class: 'StringParameterValue', name: 'credentialsId', value: gitServiceCredentials ],
                        [$class: 'StringParameterValue', name: 'url', value: gitDeployURL ],
                        [$class: 'StringParameterValue', name: 'artifactVersion', value: artifactVersion ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                        [$class: 'StringParameterValue', name: 'instanceID', value: instanceID ],
                        [$class: 'StringParameterValue', name: 'commitID', value: commitID ],
                        [$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
                    ] ;
    // Return Values                
    def amiID = subJob3.description.tokenize('#')[0].tokenize('=')[1]
currentBuild.setDescription("#amiID="+amiID+"#commitID="+commitID)

    