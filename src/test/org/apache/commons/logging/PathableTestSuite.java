/*
 * Created on 24/06/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.apache.commons.logging;

import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Vector;

/**
 * Custom TestSuite class that can be used to control the context classloader
 * in operation when a test runs.
 * <p>
 * For tests that need to control exactly what the classloader hierarchy is
 * like when the test is run, something like the following is recommended:
 * <pre>
 * class SomeTestCase extends TestCase {
 *  public static Test suite() throws Exception {
 *   PathableClassLoader parent = new PathableClassLoader(null);
 *   parent.useSystemLoader("junit.");
 * 
 *   PathableClassLoader child = new PathableClassLoader(parent);
 *   child.addLogicalLib("testclasses");
 *   child.addLogicalLib("log4j12");
 *   child.addLogicalLib("commons-logging");
 * 
 *   Class testClass = child.loadClass(SomeTestCase.class.getName());
 *   ClassLoader contextClassLoader = child;
 * 
 *   PathableTestSuite suite = new PathableTestSuite(testClass, child);
 *   return suite;
 *  }
 * 
 *  // test methods go here
 * }
 * </pre>
 * Note that if the suite method throws an exception then this will be handled
 * reasonable gracefully by junit; it will report that the suite method for 
 * a test case failed with exception yyy.
 * <p>
 * The use of PathableClassLoader is not required to use this class, but it
 * is expected that using the two classes together is common practice.
 * <p>
 * This class will run each test methods within the specified TestCase using
 * the specified context classloader and system classloader. If different
 * tests within the same class require different context classloaders,
 * then the context classloader passed to the constructor should be the 
 * "lowest" one available, and tests that need the context set to some parent
 * of this "lowest" classloader can call
 * <pre>
 *  // NB: pseudo-code only
 *  setContextClassLoader(getContextClassLoader().getParent());
 * </pre>
 * This class ensures that any context classloader changes applied by a test
 * is undone after the test is run, so tests don't need to worry about
 * restoring the context classloader on exit.
 * <p>
 * This class does not provide facilities for manipulating system properties;
 * tests that need specific system properties can simply set them in the
 * fixture or at the start of a test method.
 * <p>
 * This class cannot control the system classloader (ie what method 
 * ClassLoader.getSystemClassLoader returns) because Java provides no
 * mechanism for setting the system classloader. In this case, the only
 * option is to invoke the unit test in a separate JVM with the appropriate
 * settings.
 * <p>
 * <b>Important!</b> When the test case is run, "this.getClass()" refers of
 * course to the Class object passed to the constructor of this class - which 
 * is different from the class whose suite() method was executed to determine
 * the classpath. This means that the suite method cannot communicate with
 * the test cases simply by setting static variables (for example to make the
 * custom classloaders available to the test methods or setUp/tearDown fixtures).
 * If this is really necessary then it is possible to use reflection to invoke
 * static methods on the class object passed to the constructor of this class 
 */
public class PathableTestSuite extends TestSuite {

    /**
     * The classloader that should be set as the context classloader
     * before each test in the suite is run.
     */
    private ClassLoader contextLoader;

    /**
     * Constructor.
     * 
     * @param testClass is the TestCase that is to be run, as loaded by
     * the appropriate ClassLoader.
     * 
     * @param contextClassLoader is the loader that should be returned by
     * calls to Thread.currentThread.getContextClassLoader from test methods
     * (or any method called by test methods).
     */
    public PathableTestSuite(Class testClass, ClassLoader contextClassLoader) {
        super(testClass);
        contextLoader = contextClassLoader;
    }

    /**
     * This method is invoked once for each Test in the current TestSuite.
     * Note that a Test may itself be a TestSuite object (ie a collection
     * of tests).
     */
    public void runTest(Test test, TestResult result) {
        ClassLoader origContext = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextLoader);
            test.run(result);
        } finally {
            Thread.currentThread().setContextClassLoader(origContext);
        }
    }
}
