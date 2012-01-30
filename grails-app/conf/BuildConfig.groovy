grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

//grails.plugin.'grails-plugin-platform-core' = '../../grails-plugin-platform/grails-plugin-platform-core'

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        excludes 'spring-test'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:1.0.1") {
            export = false
        }

        build(':plugin-platform:1.0-SNAPSHOT') {
            excludes "spring-test"
        }
            
        compile(':spring-security-core:1.2.6') 
        compile(':email-confirmation:2.0-SNAPSHOT') 
        compile ":resources:1.1.6"
            
        runtime(':bootstrap-theme:1.0.BUILD-SNAPSHOT') {
            export = false
            excludes "spring-test"
        }
    }
}
