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

You must have r/w permissions to [github.com/jenkinsci/ansicolor-plugin](https://github.com/jenkinsci/ansicolor-plugin) under the same username.

### Release

Run tests, check that all tests succeed locally.

```
mvn test
```

Check that the last build succeeded in [Travis CI](https://travis-ci.org/jenkinsci/ansicolor-plugin).

In [pom.xml](pom.xml), remove the `-SNAPSHOT` postfix from the version string.

``` xml
<artifactId>ansicolor</artifactId>
<packaging>hpi</packaging>
<version>0.4.1</version>
```

*  Leave the version as it is (just removing `-SNAPSHOT`) if the release has bug fixes and/or very minor features, only.
*  Increment the second number if the release contains major features or breaking API changes (eg. change `0.4.1` to `0.5.0`).

Change "Next Release" in [CHANGELOG.md](CHANGELOG.md) to the new version.

```
0.4.1 (12/11/2014)
==================
```

Remove the line with "Your contribution here.", since there will be no more contributions to this release.

Commit your changes.

```
git add CHANGELOG.md
git commit -m "Preparing for release, 0.4.1."
git push origin master
```

Make a release.

```
$ mvn release:prepare release:perform
```

### Prepare for the Next Version

In [pom.xml](pom.xml), increment the third number of the version string and add the `-SNAPSHOT` postfix (eg. change `0.4.1` to `0.4.2-SNAPSHOT`).

``` xml
<artifactId>ansicolor</artifactId>
<packaging>hpi</packaging>
<version>0.4.2-SNAPSHOT</version>
```


Add the next release to [CHANGELOG.md](CHANGELOG.md).

```
0.4.2 (Next)
============

* Your contribution here.
```

Commit your changes.

```
git add CHANGELOG.md
git commit -m "Preparing for next development iteration."
git push origin master
```
