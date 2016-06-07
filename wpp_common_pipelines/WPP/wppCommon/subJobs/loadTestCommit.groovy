//global variables

//def publishRepoURL = "http://52.1.56.86/nexus/content/repositories/"
//def gitDeployURL = "https://github.com/WPPg2/DevOps-Deployment"
def rootPomPath="LoadTests/${serviceName}"
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

    subJob1 = build  job: 'buildAndPublish2',
                    parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentialsId', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'url', value: gitLoadTestURL ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'repoURL', value: publishRepoURL ],
                        [$class: 'StringParameterValue', name: 'targetNode', value: buildPublishTargetNode ],
                        [$class: 'StringParameterValue', name: 'rootPomPath', value: rootPomPath ],
                    ] ;
    
    // Return Values
    def artifactURL = subJob1.description.tokenize('#')[0].tokenize('=')[1]
    def artifactVersion = subJob1.description.tokenize('#')[1].tokenize('=')[1]
    def commitID = subJob1.description.tokenize('#')[2].tokenize('=')[1]
    
currentBuild.setDescription("#artifactURL="+artifactURL+"#artifactVersion="+artifactVersion+"#commitID="+commitID)
    