#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.base;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@TestExecutionListeners({ServletTestExecutionListener.class, DirtiesContextBeforeModesTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public class SOAPTest extends AbstractTest implements IHookable, ApplicationContextAware {

    /**
     * The {@link ApplicationContext} that was injected into this test instance
     * via {@link ${symbol_pound}setApplicationContext(ApplicationContext)}.
     */
    @Nullable
    protected ApplicationContext applicationContext;

    private final TestContextManager testContextManager;

    @Nullable
    private Throwable testException;

    /**
     * Construct a new SoapTest instance and initialize
     * the internal {@link TestContextManager} for the current test class.
     */
    public SOAPTest() {
        this.testContextManager = new TestContextManager(getClass());
    }

    /**
     * Set the {@link ApplicationContext} to be used by this test instance,
     * provided via {@link ApplicationContextAware} semantics.
     *
     * @param applicationContext the ApplicationContext that this test runs in
     */
    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Delegates to the configured {@link TestContextManager} to call
     * {@linkplain TestContextManager${symbol_pound}beforeTestClass() 'before test class'} callbacks.
     *
     * @throws Exception if a registered TestExecutionListener throws an exception
     */
    @BeforeClass(alwaysRun = true)
    protected void springTestContextBeforeTestClass() throws Exception {
        this.testContextManager.beforeTestClass();
    }

    /**
     * Delegates to the configured {@link TestContextManager} to
     * {@linkplain TestContextManager${symbol_pound}prepareTestInstance(Object) prepare} this test
     * instance prior to execution of any individual tests, for example for
     * injecting dependencies, etc.
     *
     * @throws Exception if a registered TestExecutionListener throws an exception
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextBeforeTestClass")
    protected void springTestContextPrepareTestInstance() throws Exception {
        this.testContextManager.prepareTestInstance(this);
    }

    /**
     * Delegates to the configured {@link TestContextManager} to
     * {@linkplain TestContextManager${symbol_pound}beforeTestMethod(Object, Method) pre-process}
     * the test method before the actual test is executed.
     *
     * @param testMethod the test method which is about to be executed
     * @throws Exception allows all exceptions to propagate
     */
    @BeforeMethod(alwaysRun = true)
    protected void springTestContextBeforeTestMethod(Method testMethod) throws Exception {
        this.testContextManager.beforeTestMethod(this, testMethod);
    }

    /**
     * Delegates to the {@linkplain IHookCallBack${symbol_pound}runTestMethod(ITestResult) test
     * method} in the supplied {@code callback} to execute the actual test
     * and then tracks the exception thrown during test execution, if any.
     *
     * @see org.testng.IHookable${symbol_pound}run(IHookCallBack, ITestResult)
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        boolean beforeCallbacksExecuted = false;

        try {
            this.testContextManager.beforeTestExecution(this, testMethod);
            beforeCallbacksExecuted = true;
        } catch (Throwable ex) {
            this.testException = ex;
        }

        if (beforeCallbacksExecuted) {
            callBack.runTestMethod(testResult);
            this.testException = getTestResultException(testResult);
        }

        try {
            this.testContextManager.afterTestExecution(this, testMethod, this.testException);
        } catch (Throwable ex) {
            if (this.testException == null) {
                this.testException = ex;
            }
        }

        if (this.testException != null) {
            throwAsUncheckedException(this.testException);
        }
    }

    /**
     * Delegates to the configured {@link TestContextManager} to call
     * {@linkplain TestContextManager${symbol_pound}afterTestClass() 'after test class'} callbacks.
     *
     * @throws Exception if a registered TestExecutionListener throws an exception
     */
    @AfterClass(alwaysRun = true)
    protected void springTestContextAfterTestClass() throws Exception {
        this.testContextManager.afterTestClass();
    }


    private Throwable getTestResultException(ITestResult testResult) {
        Throwable testResultException = testResult.getThrowable();
        if (testResultException instanceof InvocationTargetException) {
            testResultException = ((InvocationTargetException) testResultException).getCause();
        }
        return testResultException;
    }

    private RuntimeException throwAsUncheckedException(Throwable t) {
        throwAs(t);
        // Appeasing the compiler: the following line will never be executed.
        throw new IllegalStateException(t);
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> void throwAs(Throwable t) throws T {
        throw (T) t;
    }

}
