package net.es.oscars;

import org.testng.*;

/**
 * Real time test reporter.  Adapted from code at
 * http://www.philvarner.com/blog/2008/02/10/testng-part-2/
 * Not covered by license.
 */
public class ITestListener extends TestListenerAdapter {
    private int m_count = 0;

    private String name(ITestResult tr){
        return tr.getTestClass().getName() +
            "." + tr.getMethod().getMethodName();
    }

  @Override
    public void onTestFailure(ITestResult tr) {
        log("[FAILED " + (m_count++) + "] => " + name(tr) );
    }

  @Override
    public void onTestSkipped(ITestResult tr) {
        log("[SKIPPED " + (m_count++) + "] => " + name(tr) );
    }

  @Override
    public void onTestSuccess(ITestResult tr) {
        log("[" + (m_count++) + "] => " + name(tr) + " " +
            (tr.getEndMillis() - tr.getStartMillis()) + " ms");
    }

    private void log(String string) {
        System.out.println(string);
    }
}
