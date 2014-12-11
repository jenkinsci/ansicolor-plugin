Releasing AnsiColor
===================

There're no particular rules about when to release AnsiColor. Release bug fixes frequently, features not so frequently and breaking API changes rarely.

### Access

Make sure you have a [Jenkins-CI account](https://jenkins-ci.org/account) configured in `~/.m2/settings.xml`.

```xml
<settings>
  <pluginGroups>
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
  </pluginGroups>
  <servers>
    <server>
      <id>maven.jenkins-ci.org</id>
      <username>...</username>
      <password>...</password>
    </server>
  </servers>
 </settings>
```

You must have r/w permissions to [github.com/dblock/jenkins-ansicolor-plugin](https://github.com/dblock/jenkins-ansicolor-plugin) under the same username.

### Release

Run tests, check that all tests succeed locally.

```
mvn test
```

Check that the last build succeeded in [Travis CI](https://travis-ci.org/dblock/jenkins-ansicolor-plugin).

Ensure that the version in [pom.xml](pom.xml) is correct and ends with `-SNAPSHOT`.

``` xml
<artifactId>ansicolor</artifactId>
<packaging>hpi</packaging>
<version>0.4.1-SNAPSHOT</version>
```

*  Increment the third number if the release has bug fixes and/or very minor features, only (eg. change `0.4.1` to `0.4.2`).
*  Increment the second number if the release contains major features or breaking API changes (eg. change `0.4.1` to `0.5.0`).

Change "Next Release" in [CHANGELOG.md](CHANGELOG.md) to the new version.

```
0.4.1 (12/11/2014)
=================
```

Remove the line with "Your contribution here.", since there will be no more contributions to this release.

Make a release.

```
$ mvn release:prepare release:perform
```

### Prepare for the Next Version

Add the next release to [CHANGELOG.md](CHANGELOG.md).

```
Next Release
============

* Your contribution here.
```

Commit your changes.

```
git add CHANGELOG.md README.md
git commit -m "Preparing for next development iteration."
git push origin master
```
