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

            authenticationToken('ciStart')

            /** Properties & Parameters Area **/
            parameters {
                choiceParam('env', getEnvironments(currentSuite.getParameter("jenkinsEnvironments")), 'Environment to test against.')

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
                        choiceParam('device', getAndroidDeviceList(suiteName), "Select the Device a Test will run against.  ALL - Any available device, PHONE - Any available phone, TABLET - Any tablet")
                        stringParam('build', '.*', "latest - use fresh build artifact from S3 or local storage;")
                        booleanParam('recoveryMode', true, 'Restart application between retries')
                        booleanParam('auto_screenshot', true, 'Generate screenshots automatically during the test')
                        booleanParam('keep_all_screenshots', true, 'Keep screenshots even if the tests pass')
                        configure addHiddenParameter('browser', '', 'NULL')
                        configure addHiddenParameter('DefaultPool', '', currentSuite.getParameter("jenkinsMobileDefaultPool"))
                        break;
                    case ~/^.*ios.*${symbol_dollar}/:
                        choiceParam('device', getiOSDeviceList(suiteName), "Select the Device a Test will run against.  ALL - Any available device, PHONE - Any available phone, TABLET - Any tablet")
                        stringParam('build', '.*', "latest - use fresh build artifact from S3 or local storage;")
                        booleanParam('recoveryMode', true, 'Restart application between retries')
                        booleanParam('auto_screenshot', true, 'Generate screenshots automatically during the test')
                        booleanParam('keep_all_screenshots', true, 'Keep screenshots even if the tests pass')
                        configure addHiddenParameter('browser', '', 'NULL')
                        configure addHiddenParameter('DefaultPool', '', currentSuite.getParameter("jenkinsMobileDefaultPool"))
                        break;
                    default:
                        booleanParam('auto_screenshot', false, 'Generate screenshots automatically during the test')
                        booleanParam('keep_all_screenshots', false, 'Keep screenshots even if the tests pass')
                        configure addHiddenParameter('browser', '', 'NULL')
                        break;
                }

                configure addExtensibleChoice('repository', "gc_GIT_REPOSITORY", "Select a GitHub Testing Repository to run against", "https://github.${packageInPathFormat}/${artifactId}.git")
                configure addExtensibleChoice('branch', "gc_GIT_BRANCH", "Select a GitHub Testing Repository Branch to run against", "master")
                configure addHiddenParameter('zafira_project', 'Zafira project name', currentSuite.getParameter("zafira_project"))
                configure addHiddenParameter('suite', '', suiteName)
                configure addHiddenParameter('ci_parent_url', '', '')
                configure addHiddenParameter('ci_parent_build', '', '')

                stringParam('email_list', currentSuite.getParameter("jenkinsEmail").toString(), 'List of Users to be emailed after the test')
                choiceParam('retry_count', [0, 1, 2, 3], 'Number of Times to Retry a Failed Test')
                booleanParam('develop', false, 'Check to execute test without registration to Zafira and TestRail')
                booleanParam('rerun_failures', false, 'During ${symbol_escape}"Rebuild${symbol_escape}" pick it to execute only failed cases')
                configure addHiddenParameter('overrideFields', '' , getCustomFields(currentSuite))
                configure addExtensibleChoice('ci_run_id', '', 'import static java.util.UUID.randomUUID${symbol_escape}nreturn [randomUUID()]')

                def threadCount = '1'
                if (currentSuite.toXml().contains("jenkinsDefaultThreadCount")) {
                	threadCount = currentSuite.getParameter("jenkinsDefaultThreadCount")
                }
                stringParam('thread_count', threadCount, 'number of threads, number')

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
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'jp.ikedam.jenkins.plugins.extensible__choice__parameter.ExtensibleChoiceParameterDefinition'(plugin: 'extensible-choice-parameter@1.3.3') {
                name choiceName
                description desc
                editable true
                choiceListProvider(class: 'jp.ikedam.jenkins.plugins.extensible_choice_parameter.GlobalTextareaChoiceListProvider') {
                    whenToAdd 'Triggered'
                    name globalName
                    defaultChoice choice
                }
            }
        }
    }

    static Closure addExtensibleChoice(choiceName, desc, code) {
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'jp.ikedam.jenkins.plugins.extensible__choice__parameter.ExtensibleChoiceParameterDefinition'(plugin: 'extensible-choice-parameter@1.3.3') {
                name choiceName
                description desc
                editable true
                choiceListProvider(class: 'jp.ikedam.jenkins.plugins.extensible_choice_parameter.SystemGroovyChoiceListProvider') {
                    groovyScript {
                        script code
                        sandbox true
                        usePrefinedVariables false
                    }
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

    static void createRegressionPipeline(pipelineJob, suiteName, environments, List customFields, String scheduling) {
        pipelineJob.with {
            description(suiteName)
            logRotator {
                numToKeep 100
            }

            authenticationToken('ciStart')

            if (scheduling.length() > 0) {
                triggers {
                    cron(scheduling)
                }
            }
            properties {
                disableConcurrentBuilds()
            }

            configure addExtensibleChoice('repository', "gc_GIT_REPOSITORY", "Select a GitHub Testing Repository to run against", "https://github.${packageInPathFormat}/${artifactId}.git")
            configure addExtensibleChoice('branch', "gc_GIT_BRANCH", "Select a GitHub Testing Repository Branch to run against", "master")
            parameters {
                choiceParam('env', getEnvironments(environments), 'Environment to test against.')
                stringParam('email_list', '', 'List of Users to be emailed after the test. If empty then populate from jenkinsEmail suite property')
                stringParam('retry_count', '2', "number of retries for failed tests")
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
                            }
                            branch('${symbol_dollar}{branch}')
                        }
                    }
                    scriptPath('src/main/groovy/${packageInPathFormat}/pipelines/JenkinsFilePipeline')
                }
            }
        }
    }

    static List<String> getEnvironments(environments) {
        def envList = getGenericSplit(environments)

        if (envList.isEmpty()) {
            envList.add("PROD")
        }

        return envList
    }

    static String getCustomFields(currentSuite) {
        def overrideFields = getGenericSplit(currentSuite.getParameter("overrideFields"))
        def prepCustomFields = ""

        if (!overrideFields.isEmpty()) {
            for (String customField : overrideFields) {
                prepCustomFields = prepCustomFields + " -D" + customField.trim()
            }
        }

        return prepCustomFields
    }

    static List<String> getGenericSplit(genericField) {
        def genericFields = []

        if (genericField != null) {
            genericFields = genericField.split(",")
        }

        return genericFields
    }


    static List<String> getAndroidDeviceList(String suite) {
        def deviceList = ["DefaultPool", "ANY", "Asus_Zenphone", "HTC_Desire_620","Huawei_Y540","Nexus_4","Nexus_5","Nexus_7_2012","Nexus_7_2013","LG_Nexus_4","LG_Nexus_5","LG_Nexus_5x","LG_G4","LG_Spirit","Lenovo_P780","Moto_Nexus_6","Moto_G","Moto_G_XT1032","MEIZU_M5S","OnePlus_One","Samsung_Galaxy_S4_mini","Samsung_Galaxy_S4_1","Samsung_Galaxy_S6","Samsung_Galaxy_S7","Samsung_Galaxy_Grand_2","Samsung_Galaxy_S5_mini","Samsung_Galaxy_Note3","Samsung_Galaxy_J5","Samsung_Grand_Prime","Samsung_Galaxy_Core_II",,"Samsung_Galaxy_S4_2","Samsung_Galaxy_S5","Samsung_Galaxy_S4_3","Samsung_Nexus_3","Samsung_Galaxy_S3","Samsung_Galaxy_A7","Samsung_Galaxy_A5","Samsung_Galaxy_S7_2","Samsung_Galaxy","Samsung_Galaxy_S8_Plus"]
        return deviceList
    }

    static List<String> getiOSDeviceList(String suite) {
        def deviceList = ["DefaultPool", "ANY", "iPhone_7", "iPhone_7Plus"]
        return deviceList
    }

}