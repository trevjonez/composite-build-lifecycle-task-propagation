Composite build lifecycle task propagation
=================================

In order to make growing a gradle project smooth for our teams I make heavy use of lifecycle tasks to "autoconfigure" CI
jobs. Gradle does a decent job of this and using some nice tricks with plugin authoring you can create a different
lifecycle task for each phase of a PR pipeline that will ensure your dev's don't forget to update the CI pipeline
correctly because the pipeline becomes implicit via custom convention plugins.

A real world-ish example of this is show here. We have a plugin that lets us hook up a `unitTestSuite` task
automatically based on the type of build the given project hosts.

Existing lifecycle tasks are a terrible fit for this as things like `check` on an android project include far more
elements than we would want on a given `unit test` CI job. For example:

<details>
<summary>`./gradlew check --console=plain --dry-run`</summary>

```bash
./gradlew check --console=plain --dry-run

Type-safe project accessors is an incubating feature.
> Task :plugins:plugin1:compileKotlin UP-TO-DATE
> Task :plugins:plugin1:compileJava NO-SOURCE
> Task :plugins:plugin1:pluginDescriptors UP-TO-DATE
> Task :plugins:plugin1:processResources UP-TO-DATE
> Task :plugins:plugin1:classes UP-TO-DATE
> Task :plugins:plugin1:inspectClassesForKotlinIC UP-TO-DATE
> Task :plugins:plugin1:jar UP-TO-DATE
:project1:compileKotlin SKIPPED
:project1:compileJava SKIPPED
:project1:processResources SKIPPED
:project1:classes SKIPPED
:project1:compileTestKotlin SKIPPED
:project1:compileTestJava SKIPPED
:project1:processTestResources SKIPPED
:project1:testClasses SKIPPED
:project1:test SKIPPED
:project1:check SKIPPED
:project2:preBuild SKIPPED
:project2:preDebugBuild SKIPPED
:project2:compileDebugAidl SKIPPED
:project2:compileDebugRenderscript SKIPPED
:project2:generateDebugBuildConfig SKIPPED
:project2:generateDebugResValues SKIPPED
:project2:generateDebugResources SKIPPED
:project2:packageDebugResources SKIPPED
:project2:parseDebugLocalResources SKIPPED
:project2:processDebugManifest SKIPPED
:project2:generateDebugRFile SKIPPED
:project2:javaPreCompileDebug SKIPPED
:project2:compileDebugJavaWithJavac SKIPPED
:project2:processDebugJavaRes SKIPPED
:project2:bundleLibResDebug SKIPPED
:project2:bundleLibRuntimeToJarDebug SKIPPED
:project2:createFullJarDebug SKIPPED
:project2:mergeDebugJniLibFolders SKIPPED
:project2:mergeDebugNativeLibs SKIPPED
:project2:stripDebugDebugSymbols SKIPPED
:project2:copyDebugJniLibsProjectAndLocalJars SKIPPED
:project2:extractDebugAnnotations SKIPPED
:project2:extractDeepLinksForAarDebug SKIPPED
:project2:mergeDebugGeneratedProguardFiles SKIPPED
:project2:mergeDebugConsumerProguardFiles SKIPPED
:project2:mergeDebugShaders SKIPPED
:project2:compileDebugShaders SKIPPED
:project2:generateDebugAssets SKIPPED
:project2:packageDebugAssets SKIPPED
:project2:packageDebugRenderscript SKIPPED
:project2:prepareDebugArtProfile SKIPPED
:project2:prepareLintJarForPublish SKIPPED
:project2:mergeDebugJavaResource SKIPPED
:project2:syncDebugLibJars SKIPPED
:project2:writeDebugAarMetadata SKIPPED
:project2:bundleDebugLocalLintAar SKIPPED
:project2:preDebugAndroidTestBuild SKIPPED
:project2:extractDeepLinksDebug SKIPPED
:project2:processDebugAndroidTestManifest SKIPPED
:project2:compileDebugAndroidTestRenderscript SKIPPED
:project2:extractProguardFiles SKIPPED
:project2:generateDebugAndroidTestResValues SKIPPED
:project2:writeDebugLintModelMetadata SKIPPED
:project2:lintAnalyzeDebug SKIPPED
:project2:lintReportDebug SKIPPED
:project2:lintDebug SKIPPED
:project2:lint SKIPPED
:project2:bundleLibCompileToJarDebug SKIPPED
:project2:preDebugUnitTestBuild SKIPPED
:project2:generateDebugUnitTestStubRFile SKIPPED
:project2:javaPreCompileDebugUnitTest SKIPPED
:project2:compileDebugUnitTestJavaWithJavac SKIPPED
:project2:processDebugUnitTestJavaRes SKIPPED
:project2:testDebugUnitTest SKIPPED
:project2:preReleaseBuild SKIPPED
:project2:processReleaseJavaRes SKIPPED
:project2:bundleLibResRelease SKIPPED
:project2:compileReleaseAidl SKIPPED
:project2:compileReleaseRenderscript SKIPPED
:project2:generateReleaseBuildConfig SKIPPED
:project2:generateReleaseResValues SKIPPED
:project2:generateReleaseResources SKIPPED
:project2:packageReleaseResources SKIPPED
:project2:parseReleaseLocalResources SKIPPED
:project2:processReleaseManifest SKIPPED
:project2:generateReleaseRFile SKIPPED
:project2:javaPreCompileRelease SKIPPED
:project2:compileReleaseJavaWithJavac SKIPPED
:project2:bundleLibRuntimeToJarRelease SKIPPED
:project2:bundleLibCompileToJarRelease SKIPPED
:project2:preReleaseUnitTestBuild SKIPPED
:project2:generateReleaseUnitTestStubRFile SKIPPED
:project2:javaPreCompileReleaseUnitTest SKIPPED
:project2:compileReleaseUnitTestJavaWithJavac SKIPPED
:project2:processReleaseUnitTestJavaRes SKIPPED
:project2:testReleaseUnitTest SKIPPED
:project2:test SKIPPED
:project2:check SKIPPED

BUILD SUCCESSFUL in 1s
5 actionable tasks: 5 up-to-date
```

