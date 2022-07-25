1.0.2 (2022/07/24)
==================

* [#236](https://github.com/jenkinsci/ansicolor-plugin/pull/236): Supress a failing test - [@jglick](https://github.com/jglick).
* [#238](https://github.com/jenkinsci/ansicolor-plugin/pull/238): EOL JSR 305 - [@basil](https://github.com/basil).
* [#240](https://github.com/jenkinsci/ansicolor-plugin/pull/240): Remove javaLevel - [@basil](https://github.com/basil).
* [#241](https://github.com/jenkinsci/ansicolor-plugin/pull/241): Update plugin parent POM and plugin BOM - [@basil](https://github.com/basil).


1.0.1 (2021/11/08)
==================

* [JENKINS-66684](https://issues.jenkins.io/browse/JENKINS-66684) Pipeline syntax snippet generator fails with ansicolor plugin 1.0.0 - [@tszmytka](https://github.com/tszmytka).


1.0.0 (2021/05/16)
==================

* [#219](https://github.com/jenkinsci/ansicolor-plugin/pull/219): Set env var TERM also if a default color map is used - [@tszmytka](https://github.com/tszmytka).
* [#226](https://github.com/jenkinsci/ansicolor-plugin/pull/226): Work around withMaven step not passing on log line metadata - [@tszmytka](https://github.com/tszmytka).
* [#225](https://github.com/jenkinsci/ansicolor-plugin/pull/225): Work around logstash-plugin hiding some log lines from ansicolor - [@tszmytka](https://github.com/tszmytka).
* [#227](https://github.com/jenkinsci/ansicolor-plugin/pull/227): Trigger tailed log output colorization also after Jenkins restart - [@tszmytka](https://github.com/tszmytka).


0.7.5 (2021/01/31)
==================

* [#217](https://github.com/jenkinsci/ansicolor-plugin/pull/217): Reflect functionality changes in tests - [@tszmytka](https://github.com/tszmytka).


0.7.4 (2021/01/16)
==================

* [#213](https://github.com/jenkinsci/ansicolor-plugin/pull/213): Remove tabular markup on new Jenkins core - [@timja](https://github.com/timja).
* [#214](https://github.com/jenkinsci/ansicolor-plugin/pull/214): Render logs correctly also in Jenkins versions greater than 2.260 - [@tszmytka](https://github.com/tszmytka).


0.7.3 (2020/09/15)
==================

* [#202](https://github.com/jenkinsci/ansicolor-/20)/plugin/pull: Render logs correctly for long running jobs which generate lots of output - [@tszmytka](https://github.com/tszmytka).
* [#203](https://github.com/jenkinsci/ansicolor-plugin/pull/203): Interaction with kubernetes plugin - [@tszmytka](https://github.com/tszmytka).


0.7.2 (2020/08/01)
==================

* [#197](https://github.com/jenkinsci/ansicolor-plugin/pull/197): Recognize timestamper's GlobalDecorator from extension list  - [@tszmytka](https://github.com/tszmytka).
* [#185](https://github.com/jenkinsci/ansicolor-plugin/pull/185): Render escape codes correctly in shortlog - [@tszmytka](https://github.com/tszmytka).


0.7.1 (2020/07/13)
==================

* [#188](https://github.com/jenkinsci/ansicolor-plugin/pull/188): Allow rendering more complex sgrs - [@tszmytka](https://github.com/tszmytka).
* [#190](https://github.com/jenkinsci/ansicolor-plugin/pull/190): Don't leak formatting to metadata console lines - [@tszmytka](https://github.com/tszmytka).
* [#195](https://github.com/jenkinsci/ansicolor-plugin/pull/195): Running on agent - [@tszmytka](https://github.com/tszmytka).
* [#196](https://github.com/jenkinsci/ansicolor-plugin/pull/196): Interaction with timestamper - [@tszmytka](https://github.com/tszmytka).

0.7.0 (2020/05/23)
==================

* [#173](https://github.com/jenkinsci/ansicolor-plugin/pull/173): Fix for SGR Normal intensity not handled correctly - [@tszmytka](https://github.com/tszmytka).
* [#176](https://github.com/jenkinsci/ansicolor-plugin/pull/176): Ensure extended color SGRs are recognized correctly - [@tszmytka](https://github.com/tszmytka).
* [#156](https://github.com/jenkinsci/ansicolor-plugin/pull/156): Improve documentation - [@tszmytka](https://github.com/tszmytka).
* [#181](https://github.com/jenkinsci/ansicolor-plugin/pull/181): Re-implement support for multiple ansiColor steps within one pipeline script - [@tszmytka](https://github.com/tszmytka).

0.6.3 (2020/02/24)
==================

* [#163](https://github.com/jenkinsci/ansicolor-plugin/pull/163): Use OpenJDK on Trusty Linux in Travis-CI - [@dblock](https://github.com/dblock).
* [#163](https://github.com/jenkinsci/ansicolor-plugin/pull/163): Cache maven installation in Travis-CI - [@dblock](https://github.com/dblock).
* [#165](https://github.com/jenkinsci/ansicolor-plugin/pull/165): Allow for specifying a global color map - [@kcboschert](https://github.com/kcboschert).
* [#168](https://github.com/jenkinsci/ansicolor-plugin/pull/168): Fix for working with moving sequences and other invisible escape codes - [@tszmytka](https://github.com/tszmytka)
* [#169](https://github.com/jenkinsci/ansicolor-plugin/pull/169): Set up config and jenkins pipeline for testing/building the plugin on ci.jenkins.io - [@tszmytka](https://github.com/tszmytka)
* [#171](https://github.com/jenkinsci/ansicolor-plugin/pull/171): Display colors without reloading page - [@jglick](https://github.com/jglick).

0.6.2 (2019/01/31)
==================

* [#137](https://github.com/jenkinsci/ansicolor-plugin/pull/137): Allow escape sequences to span multiple lines and support color maps that set default background/foreground colors - [@dwnusbaum](https://github.com/dwnusbaum).
* [#147](https://github.com/jenkinsci/ansicolor-plugin/pull/147): Handle redundant occurrences of SGR0 in build logs - [@dwnusbaum](https://github.com/dwnusbaum).

0.6.1 (2019/01/04)
==================

* [#139](https://github.com/jenkinsci/ansicolor-plugin/issues/139)/[#143](https://github.com/jenkinsci/ansicolor-plugin/pull/143): Prevent an `IndexOutOfBoundsException` from being thrown when two or more non-ASCII-compatible characters are present in colored text - [@dwnusbaum](https://github.com/dwnusbaum).

0.6.0 (2018/11/14)
==================

* [#132](https://github.com/jenkinsci/ansicolor-plugin/pull/132): Reworked implementation to add markup on display, not to the actual build log - [@jglick](https://github.com/jglick).

0.5.3 (2018/11/06)
==================

* [#107](https://github.com/jenkinsci/ansicolor-plugin/pull/107): Removing startup banner - [@jglick](https://github.com/jglick).
* [#128](https://github.com/jenkinsci/ansicolor-plugin/pull/128): Restoring limited compatibility for coloration generated remotely by Pipeline builds on agents - [@jglick](https://github.com/jglick).

0.5.2 (2017/08/17)
==================

* [#111](https://github.com/jenkinsci/ansicolor-plugin/pull/111): Filter out escape sequence 'character set' select - [@pmhahn](https://github.com/pmhahn).
* [#112](https://github.com/jenkinsci/ansicolor-plugin/pull/112): Filter out 'font select' escape sequence - [@pmhahn](https://github.com/pmhahn).

0.5.1 (2017/08/10)
==================

* [#100](https://github.com/jenkinsci/ansicolor-plugin/pull/100): Migrated hosting to github.com/jenkinsci - [@JoeMerten](https://github.com/JoeMerten) & [@dblock](https://github.com/dblock).
* [#101](https://github.com/jenkinsci/ansicolor-plugin/pull/101): Some exceptions during plugin install and following jenkins start - [@JoeMerten](https://github.com/JoeMerten).
* [#109](https://github.com/jenkinsci/ansicolor-plugin/pull/109): Set `TERM` environment variable inside of the `ansiColor` step when using Jenkins pipelines - [@mkobit](https://github.com/mkobit).

0.5.0  (2017/03/18)
===================

* [#90](https://github.com/jenkinsci/ansicolor-plugin/pull/90): Added missing handling for ATTRIBUTE_CONCEAL_OFF - [@JoeMerten](https://github.com/JoeMerten).
* [#90](https://github.com/jenkinsci/ansicolor-plugin/pull/90): Added support for italic, strikeout, framed and overline attributes - [@JoeMerten](https://github.com/JoeMerten).
* [#92](https://github.com/jenkinsci/ansicolor-plugin/pull/92): Fixed high intensity colors for both foreground and background - [@JoeMerten](https://github.com/JoeMerten).
* [#94](https://github.com/jenkinsci/ansicolor-plugin/pull/94): Added support for negative (inverse colors) attribute - [@JoeMerten](https://github.com/JoeMerten).
* [#96](https://github.com/jenkinsci/ansicolor-plugin/pull/96): Added support for xterm 256 colors and 24 bit colors - [@JoeMerten](https://github.com/JoeMerten).
* [#98](https://github.com/jenkinsci/ansicolor-plugin/pull/98): Get rid of jansi version dependendy - [@JoeMerten](https://github.com/JoeMerten).

0.4.3 (2016/11/20)
==================

* [#83](https://github.com/jenkinsci/ansicolor-plugin/pull/83): Added custom pipeline step - [@cpoenisch](https://github.com/cpoenisch).
* [#73](https://github.com/jenkinsci/ansicolor-plugin/pull/73): Promote pipeline configuration in README - [@abrom](https://github.com/abrom).
* [#72](https://github.com/jenkinsci/ansicolor-plugin/pull/72): Support high intensity ANSI colors - [@marlene01](https://github.com/marlene01).
* [#66](https://github.com/jenkinsci/ansicolor-plugin/pull/66): Improved snippet generation - [@qvicksilver](https://github.com/qvicksilver).

0.4.2 (2015/10/29)
==================

* [#24](https://github.com/jenkinsci/ansicolor-plugin/issues/24): Configurable default fg/bg colors - [@ejelly](https://github.com/ejelly).
* [#50](https://github.com/jenkinsci/ansicolor-plugin/issues/50): SimpleBuildWrapper implementation for use in workflows - [@qvicksilver](https://github.com/qvicksilver).

0.4.1 (2014/12/11)
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
