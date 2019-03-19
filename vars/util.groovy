def getRepoName() 
{
 //return scm.getUserRemoteConfigs()[0].getUrl().tokenize(‘/’).last().split(“\\.“)[0]
 println(scm.getUserRemoteConfigs()[0].getUrl())
 return scm.getUserRemoteConfigs()[0].getUrl().tokenize('/.')[-2]
}

def writeSonarPropertiesFileIfNotExists(String projectName="", String sources=".", String projectBaseDir=".", String exclusions= "") {
    def sonarPropertiesFile = "${env.WORKSPACE}/sonar-project.properties"
    def sonarPropertiesString = ""
    if (!fileExists(sonarPropertiesFile)) {
        sources = sources == "" ? getRepoName() : sources
        projectName = projectName == "" ? getRepoName() : projectName
        sonarPropertiesString = "sonar.projectKey=" + getRepoName() 
           // + "\n" + "sonar.projectName=" + projectName 
            //\  
            //+ "\n" + "sonar.sources=" + sources \
           //+ "\n" + "sonar.projectBaseDir=" + projectBaseDir \
            //+ "\n" + "sonar.exclusions=" + exclusions \
           //+ "\n" + "sonar.sourceEncoding=UTF-8"
        writeFile file: sonarPropertiesFile, text: sonarPropertiesString
    }
    
    echo 'Contents of Sonar Properties file : ' 
    sonarPropertiesString = readFile sonarPropertiesFile
    echo temp
}
def scanWithSonarQube2() {
    def scannerHome = tool 'SonarQube Scanner 3.3'
    def qg = 'OK'
    
    writeSonarPropertiesFileIfNotExists()
    withSonarQubeEnv('digital-sonar') {
        sh "${scannerHome}/bin/sonar-scanner"
    }
    timeout(time: 30, unit: 'MINUTES') {
        qg = waitForQualityGate()
        // once analysis is complete and sonarqube returns control to jenkins, check for the quality gate status
        if (qg.status == 'ERROR') {
           error "Pipeline aborted due to quality gate failure: ${qg.status}"
        }
    }
    return qg
}
