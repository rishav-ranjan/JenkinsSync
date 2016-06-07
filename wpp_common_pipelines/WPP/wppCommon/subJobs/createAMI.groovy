import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

//incoming variables
//def url = "https://github.com/WPPg2/DevOps-Deployment"
//def credentialsId = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
//def serviceName="avreg"
//def commitID="ggggggggggggg"
def cookbookVersion="1.0.13"
//def version="1.0.2"
//def instanceID="i-576d7efb"
//def timestamp="jan.2.dgds"

//variables specific to json file
//service_name="avreg"
//def service_version="1.0.0"
//def cookbook_version="1.0.5"
def appconfig_version="nil"
def db_version="nil"
def db_cookbook_version="nil"

def amiName="${serviceName}_${artifactVersion}"
//def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"
def amiID
def slaveWorkspaceDir

node('AMIBuilder')
{
    slaveWorkspaceDir=pwd()
}
node('master')
{
    //manipulating json file 1
    File inputFile = new File("${serviceConfigPath}/${serviceName}/create_ami_${serviceName}.json")
    content = inputFile.text
    def slurped = new JsonSlurper().parseText(content)
    def builder = new JsonBuilder(slurped)
    //builder.content.aws.ami.tags[0].value="\"Service:${service_name}|Service_Version:${service_version}|Service_CookBook_Version:${cookbook_version}|AppConfig_Version:${appconfig_version}|DB_Version:${db_version}|DB_Cookbook_Version:${db_cookbook_version}\""
    builder.content.aws.ami.tags[0].value="\"Service:${serviceName}|Service_Version:${artifactVersion}|Service_CookBook_Version:${cookbookVersion}|AppConfig_Version:${appconfig_version}|DB_Version:${db_version}|DB_Cookbook_Version:${db_cookbook_version}\""
    builder.content.aws.ami.tags[1].value="${commitID}"
    builder.content.aws.instance.id="${instanceID}"
    builder.content.aws.ami.name="${amiName}"
    builder.content.log.temp_dir="${slaveWorkspaceDir}"
    content = builder.toPrettyString()
    builder=null
}
node('AMIBuilder')
{
    git credentialsId: "$credentialsId", url: "$url"   //git checkout before writeFile
    writeFile file: "create_ami_${serviceName}.json", text: content
    sh """cd ${slaveWorkspaceDir}/chef-repos/aws-manager-repo/cookbooks
    sudo chef-client -z -j ${slaveWorkspaceDir}/create_ami_${serviceName}.json -r 'recipe[ec2::createAMIFromInstance]' --log_level info"""
}
node('master')
{
    //manipulating json file 2
    File inputFile2 = new File("${serviceConfigPath}/${serviceName}/terminate_instance_${serviceName}.json")
    content = inputFile2.text
    def slurped2 = new JsonSlurper().parseText(content)
    def builder2 = new JsonBuilder(slurped2)
    builder2.content.aws.instance.id="${instanceID}"
    content = builder2.toPrettyString()
    builder2=null
}
node('AMIBuilder')
{
    writeFile file: "terminate_instance_${serviceName}.json", text: content
    sh """cd ${slaveWorkspaceDir}/chef-repos/aws-manager-repo/cookbooks
    sudo chef-client -z -j ${slaveWorkspaceDir}/terminate_instance_${serviceName}.json -r 'recipe[ec2::terminateInstance]' --log_level info"""
    def content = readFile file: "${serviceName}.log.serviceami"
    def slurped = new JsonSlurper().parseText(content)
    amiID = slurped.ImageId
    println "AMI ID is ${amiID}"
    def amiContent = "avatar_reg_ami_id=${amiID}"
    writeFile file: "${serviceName}.log.amiidprop", text: amiContent
}

currentBuild.setDescription("#amiID="+amiID)