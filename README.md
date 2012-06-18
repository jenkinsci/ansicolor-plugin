Jenkins ANSI Color Plugin
=========================

This plugin adds support for ANSI escape sequences, including color, to Console Output.

This plugin is available [here](http://maven.jenkins-ci.org:8081/content/repositories/releases/org/jvnet/hudson/plugins/ansicolor/) 
and has [a page](https://wiki.jenkins-ci.org/display/JENKINS/AnsiColor+Plugin) on the Jenkins Wiki.

Install
=======

![install](https://github.com/dblock/jenkins-ansicolor-plugin/raw/master/images/ansicolor-install.png "Install AnsiColor")

Enable
======

![enable](https://github.com/dblock/jenkins-ansicolor-plugin/raw/master/images/ansicolor-enable.png "Enable AnsiColor")

Color!
======

![color](https://github.com/dblock/jenkins-ansicolor-plugin/raw/master/images/ansicolor.png "Color with AnsiColor")

Customize
======

![color](https://github.com/dblock/jenkins-ansicolor-plugin/raw/master/images/ansicolor-config.png "Customize colors used by AnsiColor")

Misc
====

Colorizing Ruby RSpec Output
----------------------------

RSpec formatters detect whether RSpec is running in a terminal or not, therefore suppressing color output under Jenkins. Specify `--colour` in your `.rspec` file or options, along with the following setup in `spec_helper.rb`.

``` ruby
RSpec.configure do |config|
 config.tty = true
end
```

License
=======

The ANSI Color Plugin is licensed under the MIT License.

It uses [JANSI](https://github.com/fusesource/jansi/) (Apache 2.0 License).

Contributing
============

* Fork the project on [Github](https://github.com/dblock/jenkins-ansicolor-plugin)
* Make your feature addition or bug fix, write tests, commit.
* Send me a pull request. Bonus points for topic branches.
