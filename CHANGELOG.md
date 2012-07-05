0.3.2 (Next)
============

* Using `commons-lang` to HTML-encode color output - [@dblock](https://github.com/dblock).
* Failure to annotate console output will be logged as a warning - [@dblock](https://github.com/dblock).

0.3.1
=====

* Fix: broken colored console output from jobs that used a previous version of AnsiColor - [#10](https://github.com/dblock/jenkins-ansicolor-plugin/issues/10) [@dblock](https://github.com/dblock).

0.3.0
=====

* Upgraded to JANSI 1.9 - [@chirino](https://github.com/chirino).
* Fix: improper handling of default background and foreground color reset - [#5](https://github.com/dblock/jenkins-ansicolor-plugin/issues/5) [@dblock](https://github.com/dblock).
* Fix: corrupt strings that contain non-ASCII characters - [#7](https://github.com/dblock/jenkins-ansicolor-plugin/pull/7) [@takai](https://github.com/takai).
* Added support for configuring color mapping - [#9](https://github.com/dblock/jenkins-ansicolor-plugin/pull/9) [@harrah](https://github.com/harrah).

0.2.1
=====

* Upgraded to JANSI 1.7

0.2.0
=====

* Added support for concealed ANSI blocks that produced garbled output.

0.1.2
=====

* Official release that supports ANSI color, bold and underline.

