#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.jobs

class Job {

    static void createPipeline(pipelineJob, org.testng.xml.XmlSuite currentSuite, String suiteName) {
        pipelineJob.with {
            description(currentSuite.name)
            logRotator {
                numToKeep 100
            }

            /** Properties & Parameters Area **/
            parameters {
                choiceParam('env', getEnvironments(currentSuite), 'Environment to test against.')

                switch(suiteName) {
                    case ~/^(?!.*web).*api.*${symbol_dollar}/:
                        configure addHiddenParameter('platform', '', 'API')
                        configure addHiddenParameter('browser', '', 'NULL')
                        break;
                    case ~/^.*web.*${symbol_dollar}/:
                        configure addExtensibleChoice('browser', 'gc_BROWSER', 'Select a browser to run tests against.', 'chrome')
                        booleanParam('auto_screenshot', true, 'Generate screenshots automatically during the test')
                        booleanParam('keep_all_screenshots', true, 'Keep screenshots even if the tests pass')
                        break;
                    case ~/^.*android.*${symbol_dollar}/:
                        choiceParam('device', getDeviceList(suiteName), "Select the Device a Test will run against.  ALL - Any available device, PHONE - Any available phone, TABLET - Any tablet")
                        stringParam('build', '.*', "latest - use fresh build artifact from S3 or local storage;")
                        booleanParam('recoveryMode', true, 'Restart application between retries')
                        booleanParam('auto_screenshot', true, 'Generate screenshots automatically during the test')
                        booleanParam('keep_all_screenshots', true, 'Keep screenshots even if the tests pass')
                        configure addHiddenParameter('browser', '', 'NULL')
                        break;
                    default:
			throw new RuntimeException("Undefined suite test type: " + suiteName);
                        break;
                }

                configure addExtensibleChoice('repository', "gc_GIT_REPOSITORY", "Select a GitHub Testing Repository to run against", "git@github.com:qaprosoft/${artifactId}.git")
                configure addExtensibleChoice('branch', "gc_GIT_BRANCH", "Select a GitHub Testing Repository Branch to run against", "master")
                configure addHiddenParameter('suite', '', suiteName)
                configure addHiddenParameter('ci_parent_url', '', '')
                configure addHiddenParameter('ci_parent_build', '', '')

                stringParam('email_list', currentSuite.getParameter("jenkinsEmail").toString(), 'List of Users to be emailed after the test')
                choiceParam('retry_count', [0, 1, 2, 3], 'Number of Times to Retry a Failed Test')
                booleanParam('develop', false, 'Check to execute test without registration to Zafira and TestRail')
                booleanParam('rerun_failures', false, 'During ${symbol_escape}"Rebuild${symbol_escape}" pick it to execute only failed cases')
                configure addHiddenParameter('project', '', "unknown")
                configure addHiddenParameter('overrideFields', '' , getCustomFields(currentSuite))

                configure addExtensibleGroovyScript('ci_run_id', "", "import static java.util.UUID.randomUUID${symbol_escape}nreturn [randomUUID()]", true)
            }

            /** Git Stuff **/
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                url('${symbol_dollar}{repository}')
                            }
                            branch('${symbol_dollar}{branch}')
                        }
                    }
                    scriptPath('src/main/groovy/${packageInPathFormat}/pipelines/JenkinsFile')
                }
            }
        }
    }

    static Closure addExtensibleChoice(choiceName, globalName, desc, choice) {
        //TODO:  Need to move the choiceListProvider into a parameterized class as well as that can change.
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'jp.ikedam.jenkins.plugins.extensible__choice__parameter.ExtensibleChoiceParameterDefinition'(plugin: 'extensible-choice-parameter@1.4.1') {
                name choiceName
                description desc
                editable false
                choiceListProvider(class: 'jp.ikedam.jenkins.plugins.extensible_choice_parameter.GlobalTextareaChoiceListProvider') {
                    whenToAdd 'Triggered'
                    name globalName
                    defaultChoice choice
                }
            }
        }
    }

    static Closure addExtensibleGroovyScript(choiceName, desc, scriptValue, editableValue) {
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'jp.ikedam.jenkins.plugins.extensible__choice__parameter.ExtensibleChoiceParameterDefinition'(plugin: 'extensible-choice-parameter@1.4.1') {
                name choiceName
                description desc
                editable editableValue
                choiceListProvider(class: 'jp.ikedam.jenkins.plugins.extensible_choice_parameter.SystemGroovyChoiceListProvider') {
		    groovyScript {
                	script scriptValue
			sandbox true
	            }
                    usePredefinedVariables false
                }
            }
        }
    }


    static Closure addHiddenParameter(paramName, paramDesc, paramValue) {
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'com.wangyin.parameter.WHideParameterDefinition'(plugin: 'hidden-parameter@0.0.4') {
                name paramName
                description paramDesc
                defaultValue paramValue
            }
        }
    }

    static void createRegressionPipeline(pipelineJob, suiteName, List customFields) {
        pipelineJob.with {
            description(suiteName)
            logRotator {
                numToKeep 100
            }
            configure addExtensibleChoice('repository', "repositories", "Select a GitHub Testing Repository to run against", "git@github.com:qaprosoft/${artifactId}.git")
            configure addExtensibleChoice('branch', "gc_GIT_BRANCH", "Select a GitHub Testing Repository Branch to run against", "master")
            parameters {
                stringParam('email_list_for_pipeline', 'msarychau@qaprosoft.com', 'List of Users to be emailed after the test')
                booleanParam('overrideEmail', false, 'Check to override email field on all Pipeline Jobs with above email')
                for (String customField : customFields) {
                    if (!customField.contains("=")) {
                        stringParam(customField, "", "Custom Field")
                    } else {
                        def customFieldList = customField.split("=")
                        stringParam(customFieldList[0], customFieldList[1].toString(), "Custom Field")
                    }
                }
            }
            configure addHiddenParameter('overrideFields', 'This allows for mass overriding of any fields in this pipeline.', '')
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                url('${symbol_dollar}{repository}')
                                credentials('qJenkins-Token')
                            }
                            branch('${symbol_dollar}{branch}')
                        }
                    }
                    scriptPath('src/main/groovy/${packageInPathFormat}/pipelines/JenkinsFilePipeline')
                }
            }
        }
    }

    static List<String> getEnvironments(currentSuite) {
        def envList = getGenericSplit(currentSuite, "jenkinsEnvironments")

        if (envList.isEmpty()) {
            envList.add("QA")
        }

        return envList
    }

    static String getCustomFields(currentSuite) {
        def overrideFields = getGenericSplit(currentSuite, "overrideFields")
        def prepCustomFields = ""

        if (!overrideFields.isEmpty()) {
            for (String customField : overrideFields) {
                prepCustomFields = prepCustomFields + " -D" + customField
            }
        }

        return prepCustomFields
    }

    static List<String> getGenericSplit(currentSuite, parameterName) {
        String genericField = currentSuite.getParameter(parameterName)
        def genericFields = []

        if (genericField != null) {
            if (!genericField.contains(", ")) {
                genericFields = genericField.split(",")
            } else {
                genericFields = genericField.split(", ")
            }
        }
        return genericFields
    }

    static List<String> getTagList(String tags) {
        if (tags.contains(", ")) {
            return tags.split(", ")
        } else {
            return tags.split(",")
        }
    }

    static List<String> getDeviceList(String suite) {
        def currentBuild = Thread.currentThread().executable
        def workspace = currentBuild.getEnvVars()["WORKSPACE"]
        def deviceList = ["ALL", "Samsung_Galaxy_TAB4_10", "Samsung_Galaxy_Note_4", "Samsung_Grand_Prime", "Samsung_Galaxy_S6", "Samsung_Galaxy_S7", "Motorola_Nexus_6"]

        return deviceList
    }

    static checkAndAddDevice(List<String> deviceList, String suite, Map deviceEntry) {

        switch(suite.toLowerCase()) {
            case ~/^.*handset.*${symbol_dollar}/:
                deviceList.add(deviceEntry.get("device").toString())
                checkAndAddDeviceType(deviceList, deviceEntry)
                deviceList.sort { a,b -> a.toLowerCase() <=> b.toLowerCase() }
                deviceList.remove("phone")
                deviceList.add(0,"phone")
            case ~/^.*tablet.*${symbol_dollar}/:
                if (deviceEntry.get("deviceType").toString().toLowerCase().contains("tablet") && !deviceList.contains(deviceEntry.get("device"))) {
                    deviceList.add(deviceEntry.get("device").toString())
                    checkAndAddDeviceType(deviceList, deviceEntry)
                    deviceList.sort { a,b -> a.toLowerCase() <=> b.toLowerCase()  }
                    deviceList.remove("tablet")
                    deviceList.add(0,"tablet")
                }
                break;
            default:
                deviceList.add(deviceEntry.get("device").toString())
                checkAndAddDeviceType(deviceList, deviceEntry)
                deviceList.sort { a,b -> a.toLowerCase() <=> b.toLowerCase()  }
                break;
        }
    }

    static checkAndAddDeviceType(List<String> deviceList, Map deviceEntry) {
        if (!deviceList.contains(deviceEntry.get("deviceType").toString().toLowerCase())) {
            deviceList.add(deviceEntry.get("deviceType").toString())
        }
        checkAndAddTag(deviceList, deviceEntry)

        return deviceList
    }

    static checkAndAddTag(List<String> deviceList, Map deviceEntry) {
        if (deviceEntry.get("tags") != null) {
            for (String tag : getTagList(deviceEntry.get("tags").toString())) {
                if (!deviceList.contains(tag.toLowerCase())) {
                    deviceList.add(tag)
                }
            }
        }
        return deviceList
    }
}