def projectGitURL = "https://github.com/alextoderita/sapient.git"
def gitCredentialsId = "sapient-creds"


node("master"){
    getSourceCode(projectGitURL, gitCredentialsId)
    buildDockerImageParallel()
    deployDockerImageParallel()
    runInput()
    deleteDockerContainer("sapient-app1-com")
    deleteDockerContainer("sapient-app2-com")
}

def buildDockerImageParallel(){
  steps {
    parallel(
      a: {
        buildDockerImage("sapient-app1-tomcat", "Dockerfile1")
      },
      b: {
        buildDockerImage("sapient-app2-tomcat", "Dockerfile2")
      }
    )
  }
}

def deployDockerImageParallel(){
  steps {
    parallel(
      a: {
        deployDockerContainer("sapient-app1-com", "sapient-app1-tomcat", "8081")
      },
      b: {
        deployDockerContainer("sapient-app2-com", "sapient-app2-tomcat", "8082")
      }
    )
  }
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

def deployDockerContainer(dockerContainerName, dockerImageName, port){
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
