import org.jetbrains.grammarkit.tasks.GenerateLexerTask

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.4"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
}

group = "com.linuxgods.kreiger.idea.jmte"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.floreysoft:jmte:7.0.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    intellijPlatform {
        intellijIdeaCommunity("2025.2.4")

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.intelliLang")
        bundledPlugin("com.intellij.modules.json")
        //bundledPlugin("com.intellij.freemarker")
        //bundledPlugin("com.intellij.velocity")
        plugin( "PsiViewer:252.23892.248")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellijPlatform {
    pluginConfiguration {
    }
    /*
    plugins = ['com.intellij.java', 'org.intellij.intelliLang',
               'PsiViewer:233.2',
              // 'org.jetbrains.plugins.go-template:232.9921.89',
              // 'com.dmarcotte.handlebars:232.8660.88',
               'org.jusecase.jte-intellij:2.1.2',
               'com.intellij.freemarker', 'com.intellij.velocity'
    ]
    updateSinceUntilBuild = false

     */
}

tasks.generateLexer {
    sourceFile = file("src/main/resources/JmteExpression.flex")
    targetOutputDir = file("build/generated/lexer/com/linuxgods/kreiger/idea/jmte")
    dependsOn("cleanGenerateLexer")
}

val generateReferenceLexer = tasks.register<GenerateLexerTask>("generateReferenceLexer") {
    sourceFile = file("src/main/resources/JmteReference.flex")
    targetOutputDir = file("build/generated/reference-lexer/com/linuxgods/kreiger/idea/jmte")
    dependsOn("cleanGenerateReferenceLexer")
}

tasks.generateParser {
    sourceFile = file("src/main/resources/Jmte.bnf")
    pathToParser = "_JmteParser"
    pathToPsiRoot = "psi"
    targetRootOutputDir = file("build/generated/parser")
    dependsOn("cleanGenerateParser")
}

sourceSets {
    named("main") {
        java {
            srcDir("build/generated/lexer")
            srcDir("build/generated/reference-lexer")
            srcDir("build/generated/parser")
        }
    }
}

tasks.withType<JavaCompile> {
    options.release = 17
    dependsOn(generateReferenceLexer)
    dependsOn(tasks.generateLexer)
    dependsOn(tasks.generateParser)
}

tasks.runIde {
    jvmArgs("-Xmx4G")
}

tasks.test {
    useJUnitPlatform()
}
