0.5.3 (Next)
============

* Your contribution here.
* [#107](https://github.com/jenkinsci/ansicolor-plugin/pull/107): Removing startup banner - [@jglick](https://github.com/jglick).
* [#128](https://github.com/jenkinsci/ansicolor-plugin/pull/128): Restoring limited compatibility for coloration generated remotely by Pipeline builds on agents - [@jglick](https://github.com/jglick).

0.5.2 (08/17/2017)
============

* [#111](https://github.com/jenkinsci/ansicolor-plugin/pull/111): Filter out escape sequence 'character set' select - [@pmhahn](https://github.com/pmhahn).
* [#112](https://github.com/jenkinsci/ansicolor-plugin/pull/112): Filter out 'font select' escape sequence - [@pmhahn](https://github.com/pmhahn).

0.5.1 (08/10/2017)
==================

* [#100](https://github.com/jenkinsci/ansicolor-plugin/pull/100): Migrated hosting to github.com/jenkinsci - [@JoeMerten](https://github.com/JoeMerten) & [@dblock](https://github.com/dblock).
* [#101](https://github.com/jenkinsci/ansicolor-plugin/pull/101): Some exceptions during plugin install and following jenkins start - [@JoeMerten](https://github.com/JoeMerten).
* [#109](https://github.com/jenkinsci/ansicolor-plugin/pull/109): Set `TERM` environment variable inside of the `ansiColor` step when using Jenkins pipelines - [@mkobit](https://github.com/mkobit).

0.5.0  (03/18/2017)
===================

* [#90](https://github.com/jenkinsci/ansicolor-plugin/pull/90): Added missing handling for ATTRIBUTE_CONCEAL_OFF - [@JoeMerten](https://github.com/JoeMerten).
* [#90](https://github.com/jenkinsci/ansicolor-plugin/pull/90): Added support for italic, strikeout, framed and overline attributes - [@JoeMerten](https://github.com/JoeMerten).
* [#92](https://github.com/jenkinsci/ansicolor-plugin/pull/92): Fixed high intensity colors for both foreground and background - [@JoeMerten](https://github.com/JoeMerten).
* [#94](https://github.com/jenkinsci/ansicolor-plugin/pull/94): Added support for negative (inverse colors) attribute - [@JoeMerten](https://github.com/JoeMerten).
* [#96](https://github.com/jenkinsci/ansicolor-plugin/pull/96): Added support for xterm 256 colors and 24 bit colors - [@JoeMerten](https://github.com/JoeMerten).
* [#98](https://github.com/jenkinsci/ansicolor-plugin/pull/98): Get rid of jansi version dependendy - [@JoeMerten](https://github.com/JoeMerten).

0.4.3 (11/20/2016)
==================

* [#83](https://github.com/jenkinsci/ansicolor-plugin/pull/83): Added custom pipeline step - [@cpoenisch](https://github.com/cpoenisch).
* [#73](https://github.com/jenkinsci/ansicolor-plugin/pull/73): Promote pipeline configuration in README - [@abrom](https://github.com/abrom).
* [#72](https://github.com/jenkinsci/ansicolor-plugin/pull/72): Support high intensity ANSI colors - [@marlene01](https://github.com/marlene01).
* [#66](https://github.com/jenkinsci/ansicolor-plugin/pull/66): Improved snippet generation - [@qvicksilver](https://github.com/qvicksilver).

0.4.2 (10/29/2015)
==================

* [#24](https://github.com/jenkinsci/ansicolor-plugin/issues/24): Configurable default fg/bg colors - [@ejelly](https://github.com/ejelly).
* [#50](https://github.com/jenkinsci/ansicolor-plugin/issues/50): SimpleBuildWrapper implementation for use in workflows - [@qvicksilver](https://github.com/qvicksilver).

0.4.1 (12/11/2014)
==================

* [#28](https://github.com/jenkinsci/ansicolor-plugin/pull/28): Added support for default text and background color sequences (resetting color changes) - [@ejelly](https://github.com/ejelly).
* [#32](https://github.com/jenkinsci/ansicolor-plugin/pull/32): Added a new color map, gnome-terminal - [@javawizard](https://github.com/javawizard).

0.4.0
=====

* [#17](https://github.com/jenkinsci/ansicolor-plugin/issues/17), [#26](https://github.com/jenkinsci/ansicolor-plugin/pull/26): Log text is not replaced with markup anymore, just supplemented - [@ejelly](https://github.com/ejelly).
* [#19](https://github.com/jenkinsci/ansicolor-plugin/issues/19): Actually remove escape sequences from log stream - [@ejelly](https://github.com/ejelly).
* Properly nest HTML elements - [@ejelly](https://github.com/ejelly).
* Added support for ANSI underline double - [@ejelly](https://github.com/ejelly).
* Using `commons-lang` to HTML-encode color output - [@dblock](https://github.com/dblock).
* Failure to annotate console output will be logged as a warning - [@dblock](https://github.com/dblock).

0.3.1
=====

* [#10](https://github.com/jenkinsci/ansicolor-plugin/issues/10): Fix: broken colored console output from jobs that used a previous version of AnsiColor - [@dblock](https://github.com/dblock).

0.3.0
=====

* Upgraded to JANSI 1.9 - [@chirino](https://github.com/chirino).
* [#5](https://github.com/jenkinsci/ansicolor-plugin/issues/5): Fix: improper handling of default background and foreground color reset - [@dblock](https://github.com/dblock).
* [#7](https://github.com/jenkinsci/ansicolor-plugin/pull/7): Fix: corrupt strings that contain non-ASCII characters - [@takai](https://github.com/takai).
* [#9](https://github.com/jenkinsci/ansicolor-plugin/pull/9): Added support for configuring color mapping - [@harrah](https://github.com/harrah).

0.2.1
=====

* Upgraded to JANSI 1.7

0.2.0
=====

* Added support for concealed ANSI blocks that produced garbled output.

0.1.2
=====

* Official release that supports ANSI color, bold and underline.
