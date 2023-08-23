Contributing
============

You're encouraged to submit [pull requests](https://github.com/jenkinsci/ansicolor-plugin/pulls), [propose features and discuss issues](https://github.com/jenkinsci/ansicolor-plugin/issues).

#### Writing Jenkins Plugins

Before you begin, check out the [Jenkins Plugin Tutorial](https://www.jenkins.io/doc/developer/tutorial/).

#### Fork the Project

Fork the [project on Github](https://github.com/jenkinsci/ansicolor-plugin) and check out your copy.

```
git clone https://github.com/contributor/ansicolor-plugin.git
cd ansicolor-plugin
git remote add upstream https://github.com/jenkinsci/ansicolor-plugin.git
```

#### Create a Topic Branch

Make sure your fork is up-to-date and create a topic branch for your feature or bug fix.

```
git checkout master
git pull upstream master
git checkout -b my-feature-branch
```

#### Run Maven

Build and run tests with `mvn`, make sure it outputs BUILD SUCCESS.

#### Write Tests

Write a test that reproduces the problem you're trying to fix or describes a feature that you want to build.

We definitely appreciate pull requests that highlight or reproduce a problem, even without a fix. We don't accept pull requests without tests.

#### Write Code

Implement your feature or bug fix.

Make sure that you can build the project and run all tests successfully.

#### Write Documentation

Document any external behavior in the [README](README.md).

#### Commit Changes

Make sure git knows your name and email address:

```
git config --global user.name "Your Name"
git config --global user.email "contributor@example.com"
```

Writing good commit logs is important. A commit log should describe what changed and why.

```
git add ...
git commit
```

#### Push

```
git push origin my-feature-branch
```

#### Make a Pull Request

Go to https://github.com/contributor/ansicolor-plugin and select your feature branch. Click the 'Pull Request' button and fill out the form. Pull requests are usually reviewed within a few days.

#### Rebase

If you've been working on a change for a while, rebase with upstream/master.

```
git fetch upstream
git rebase upstream/master
git push origin my-feature-branch -f
```

#### Check on Your Pull Request

Go back to your pull request after a few minutes and see whether it passed CI tests. Everything should look green, otherwise fix issues and amend your commit as described above.

#### Be Patient

It's likely that your change will not be merged and that the nitpicky maintainers will ask you to do more, or fix seemingly benign problems. Hang on there!

#### Thank You

Please do know that we really appreciate and value your time and work. We love you, really.
