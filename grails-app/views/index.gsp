<%

grailsApplication.tagLibClasses.each { art ->
    println "Taglib: ${art.name} / ${art.clazz}"
    println "Tags: ${art.tagNames}"
}


%>