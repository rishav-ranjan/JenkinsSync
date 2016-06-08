//def mvnpath = "/opt/apache-maven-3.2.5/bin/mvn"

//incoming parameters

//def url = "https://github.com/WPPg2/avatar-reg"
//def credentialsId = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
//def serviceName = "avreg"
//def buildTrigger = "NIGHTLY"
//def repoURL = "http://52.1.56.86/nexus/content/repositories/"

//def repoID = "morpheussnapshots"

//def targetNode = 'Morpheus'


def workspaceDir
def pom
def version
def groupID
def artifactVersion
def repoID
//example-May.26.2016.12.32.AM
def buildTimestamp = new Date().format("MMM.dd.yyyy.hh.mm.aaa")
def mvnpath = "/opt/apache-maven-3.2.5/bin/mvn"
node(targetNode){
    //get current workspace
    workspaceDir = pwd()
    //git checkin
    git credentialsId: "$credentialsId", url: "$url"
    
    sh "${mvnpath} org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version  | grep Building | cut -d' ' -f4 > maven.version"

    

    //read pom.xml
    pom = readFile('pom.xml')
    
    //extract values from POM
    def matcher = pom =~ '<avreg.version>(.+)</avreg.version>'
    version = matcher[0][1]
    matcher = pom =~ '<groupId>(.+)</groupId>'
    groupID = matcher[0][1]
    groupIdSlashed = groupID.replaceAll('\\.','/')
    matcher = pom =~ '<artifactId>(.+)</artifactId>'
    artifactID = matcher[0][1]
    matcher = pom =~ '<url>.+/(.+)/</url>'
    repoID = matcher[0][1]



    artifactVersion = "${version}-${buildTrigger}-${buildTimestamp}"
    echo "version ${version}"
    echo "groupID ${groupID}"
    echo "artifactID ${artifactID}"
    //set non-serializable matcher to null
    matcher=null
    
    //pre-build steps
    
    sh "${mvnpath} clean versions:set -DnewVersion=${artifactVersion}"
    sh "mkdir -p /home/ec2-user/avreg"
    sh "touch /home/ec2-user/avreg/morpheus_avatar_reg.log.properties"
    sh "git rev-parse HEAD > /home/ec2-user/avreg/morpheus_avatar_reg_commit_sha"
    sh 'echo "service_name=REG" > /home/ec2-user/avreg/morpheus_avatar_reg.log.properties'

    def com_id = readFile file: "/home/ec2-user/avreg/morpheus_avatar_reg_commit_sha"
    def commitID =com_id.trim()
    println "commit_id is ${commitID}"
    sh '''commit_id=`cat /home/ec2-user/avreg/morpheus_avatar_reg_commit_sha`
    echo "commit_id=${commit_id}" >> /home/ec2-user/avreg/morpheus_avatar_reg.log.properties'''
    
    
    //build and deploy step
    sh "${mvnpath} -B -f ${workspaceDir}/pom.xml deploy cobertura:cobertura -Dcobertura.report.format=xml"
    
     //outgoing parameters-artifactURL, commitID, artifactVersion i.e. ParentPomVersion, buildTimestamp
     def fileName = "${serviceName}-restapp-${artifactVersion}.war"
     def artifactURL = "${repoURL}${repoID}/${groupIdSlashed}/${serviceName}-restapp/${artifactVersion}/${fileName}"
    println artifactURL
    //currentBuild.setDescription("#artifactURL="+artifactURL+" #commitID="+commitID+"#artifactVersion="+artifactVersion+"#buildTimestamp="+buildTimestamp)
    currentBuild.setDescription("#artifactURL="+artifactURL+"#artifactVersion="+artifactVersion+"#commitID="+commitID)
}



