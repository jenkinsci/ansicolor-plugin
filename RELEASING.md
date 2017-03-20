Releasing AnsiColor
===================

There're no particular rules about when to release AnsiColor. Release bug fixes frequently, features not so frequently and breaking API changes rarely.

### Access

Make sure you have a [Jenkins-CI account](https://jenkins-ci.org/account) configured in `~/.m2/settings.xml`. You can get the encrypted password from [here](https://repo.jenkins-ci.org/webapp/#/profile) - enter your password and click `Unlock`.

```xml
<settings>
  <pluginGroups>
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
  </pluginGroups>
  <servers>
    <server>
      <id>repo.jenkins-ci.org</id>
      <username>...</username>
      <password>...</password>
    </server>
  </servers>
 </settings>
```

You must have r/w permissions to [github.com/jenkinsci/ansicolor-plugin](https://github.com/jenkinsci/ansicolor-plugin) under the same username
and you need to be listed as developer in [jenkins plugin-ansicolor.yml](https://github.com/jenkins-infra/repository-permissions-updater/blob/master/permissions/plugin-ansicolor.yml) (see also [jenkins #258](https://github.com/jenkins-infra/repository-permissions-updater/pull/258)).

### Release

Run tests, check that all tests succeed locally.

```
mvn test
```

Check that the last build succeeded in [Travis CI](https://travis-ci.org/jenkinsci/ansicolor-plugin).

Ensure that the version in [pom.xml](pom.xml) is correct and ends with `-SNAPSHOT`.

``` xml
<artifactId>ansicolor</artifactId>
<packaging>hpi</packaging>
<version>0.4.1-SNAPSHOT</version>
```

Change "Next Release" in [CHANGELOG.md](CHANGELOG.md) to the new version.

```
0.4.1 (12/11/2014)
==================
```

Remove the line with "Your contribution here.", since there will be no more contributions to this release.

Commit your changes.

```
git add CHANGELOG.md
git commit -m "Preparing for release, 0.4.1"
git push origin master
```

Make sure that your working directory is clean (no uncommited changes).

Make a release.

```
$ mvn release:prepare release:perform
```

The `mvn release:prepare` will interactively prompt you for version numbers:

    [INFO] Checking dependencies and plugins for snapshots ...
    What is the release version for "AnsiColor"? (org.jenkins-ci.plugins:ansicolor) 0.4.1: :
    What is SCM release tag or label for "AnsiColor"? (org.jenkins-ci.plugins:ansicolor) ansicolor-0.4.1: :
    What is the new development version for "AnsiColor"? (org.jenkins-ci.plugins:ansicolor) 0.4.2-SNAPSHOT: :

Please read [Maven Release Plugin](http://maven.apache.org/maven-release/maven-release-plugin/examples/prepare-release.html).

*  Leave the version as provided (plugin will just remove the `-SNAPSHOT` postfix) if the release has bug fixes and/or very minor features, only (e.g. `0.4.1`).
*  Increment the second number if the release contains major features or breaking API changes (eg. change `0.4.1` to `0.5.0`).

### Prepare for the Next Version

Add the next release to [CHANGELOG.md](CHANGELOG.md).

```
0.4.2 (Next)
============

* Your contribution here.
```

Commit your changes.

```
git add CHANGELOG.md
git commit -m "Preparing for next development iteration"
git push origin master
```
