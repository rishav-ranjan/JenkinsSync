

// parameters

//url
//gitCredentials
//serviceName
//buildTrigger
//repoURL
//targetNode


def mvnpath = "/opt/apache-maven-3.2.5/bin/mvn"
def repoID = "ctfsnapshots"
node(targetNode){
    //get current workspace
    workspaceDir = pwd()
	
	if(!rootPomPath){
       rootPomPath="."
    }
	
    //git checkout, 
    git gitCredentials: "$gitCredentials", url: "$url"

    

	
    sh """  cd ${rootPomPath}
        ${mvnpath} org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.groupId  | grep -v INFO | grep -i -v WARNING > ${workspaceDir}/maven.groupId
		${mvnpath} org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.artifactId  | grep -v INFO | grep -i -v WARNING > ${workspaceDir}/maven.artifactId
		${mvnpath} org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version  | grep -v INFO | grep -i -v WARNING> ${workspaceDir}/maven.version"""

	groupID = readFile file: "${workspaceDir}/maven.groupId".trim()
	artifactID = readFile file: "${workspaceDir}/maven.artifactId".trim()
	version = readFile file: "${workspaceDir}/maven.version".trim()
	
	groupID=groupID.trim()
	artifactID=artifactID.trim()
	version=version.trim()
	groupIdSlashed = groupID.replaceAll('\\.','/')
	buildTimestamp = new Date().format("MMM.dd.yyyy.hh.mm.aaa")
	artifactVersion = "${version}-${buildTrigger}-${buildTimestamp}"
	
	println "groupId ${groupID}"
	println "artifactId ${artifactID}"
    println "version ${version}"
    println "buildTrigger ${buildTrigger}"
    println "buildTimestamp ${buildTimestamp}"
    println "artifactVersion ${artifactVersion}"
	

	sh "${mvnpath} -f ${workspaceDir}/${rootPomPath}/pom.xml clean versions:set -DnewVersion=${artifactVersion}"	
		
		
	//Get commit id
	sh "git rev-parse HEAD > ./git.sha"
	commitID = readFile file: "./git.sha".trim()	
		

    
    //build and deploy step
    //sh "${mvnpath} -B -f ${workspaceDir}/${rootPomPath}/pom.xml deploy cobertura:cobertura -Dcobertura.report.format=xml"

    //currentBuild.setDescription("#pomVersion="+version+"#artifactVersion="+artifactVersion+"#commitID="+commitID)
    def fileName = "${serviceName}-restapp-${artifactVersion}.war"
     def artifactURL = "${repoURL}${repoID}/${groupIdSlashed}/${serviceName}-restapp/${artifactVersion}/${fileName}"
    println artifactURL
    currentBuild.setDescription("#artifactURL="+artifactURL+"#artifactVersion="+artifactVersion+"#commitID="+commitID)
}

