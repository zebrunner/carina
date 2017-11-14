#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.jobs


@Grab('org.testng:testng:6.3.1')

import groovy.io.FileType;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import ${package}.jobs.Job;

createJobs()

void createJobs() {
    def list = []

    def currentBuild = Thread.currentThread().executable
    def workspace = currentBuild.getEnvVars()["WORKSPACE"]
    println "JENKINS_HOME: ${symbol_dollar}{JENKINS_HOME}"
    println "WORKSPACE: ${symbol_dollar}{WORKSPACE}"

    def dir = new File(workspace, "src/test/resources/testng_suites")
    dir.eachFileRecurse (FileType.FILES) { file ->
        list << file
    }

    list.each {
        def currentSuiteItem = it
        if (currentSuiteItem.name.endsWith('.xml')) {
            def xmlFile = new Parser(new File(currentSuiteItem.path).absolutePath)
            xmlFile.setLoadClasses(false)

            List<XmlSuite> suiteXml = xmlFile.parseToList()
            XmlSuite currentSuite = suiteXml.get(0)

            if (currentSuite.toXml().contains("jenkinsJobCreation")) {
            
                def jobFolder = currentSuite.getParameter("jenkinsJobFolder").toString()
    			folder(jobFolder) {
        			displayName(jobFolder)
    			}
    			
                String suiteName = currentSuiteItem.path
                String suiteFrontRemoval = "testng_suites/"
                suiteName = suiteName.substring(suiteName.lastIndexOf(suiteFrontRemoval) + suiteFrontRemoval.length(), suiteName.indexOf(".xml"))

                println "${symbol_escape}n" + currentSuiteItem.path
                println "${symbol_escape}n" + suiteName
                println "${symbol_escape}n" + currentSuite.toXml() + "${symbol_escape}n"
                println "jenkinsJobCreation: " + currentSuite.getParameter("jenkinsJobCreation").toString()
                if (currentSuite.getParameter("jenkinsJobCreation").contains("true")) {
                    def jobName = currentSuite.getParameter("jenkinsJobName").toString()
                    createViews(suiteName, jobFolder)
                    Job.createPipeline(pipelineJob(jobFolder + "/" + jobName), currentSuite, suiteName)
                }
            }
        }
    }
}

void createViews(suiteName, jobFolder) {
    String[] viewCreationList = suiteName.tokenize("${symbol_escape}${symbol_escape}")
    println "${symbol_escape}n Checking View List: ${symbol_dollar}{viewCreationList}"

    if (viewCreationList.size() >= 2) {
        for (int i = 0; i < 2; i++) {
            println viewCreationList[i].capitalize()
            listView(jobFolder + "/" + viewCreationList[i].toUpperCase()) {
                columns {
                    status()
                    weather()
                    name()
                    lastSuccess()
                    lastFailure()
                    lastDuration()
                    buildButton()
                }
                jobs {
                    def filterString = String.format("(?i).*%s.*", viewCreationList[i]).capitalize()
                    regex("${symbol_dollar}{filterString}")
                }
            }
        }
    }
}

