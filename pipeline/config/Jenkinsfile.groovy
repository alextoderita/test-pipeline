def projectGitURL = "https://github.com/alextoderita/test-pipeline.git"
def gitCredentialsId = "jenkins-git-credentials"


node("master"){
    getSourceCode(projectGitURL, gitCredentialsId)
    buildDockerImage("app1-image", "Dockerfile1")
    buildDockerImage("app2-image", "Dockerfile2")
    deployDockerContainer("app1-image", "app1-com", "8081")
    deployDockerContainer("app2-image", "app2-com", "8082")
    runInput()
    deleteDockerContainer("app1-com")
    deleteDockerContainer("app2-com")
}

def getSourceCode(projectGitURL, gitCredentialsId){
    stage("Git Checkout"){
        echo "Checking out the code from SCM"
        checkout([$class: 'GitSCM', branches: [[name: '*/master']],
            userRemoteConfigs: [[ credentialsId: "${gitCredentialsId}", url: "${projectGitURL}" ]]])
    }
}

def buildDockerImage(dockerImageName, dockerFileName){
	stage("Build Docker image"){
		sh "sudo docker build -t ${dockerImageName} . --file infra/docker/${dockerFileName}"
	}
}

def deployDockerContainer(dockerImageName, dockerContainerName, port){
	stage("Deploy Docker container"){
		sh "sudo docker run --security-opt=apparmor:unconfined --security-opt seccomp:unconfined --privileged -p ${port}:8080 -d --name ${dockerContainerName} ${dockerImageName}"
	}
}

def runInput(){
  stage("Approval Definition"){
	  input id: "approval-1", message: 'Check the application status... Is it running?', ok: "Approve"
  }
}

def deleteDockerContainer(dockerContainerName){
	stage("Remove Docker container"){
		sh "sudo docker rm --force ${dockerContainerName}"
	}
}
