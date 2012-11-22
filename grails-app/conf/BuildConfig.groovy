grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

//grails.plugin.'grails-plugin-platform-core' = '../../grails-plugin-platform/grails-plugin-platform-core'

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        excludes 'xml-apis', 'xerces'
    }
    
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        //grailsCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://repo.grails.org/grails/plugins"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":hibernate:$grailsVersion",
              ":release:2.0.3") {
            export = false
        }

        compile(':platform-core:1.0.RC1') {
            excludes "spring-test"
        }

        compile(':platform-ui:1.0.M1-SNAPSHOT') 
        compile(':spring-security-core:1.2.6') 
        compile(':email-confirmation:2.0.6') 
        compile ":resources:1.2.RC2"
/*
        runtime(':bootstrap-ui:1.0-SNAPSHOT') {
            excludes "svn"
        }

        runtime(':bootstrap-theme:1.0.BUILD-SNAPSHOT') {
            export = false
            excludes "spring-test"
        }
*/
    }
}
