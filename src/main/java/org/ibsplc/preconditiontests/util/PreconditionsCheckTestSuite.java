package org.ibsplc.preconditiontests.util;

import org.ibsplc.preconditiontests.CheckstyleTest;
import org.ibsplc.preconditiontests.FindbugsTest;
import org.ibsplc.preconditiontests.PMDTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	PMDTest.class,
	CheckstyleTest.class,
	FindbugsTest.class
})
public class PreconditionsCheckTestSuite {
	
}