</details>
Shows us that we will trigger `lint` which also includes all variants of lint under it. 
It will trigger `test` which again hits both `debug` and `release` variants. We do get just the test task we want on the simpler `kotlin("jvm")` project though.

So instead we create custom lifecycle tasks to reduce the task explosion and make running what CI runs locally trivial.


<details>
<summary>`./gradlew unitTestSuite --console=plain --dry-run`</summary>

```bash
./gradlew unitTestSuite --console=plain --dry-run

Type-safe project accessors is an incubating feature.
> Task :plugins:plugin1:compileKotlin UP-TO-DATE
> Task :plugins:plugin1:compileJava NO-SOURCE
> Task :plugins:plugin1:pluginDescriptors UP-TO-DATE
> Task :plugins:plugin1:processResources UP-TO-DATE
> Task :plugins:plugin1:classes UP-TO-DATE
> Task :plugins:plugin1:inspectClassesForKotlinIC UP-TO-DATE
> Task :plugins:plugin1:jar UP-TO-DATE
:project1:compileKotlin SKIPPED
:project1:compileJava SKIPPED
:project1:processResources SKIPPED
:project1:classes SKIPPED
:project1:compileTestKotlin SKIPPED
:project1:compileTestJava SKIPPED
:project1:processTestResources SKIPPED
:project1:testClasses SKIPPED
:project1:test SKIPPED
:project1:unitTestSuite SKIPPED
:project2:preBuild SKIPPED
:project2:preDebugBuild SKIPPED
:project2:processDebugJavaRes SKIPPED
:project2:bundleLibResDebug SKIPPED
:project2:compileDebugAidl SKIPPED
:project2:compileDebugRenderscript SKIPPED
:project2:generateDebugBuildConfig SKIPPED
:project2:generateDebugResValues SKIPPED
:project2:generateDebugResources SKIPPED
:project2:packageDebugResources SKIPPED
:project2:parseDebugLocalResources SKIPPED
:project2:processDebugManifest SKIPPED
:project2:generateDebugRFile SKIPPED
:project2:javaPreCompileDebug SKIPPED
:project2:compileDebugJavaWithJavac SKIPPED
:project2:bundleLibRuntimeToJarDebug SKIPPED
:project2:bundleLibCompileToJarDebug SKIPPED
:project2:preDebugUnitTestBuild SKIPPED
:project2:generateDebugUnitTestStubRFile SKIPPED
:project2:javaPreCompileDebugUnitTest SKIPPED
:project2:compileDebugUnitTestJavaWithJavac SKIPPED
:project2:processDebugUnitTestJavaRes SKIPPED
:project2:testDebugUnitTest SKIPPED
:project2:unitTestSuite SKIPPED

BUILD SUCCESSFUL in 724ms
5 actionable tasks: 5 up-to-date
```

