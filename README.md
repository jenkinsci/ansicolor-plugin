Jenkins ANSI Color Plugin
=========================

Work in progress. Currently this plugin strips ANSI codes.

Once finished it should appear on http://wiki.jenkins-ci.org/display/JENKINS/AnsiColor

Installation
============

The simplest way is by going to your installation's management screen and clicking *Manage Plugins*. 
The web interface will then download *.hpi files from here, and you will just need to restart your 
Jenkins to pick up the changes.

See [this page](https://wiki.jenkins-ci.org/display/JENKINS/Plugins#Plugins-Howtoinstallplugins) 
for more information.

Contributing
============

* Fork the project on [Github](https://github.com/dblock/jenkins-ansicolor-plugin)
* Make your feature addition or bug fix, write tests, commit.
* Send me a pull request. Bonus points for topic branches.

Build AnsiColor
---------------

	mvn install

Setting Up Eclipse
------------------

To build this project in Eclipse, import it and then add the maven repo like this.

	mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo

Debugging the Plugin
--------------------

	export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
	mvn hpi:run

Hudson is now running on http://localhost:8080/ and a debugger can be attached to 8000.
