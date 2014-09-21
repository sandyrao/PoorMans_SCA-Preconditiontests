Poor Man's SCA-Preconditiontests
=====================

PMD, Checkstyle, Findbugs port to JUnit tests for comparison against baselines. Intended to be used for precondition checks as JUnit test cases before checkin to an SCM like Jazz

Building artifacts
------------------
To generate jar files, run 

`mvn package`

To run the tests

-add *preconditions.jar* to your build path
-run Junit testcase PreconditionsCheckTestSuite.java

To run as a Jazz CLM precondition, run the JUnit test from within Eclipse.
Refer link to setup "Require JUnit run, Precondition"<br/>
[Source control process recipes for Rational Team Concert](https://jazz.net/library/article/1075)