</details>

Just that easy and we are up and running. But now we can see a new problem! We have code that needs to be tested that
lives inside the included build that won't be invoked by the lifecycle task request. This sadly puts us back to square
one of what the lifecycle tasks try to solve. We have to explicitly list out every included build task.


<details>
<summary>`./gradlew unitTestSuite :included-project:unitTestSuite :plugins:plugin1:unitTestSuite --console=plain --dry-run`</summary>

```bash
StartParameter{taskRequests=[DefaultTaskExecutionRequest{args=[unitTestSuite, :included-project:unitTestSuite, :plugins:plugin1:unitTestSuite],projectPath='null',rootDir='null'}], excludedTaskNames=[], currentDir=/home/tjones/Source/composite-build-lifecycle-task-propagation, projectDir=null, projectProperties={}, systemPropertiesArgs={}, gradleUserHomeDir=/home/tjones/.gradle, gradleHome=/home/tjones/.gradle/wrapper/dists/gradle-7.5.1-all/1ehga6e77gqps5uk2kc5kf1vc/gradle-7.5.1, logLevel=LIFECYCLE, showStacktrace=INTERNAL_EXCEPTIONS, buildFile=null, initScripts=[], dryRun=true, rerunTasks=false, offline=false, refreshDependencies=false, parallelProjectExecution=false, configureOnDemand=false, maxWorkerCount=128, buildCacheEnabled=false, writeDependencyLocks=false, verificationMode=STRICT, refreshKeys=false}
StartParameter{taskRequests=[], excludedTaskNames=[], currentDir=/home/tjones/Source/composite-build-lifecycle-task-propagation/gradle/plugins, projectDir=null, projectProperties={}, systemPropertiesArgs={}, gradleUserHomeDir=/home/tjones/.gradle, gradleHome=/home/tjones/.gradle/wrapper/dists/gradle-7.5.1-all/1ehga6e77gqps5uk2kc5kf1vc/gradle-7.5.1, logLevel=LIFECYCLE, showStacktrace=INTERNAL_EXCEPTIONS, buildFile=null, initScripts=[], dryRun=false, rerunTasks=false, offline=false, refreshDependencies=false, parallelProjectExecution=false, configureOnDemand=false, maxWorkerCount=128, buildCacheEnabled=false, writeDependencyLocks=false, verificationMode=STRICT, refreshKeys=false}
StartParameter{taskRequests=[], excludedTaskNames=[], currentDir=/home/tjones/Source/composite-build-lifecycle-task-propagation/included-project, projectDir=null, projectProperties={}, systemPropertiesArgs={}, gradleUserHomeDir=/home/tjones/.gradle, gradleHome=/home/tjones/.gradle/wrapper/dists/gradle-7.5.1-all/1ehga6e77gqps5uk2kc5kf1vc/gradle-7.5.1, logLevel=LIFECYCLE, showStacktrace=INTERNAL_EXCEPTIONS, buildFile=null, initScripts=[], dryRun=false, rerunTasks=false, offline=false, refreshDependencies=false, parallelProjectExecution=false, configureOnDemand=false, maxWorkerCount=128, buildCacheEnabled=false, writeDependencyLocks=false, verificationMode=STRICT, refreshKeys=false}
Type-safe project accessors is an incubating feature.
> Task :plugins:plugin1:compileKotlin UP-TO-DATE
> Task :plugins:plugin1:compileJava NO-SOURCE
> Task :plugins:plugin1:pluginDescriptors UP-TO-DATE
> Task :plugins:plugin1:processResources UP-TO-DATE
> Task :plugins:plugin1:classes UP-TO-DATE
> Task :plugins:plugin1:inspectClassesForKotlinIC UP-TO-DATE
> Task :plugins:plugin1:jar UP-TO-DATE
:project1:compileKotlin SKIPPED
:project1:compileJava SKIPPED
:project1:processResources SKIPPED
:project1:classes SKIPPED
:project1:compileTestKotlin SKIPPED
:project1:compileTestJava SKIPPED
:project1:processTestResources SKIPPED
:project1:testClasses SKIPPED
:project1:test SKIPPED
:project1:unitTestSuite SKIPPED
:project2:preBuild SKIPPED
:project2:preDebugBuild SKIPPED
:project2:processDebugJavaRes SKIPPED
:project2:bundleLibResDebug SKIPPED
:project2:compileDebugAidl SKIPPED
:project2:compileDebugRenderscript SKIPPED
:project2:generateDebugBuildConfig SKIPPED
:project2:generateDebugResValues SKIPPED
:project2:generateDebugResources SKIPPED
:project2:packageDebugResources SKIPPED
:project2:parseDebugLocalResources SKIPPED
:project2:processDebugManifest SKIPPED
:project2:generateDebugRFile SKIPPED
:project2:javaPreCompileDebug SKIPPED
:project2:compileDebugJavaWithJavac SKIPPED
:project2:bundleLibRuntimeToJarDebug SKIPPED
:project2:bundleLibCompileToJarDebug SKIPPED
:project2:preDebugUnitTestBuild SKIPPED
:project2:generateDebugUnitTestStubRFile SKIPPED
:project2:javaPreCompileDebugUnitTest SKIPPED
:project2:compileDebugUnitTestJavaWithJavac SKIPPED
:project2:processDebugUnitTestJavaRes SKIPPED
:project2:testDebugUnitTest SKIPPED
:project2:unitTestSuite SKIPPED
> Task :included-project:compileKotlin NO-SOURCE
> Task :included-project:compileJava NO-SOURCE
> Task :included-project:processResources NO-SOURCE
> Task :included-project:classes UP-TO-DATE
> Task :included-project:compileTestKotlin NO-SOURCE
> Task :included-project:compileTestJava NO-SOURCE
> Task :included-project:processTestResources NO-SOURCE
> Task :included-project:testClasses UP-TO-DATE
> Task :included-project:test NO-SOURCE
> Task :included-project:unitTestSuite UP-TO-DATE
> Task :plugins:plugin1:compileTestKotlin UP-TO-DATE
> Task :plugins:plugin1:pluginUnderTestMetadata UP-TO-DATE
> Task :plugins:plugin1:compileTestJava NO-SOURCE
> Task :plugins:plugin1:processTestResources NO-SOURCE
> Task :plugins:plugin1:testClasses UP-TO-DATE
> Task :plugins:plugin1:test UP-TO-DATE
> Task :plugins:plugin1:unitTestSuite UP-TO-DATE

BUILD SUCCESSFUL in 844ms
8 actionable tasks: 8 up-to-date

```

</details>

Oh, fun! It looks like `--dry-run` doesn't behave quite how we would have wanted either. That is probably something we
can look past.

I really don't want to relocate these plugins out to a new repo just yet. It might be time to actually have gradle
generate my entire CI pipeline configuration? Or is there some gradle magic we can work in to treat certain tasks in a
special way when used in the context of a composite build.