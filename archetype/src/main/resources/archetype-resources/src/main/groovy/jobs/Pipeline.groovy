#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.jobs

@Grab('org.testng:testng:6.3.1')

import groovy.io.FileType;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

createPipelines()

void createPipelines() {
    def list = []
    def listPipelines = []

    def currentBuild = Thread.currentThread().executable
    def workspace = currentBuild.getEnvVars()["WORKSPACE"]
    println "JENKINS_HOME: ${symbol_dollar}{JENKINS_HOME}"
    println "WORKSPACE: ${symbol_dollar}{WORKSPACE}"

    def dir = new File(workspace, "src/test/resources/testng_suites")
    dir.eachFileRecurse (FileType.FILES) { file ->
        list << file
    }

    def jobFolder = "Pipeline"
    folder(jobFolder) {
        displayName(jobFolder)
    }

    list.each {
        def currentSuiteItem = it
        if (currentSuiteItem.name.endsWith('.xml')) {
            def xmlFile = new Parser(new File(currentSuiteItem.path).absolutePath)
            xmlFile.setLoadClasses(false)

            List<XmlSuite> suiteXml = xmlFile.parseToList()
            XmlSuite currentSuite = suiteXml.get(0)

            if (currentSuite.toXml().contains("jenkinsRegressionPipeline")) {
                String suiteName = currentSuiteItem.path
                String suiteFrontRemoval = "testng_suites/"
                suiteName = suiteName.substring(suiteName.lastIndexOf(suiteFrontRemoval) + suiteFrontRemoval.length(), suiteName.indexOf(".xml"))

                println "${symbol_escape}n" + currentSuiteItem.path
                println "${symbol_escape}n" + suiteName
                println "${symbol_escape}n" + currentSuite.toXml() + "${symbol_escape}n"
                println "jenkinsRegressionPipeline: " + currentSuite.getParameter("jenkinsRegressionPipeline").toString()
                if (currentSuite.getParameter("jenkinsRegressionPipeline").length() > 0) {
                    scanPipelines(currentSuite, listPipelines)
                }
            }
        }
    }
    println "${symbol_escape}n"
    println "Pipeline List: " + listPipelines

    buildPipeline(jobFolder, listPipelines)
}

def scanPipelines(XmlSuite currentSuite, List listPipelines) {
    def pipelineNames = currentSuite.getParameter("jenkinsRegressionPipeline").toString().split(",")

    for (String pipelineName : pipelineNames) {
        def pipelineMap = [:]

        pipelineMap.put("name", pipelineName)
        pipelineMap.put("jobName", currentSuite.getParameter("jenkinsJobName").toString())
        pipelineMap.put("environments", currentSuite.getParameter("jenkinsEnvironments").toString())
        pipelineMap.put("overrideFields", currentSuite.getParameter("overrideFields").toString())
        pipelineMap.put("scheduling", currentSuite.getParameter("jenkinsPipelineScheduling").toString())


        println "${symbol_escape}n"
        println "${symbol_dollar}{pipelineMap.values()}"
        listPipelines.add(pipelineMap);
    }
}

def buildPipeline(String jobFolder, List fullPipelineList) {

    while (fullPipelineList.size() > 0) {
        def grabbedPipeline = fullPipelineList.findAll { it.name == fullPipelineList.first().name.toString() }
        println "${symbol_escape}n"
        println "grabbedPipeline: ${symbol_dollar}{grabbedPipeline}"
        
        fullPipelineList.removeAll { it.name.toString().equalsIgnoreCase(grabbedPipeline.first().name.toString())}

        settingUpPipeline(jobFolder, grabbedPipeline)
    }
}

def settingUpPipeline(String jobFolder, List pipelineList) {
    println "${symbol_escape}n"
    println "Creating Regression Pipeline: " + pipelineList.first().name

    def customFields = []
    def scheduling = ""

    for (Map pipelineItem : pipelineList) {
        println "${symbol_escape}n"
        println "Pipeline Item: " + pipelineItem

        if (!pipelineItem.get("overrideFields").toString().contains("null")) {
            scanForCustomFields(pipelineItem, customFields)
        }

        if (scheduling.length() == 0 && !pipelineItem.get("scheduling").toString().contains("null") ) {
            scheduling = pipelineItem.get("scheduling").toString()
        }
    }

    Job.createRegressionPipeline(pipelineJob(jobFolder + "/" + pipelineList.first().name), pipelineList.first().name, pipelineList.first().environments, customFields, scheduling)
}

def scanForCustomFields(Map pipelineItem, List existingCustomFieldsList) {
    def currentCustomFields = pipelineItem.get("overrideFields").toString().split(",")
    for (String customField : currentCustomFields) {
        if (!existingCustomFieldsList.contains(customField.trim())) {
            existingCustomFieldsList.add(customField.trim())
        }
    }
}