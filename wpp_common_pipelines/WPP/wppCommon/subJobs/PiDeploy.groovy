//global variables
def gitDeployURL = "https://github.com/WPPg2/DevOps-Deployment"

//service variables
//def serviceName = "avreg"
//def gitCredentials = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"
//def parentstageName = "Acceptance"
//userjob variables
def deployAmiTargetNode = 'AMIBuilder'

def subnetIDs="96d1e0e1,55041830"

def subnetList = subnetIDs.tokenize(',')
    def subnetNum = 1
    for (subnetID in subnetList) {
    stage "${parentstageName}::DeploySubnet${subnetNum}"
    subJob2 = build  job: 'deployAMI',
                     parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'url', value: gitDeployURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
                        [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
                        [$class: 'StringParameterValue', name: 'subnetID', value: subnetID ],
                        [$class: 'StringParameterValue', name: 'subnetNum', value: subnetNum.toString() ],
                        [$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
                    ] ;
            subnetNum++ }


    