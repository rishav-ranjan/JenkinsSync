import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

//incoming parameters
//def url = "https://github.com/WPPg2/DevOps-Deployment"
//def credentialsId = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
//def ParentPomVersion = "1.0.2-NIGHTLY-May.19.2016.09.33.PM"
//def cookbookVersion="1.0.13"
//def serviceName="avreg"
//def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"
//def artifactURL

//def pom_v = ParentPomVersion
def key
def secret_file
def content
def logName
def MasterWorkspaceDir
def SlaveWorkspaceDir

node('master'){
    //get master workspace directory
    MasterWorkspaceDir = pwd()

    //sudo chown -R ec2-user
    
    //manipulate json file 1
    File inputFile = new File("${serviceConfigPath}/${serviceName}/deploy_${serviceName}_input.json")
    content = inputFile.text
    def slurped = new JsonSlurper().parseText(content)
    secret_file = slurped.secretFile
    println "Secret file is ${secret_file}"
    def builder = new JsonBuilder(slurped)
    builder.content.service.artifact_url = "${artifactURL}"
    //print builder.content.service.artifact_url
    content = builder.toPrettyString()
    // null since non-serializable
    builder=null
}
    //writing manipulated json to slave node
    node('AMIBuilder') {
            //git checkout before any writeFile
            git credentialsId: "$credentialsId", url: "$url"
            // get slave workspace dir
            slaveWorkspaceDir = pwd()
            writeFile file: "deploy_${serviceName}_input.json", text: content
          }

//json file 2
node('master') { File inputFile2 = new File("${serviceConfigPath}/${serviceName}/deploy_${serviceName}.json")
    content = inputFile2.text
    def slurped2 = new JsonSlurper().parseText(content)
    key=slurped2.aws.instance.key.local_path
    println "AWS key is ${key}"
    logName=slurped2.log.name
    println "logName is ${logName}"
    def builder2 = new JsonBuilder(slurped2)
    //builder2.content.service.cookbook.url = "http://ec2-52-1-56-86.compute-1.amazonaws.com/nexus/service/local/repositories/devops-test-releases/content/com/hp/wpp/gen2/${serviceName}/${cookbookVersion}/${serviceName}-${cookbookVersion}.tar.gz"
    //builder2.content.service.env_databag_url="http://ec2-52-1-56-86.compute-1.amazonaws.com/nexus/service/local/repositories/devops-test-releases/content/com/hp/wpp/gen2/${serviceName}/${cookbookVersion}/env_databags.tar.gz"
    builder2.content.log.temp_dir="${slaveWorkspaceDir}"
    content = builder2.toPrettyString()
    builder2=null
}
node ('AMIBuilder') {
            writeFile file: "deploy_${serviceName}.json", text: content
}

node ('AMIBuilder') {
    sh "cd ${slaveWorkspaceDir}/chef-repos/aws-manager-repo/cookbooks"
    sh "ls"
    sh """ cd ${slaveWorkspaceDir}/chef-repos/aws-manager-repo/cookbooks
            ls
            sudo chef-client -z -j ${slaveWorkspaceDir}/deploy_${serviceName}.json -r 'recipe[ec2::deployService]' --log_level info
        """        
    //get deployment IP
    def ip_ad = readFile file: "${slaveWorkspaceDir}/${logName}.ipaddr"
    def ip_addr =ip_ad.trim()
    println "the IP address is ${ip_addr}"
    //get instance id
    def inst_id = readFile file: "${slaveWorkspaceDir}/${logName}.instid"
    def instanceID = inst_id.trim()
    println "${serviceName} Instance ID is ${instanceID}"
    sh "sudo scp -q -o StrictHostKeyChecking=no  -i ${key} /opt/keyfiles/stage/${serviceName}/encrypted_data_bag_secret ec2-user@${ip_addr}:${secret_file}"
    
    //outgoing parameters- BUILD_TIMESTAMP,instanceID
    currentBuild.setDescription("#instanceID="+instanceID)
 
}