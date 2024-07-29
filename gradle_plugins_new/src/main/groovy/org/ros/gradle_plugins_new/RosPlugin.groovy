package org.ros.gradle_plugins_new;

import org.gradle.api.*;
import org.gradle.api.publish.maven.MavenPublication;

/**
 * Configures a Java project for use with ROS.
 *
 * - project.ros.mavenPath : location of local ros maven repositories (in your chained workspaces)
 * - project.ros.mavenDeploymentRepository : location of the ros maven repository you will publish to
 *
 * It also performs the following actions
 *
 * - checks and makes sure the maven plugin is running
 * - constructs the sequence of dependent maven repos (local ros maven repos, mavenLocal, external ros maven repo)
 * - configures the uploadArchives for artifact deployment to the local ros maven repo (devel/share/maven)
 */
class RosPlugin implements Plugin<Project> {

  def void apply(Project project) {
    project.apply plugin: "maven-publish"

    project.extensions.create("ros", RosPluginExtension)

    // project.ros.mavenRepository = System.getenv("ROS_MAVEN_REPOSITORY")
    project.ros.mavenRepository = "https://github.com/hedgehog412/rosjava_mvn_repo/tree/master"
    project.ros.mavenDeploymentRepository = "https://github.com/hedgehog412/rosjava_mvn_repo/tree/master"
    String mavenPath = System.getenv("ROS_MAVEN_PATH")
    if (mavenPath != null) {
      project.ros.mavenPath = mavenPath.tokenize(":")
    }
    project.repositories {
      if (project.ros.mavenPath != null) {
        project.ros.mavenPath.each { path ->
          maven {
            url project.uri(path)
            allowInsecureProtocol = true
          }
        }
      }
      if (project.ros.mavenRepository != null) {
        maven {
          url project.ros.mavenRepository
          allowInsecureProtocol = true
        }
      }
      /* 
       * This will often be the same as ROS_MAVEN_REPOSITORY, but this way it lets a user
       * provide a repository of their own via the environment variable and use this as a fallback.
       */
      maven {
        url "https://github.com/hedgehog412/rosjava_mvn_repo/raw/master"
        allowInsecureProtocol = true
      }
      mavenLocal()
      maven {
        url "http://repository.springsource.com/maven/bundles/release"
        allowInsecureProtocol = true
      }
      maven {
        url "http://repository.springsource.com/maven/bundles/external"
        allowInsecureProtocol = true
      }
      mavenCentral()
    }
  }
}

/* http://www.gradle.org/docs/nightly/dsl/org.gradle.api.plugins.ExtensionAware.html */
class RosPluginExtension {
  String mavenRepository
  String mavenDeploymentRepository
  List<String> mavenPath

  RosPluginExtension() {
    /* Initialising the strings here gets rid of the dynamic property deprecated warnings. */
    this.mavenDeploymentRepository = ""
    this.mavenRepository = ""
  }
}
