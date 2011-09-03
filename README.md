Jenkins ANSI Color Plugin
=========================

This plugin colorizes ANSI text in Jenkins build console output. 
It supports ANSI colors, bold and underline text escape codes.

Installation
============

The simplest way is by going to your installation's management screen and clicking *Manage Plugins*. 
The web interface will then download *.hpi files from here, and you will just need to restart your 
Jenkins to pick up the changes.

Configure your project, check *Color ANSI Console Output*. Sit back and enjoy.

See [this page](https://wiki.jenkins-ci.org/display/JENKINS/Plugins#Plugins-Howtoinstallplugins) 
for more information.

License
=======

The ANSI Color Plugin is licensed under the MIT License.

It uses [JANSI](https://github.com/fusesource/jansi/) (Apache 2.0 License).

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

To build this project in Eclipse, prepare your environment and add the maven repo like this.

	mvn -DdownloadSources=true -DdownloadJavadocs=true -DoutputDirectory=target/eclipse-classes eclipse:eclipse
	mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo

Finally, import the project into Eclipse.

Debugging the Plugin
--------------------

	export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
	mvn hpi:run

Hudson is now running on http://localhost:8080/ and a debugger can be attached to 8000.
