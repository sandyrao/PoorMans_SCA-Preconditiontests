Poor Man's SCA-Preconditiontests
=====================

PMD, Checkstyle, Findbugs port to JUnit tests for comparison against baselines. Intended to be used for precondition checks as JUnit test cases before checkin to an SCM like Jazz

Building artifacts
------------------
To generate jar files, run 

`mvn package`

To run the tests

-add *PreconditionChecks-0.x.x.jar* to your build path
-run Junit testcase PreconditionsCheckTestSuite.java

To run as a Jazz CLM precondition, run the JUnit test from within Eclipse.
Refer link to setup "Require JUnit run, Precondition"<br/>
[Source control process recipes for Rational Team Concert](https://jazz.net/library/article/1075)

Thanks to..
-----------
<a href="http://structure101.com">![Structure101](http://structure101.com/images/s101_170.png)</a>
Special Thanks to Structure101 for lending a copy of their Architecture Development Environment to build my project.  
Find more info on Structure101 at <http://structure101.com>  
