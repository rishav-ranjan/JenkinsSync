def serviceName = "avreg"
def gitServiceURL = "https://github.com/WPPg2/avatar-reg"
def gitLoadTestURL = "https://github.com/WPPg2/CTF"
def gitDeployURL = "https://github.com/WPPg2/DevOps-Deployment"
def gitCredentials = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
def buildTrigger = "NIGHTLY"

subJob = build  job: 'Gen2_CD_Pipeline',
            parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'gitServiceURL', value: gitServiceURL ],
                        [$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                        [$class: 'StringParameterValue', name: 'gitLoadTestURL', value: gitLoadTestURL ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                ];
