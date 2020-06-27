package com.dwiveddi.restapi;


import com.dwiveddi.restapi.dto.RequestResponseCombination;
import org.testng.*;

public class RestApiListener implements ITestListener,ISuiteListener,IInvokedMethodListener {


    @Override
    public void onStart(ITestContext arg0) {

        Reporter.log("About to begin executing Test " + arg0.getName(), true);

    }

    // This belongs to ITestListener and will execute, once the Test set/batch is finished
    @Override
    public void onFinish(ITestContext arg0) {

        Reporter.log("Completed executing test " + arg0.getName(), true);

    }

    // This belongs to ITestListener and will execute only when the test is pass
    @Override
    public void onTestSuccess(ITestResult arg0) {

        // This is calling the printTestResults method
        //String issueKey = (String)arg0.getParameters()[0];
        System.out.println("==========PASS===================");
        String issueKey = ((RequestResponseCombination)arg0.getParameters()[0]).getId();
        ZephyrHelper.updateTestCaseStatus(issueKey, ZephyrHelper.Status.PASS);
        printTestResults(arg0);

    }

    // This belongs to ITestListener and will execute only on the event of fail test
    @Override
    public void onTestFailure(ITestResult arg0) {

        // This is calling the printTestResults method
        //String issueKey = (String)arg0.getParameters()[0];
        System.out.println("==========FAIL===================");
        String issueKey = ((RequestResponseCombination)arg0.getParameters()[0]).getId();
        ZephyrHelper.updateTestCaseStatus(issueKey, ZephyrHelper.Status.FAIL);
        printTestResults(arg0);

    }

    // This belongs to ITestListener and will execute before the main test start (@Test)
    @Override
    public void onTestStart(ITestResult arg0) {

        System.out.println("The execution of the main test starts now");

    }

    // This belongs to ITestListener and will execute only if any of the main test(@Test) get skipped
    @Override
    public void onTestSkipped(ITestResult arg0) {
        String issueKey = ((RequestResponseCombination)arg0.getParameters()[0]).getId();
        ZephyrHelper.updateTestCaseStatus(issueKey, ZephyrHelper.Status.UNEXECUTED);
        printTestResults(arg0);

    }

    // This is just a piece of shit, ignore this
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {

    }

    // This is the method which will be executed in case of test pass or fail

    // This will provide the information on the test
    private void printTestResults(ITestResult result) {

        Reporter.log("Test Method resides in " + result.getTestClass().getName(), true);

        if (result.getParameters().length != 0) {

            String params = null;

            for (Object parameter : result.getParameters()) {

                params += parameter.toString() + ",";

            }

            Reporter.log("Test Method had the following parameters : " + params, true);

        }

        String status = null;

        switch (result.getStatus()) {

            case ITestResult.SUCCESS:

                status = "Pass";

                break;

            case ITestResult.FAILURE:

                status = "Failed";

                break;

            case ITestResult.SKIP:

                status = "Skipped";

        }

        Reporter.log("Test Status: " + status, true);

    }

    // This belongs to IInvokedMethodListener and will execute before every method including @Before @After @Test
    @Override
    public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {

        String textMsg = "About to begin executing following method : " + returnMethodName(arg0.getTestMethod());

        Reporter.log(textMsg, true);

    }

    // This belongs to IInvokedMethodListener and will execute after every method including @Before @After @Test
    @Override
    public void afterInvocation(IInvokedMethod arg0, ITestResult arg1) {

        String textMsg = "Completed executing following method : " + returnMethodName(arg0.getTestMethod());

        Reporter.log(textMsg, true);

    }

    // This will return method names to the calling function

    private String returnMethodName(ITestNGMethod method) {

        return method.getRealClass().getSimpleName() + "." + method.getMethodName();

    }

    @Override
    public void onStart(ISuite iSuite) {
     ZephyrHelper.init(iSuite.getParameter("zephyrEnv"),iSuite.getParameter("versionName"),iSuite.getParameter("cycleName"));
    }


    @Override
    public void onFinish(ISuite iSuite) {

    }
}
