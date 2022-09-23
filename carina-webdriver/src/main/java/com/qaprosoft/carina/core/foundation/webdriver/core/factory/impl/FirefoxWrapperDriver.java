package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import static org.openqa.selenium.remote.CapabilityType.PROXY;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import org.openqa.selenium.Beta;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PersistentCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.bidi.BiDi;
import org.openqa.selenium.bidi.BiDiException;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.devtools.CdpEndpointFinder;
import org.openqa.selenium.devtools.CdpInfo;
import org.openqa.selenium.devtools.CdpVersionFinder;
import org.openqa.selenium.devtools.Connection;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.noop.NoOpCdpInfo;
import org.openqa.selenium.firefox.AddHasContext;
import org.openqa.selenium.firefox.AddHasExtensions;
import org.openqa.selenium.firefox.AddHasFullPageScreenshot;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxCommandContext;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.HasContext;
import org.openqa.selenium.firefox.HasExtensions;
import org.openqa.selenium.firefox.HasFullPageScreenshot;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebDriverBuilder;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.HttpClient;

import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

/**
 * This class is a partial copy of FirefoxDriver for remote running
 */
class FirefoxWrapperDriver extends RemoteWebDriver
        implements WebStorage, HasExtensions, HasFullPageScreenshot, HasContext, HasDevTools, HasBiDi {

    private static final Logger LOG = Logger
            .getLogger(com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.FirefoxWrapperDriver.class.getName());
    private final Capabilities capabilities;
    private final RemoteWebStorage webStorage;
    private final HasExtensions extensions;
    private final HasFullPageScreenshot fullPageScreenshot;
    private final HasContext context;
    private final Optional<URI> cdpUri;
    private final Optional<URI> biDiUri;
    protected FirefoxBinary binary;
    private DevTools devTools;
    private BiDi biDi;

    public FirefoxWrapperDriver(HttpCommandExecutor executor, FirefoxOptions options) {
        super(executor, checkCapabilitiesAndProxy(options));
        webStorage = new RemoteWebStorage(getExecuteMethod());
        extensions = new AddHasExtensions().getImplementation(getCapabilities(), getExecuteMethod());
        fullPageScreenshot = new AddHasFullPageScreenshot().getImplementation(getCapabilities(), getExecuteMethod());
        context = new AddHasContext().getImplementation(getCapabilities(), getExecuteMethod());

        Capabilities capabilities = super.getCapabilities();
        HttpClient.Factory clientFactory = HttpClient.Factory.createDefault();
        Optional<URI> cdpUri = CdpEndpointFinder.getReportedUri("moz:debuggerAddress", capabilities)
                .flatMap(reported -> CdpEndpointFinder.getCdpEndPoint(clientFactory, reported));

        Optional<String> webSocketUrl = Optional.ofNullable((String) capabilities.getCapability("webSocketUrl"));

        this.biDiUri = webSocketUrl.map(uri -> {
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                LOG.warning(e.getMessage());
            }
            return null;
        });

        this.cdpUri = cdpUri;
        this.capabilities = cdpUri.map(uri -> new ImmutableCapabilities(
                new PersistentCapabilities(capabilities)
                        .setCapability("se:cdp", uri.toString())
                        .setCapability("se:cdpVersion", "85.0")))
                .orElse(new ImmutableCapabilities(capabilities));
    }

    @Beta
    public static RemoteWebDriverBuilder builder() {
        return RemoteWebDriver.builder().oneOf(new FirefoxOptions());
    }

    /**
     * Check capabilities and proxy if it is set
     */
    private static Capabilities checkCapabilitiesAndProxy(Capabilities capabilities) {
        if (capabilities == null) {
            return new ImmutableCapabilities();
        }

        MutableCapabilities caps = new MutableCapabilities(capabilities);

        // Ensure that the proxy is in a state fit to be sent to the extension
        Proxy proxy = Proxy.extractFrom(capabilities);
        if (proxy != null) {
            caps.setCapability(PROXY, proxy);
        }

        return caps;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public void setFileDetector(FileDetector detector) {
        throw new WebDriverException(
                "Setting the file detector only works on remote webdriver instances obtained " +
                        "via RemoteWebDriver");
    }

    @Override
    public LocalStorage getLocalStorage() {
        return webStorage.getLocalStorage();
    }

    @Override
    public SessionStorage getSessionStorage() {
        return webStorage.getSessionStorage();
    }

    @Override
    public String installExtension(Path path) {
        Require.nonNull("Path", path);
        return extensions.installExtension(path);
    }

    @Override
    public String installExtension(Path path, Boolean temporary) {
        Require.nonNull("Path", path);
        Require.nonNull("Temporary", temporary);
        return extensions.installExtension(path, temporary);
    }

    @Override
    public void uninstallExtension(String extensionId) {
        Require.nonNull("Extension ID", extensionId);
        extensions.uninstallExtension(extensionId);
    }

    /**
     * Capture the full page screenshot and store it in the specified location.
     *
     * @param <X> Return type for getFullPageScreenshotAs.
     * @param outputType target type, @see OutputType
     * @return Object in which is stored information about the screenshot.
     * @throws WebDriverException on failure.
     */
    @Override
    public <X> X getFullPageScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        Require.nonNull("OutputType", outputType);

        return fullPageScreenshot.getFullPageScreenshotAs(outputType);
    }

    @Override
    public FirefoxCommandContext getContext() {
        return context.getContext();
    }

    @Override
    public void setContext(FirefoxCommandContext commandContext) {
        Require.nonNull("Firefox Command Context", commandContext);
        context.setContext(commandContext);
    }

    @Override
    public Optional<DevTools> maybeGetDevTools() {
        if (devTools != null) {
            return Optional.of(devTools);
        }

        if (!cdpUri.isPresent()) {
            return Optional.empty();
        }

        URI wsUri = cdpUri.orElseThrow(() -> new DevToolsException("This version of Firefox or geckodriver does not support CDP"));
        HttpClient.Factory clientFactory = HttpClient.Factory.createDefault();

        ClientConfig wsConfig = ClientConfig.defaultConfig().baseUri(wsUri);
        HttpClient wsClient = clientFactory.createClient(wsConfig);

        Connection connection = new Connection(wsClient, wsUri.toString());
        CdpInfo cdpInfo = new CdpVersionFinder().match("85.0").orElseGet(NoOpCdpInfo::new);
        devTools = new DevTools(cdpInfo::getDomains, connection);

        return Optional.of(devTools);
    }

    @Override
    public DevTools getDevTools() {
        if (!cdpUri.isPresent()) {
            throw new DevToolsException("This version of Firefox or geckodriver does not support CDP");
        }

        return maybeGetDevTools()
                .orElseThrow(() -> new DevToolsException("Unable to initialize CDP connection"));
    }

    @Override
    public Optional<BiDi> maybeGetBiDi() {
        if (biDi != null) {
            return Optional.of(biDi);
        }

        if (!biDiUri.isPresent()) {
            return Optional.empty();
        }

        URI wsUri = biDiUri.orElseThrow(
                () -> new BiDiException("This version of Firefox or geckodriver does not support BiDi"));

        HttpClient.Factory clientFactory = HttpClient.Factory.createDefault();
        ClientConfig wsConfig = ClientConfig.defaultConfig().baseUri(wsUri);
        HttpClient wsClient = clientFactory.createClient(wsConfig);

        org.openqa.selenium.bidi.Connection connection = new org.openqa.selenium.bidi.Connection(wsClient, wsUri.toString());

        biDi = new BiDi(connection);

        return Optional.of(biDi);
    }

    @Override
    public BiDi getBiDi() {
        if (!biDiUri.isPresent()) {
            throw new BiDiException("This version of Firefox or geckodriver does not support Bidi");
        }

        return maybeGetBiDi()
                .orElseThrow(() -> new DevToolsException("Unable to initialize Bidi connection"));
    }

    public static final class SystemProperty {

        /**
         * System property that defines the location of the Firefox executable file.
         */
        public static final String BROWSER_BINARY = "webdriver.firefox.bin";

        /**
         * System property that defines the location of the file where Firefox log should be stored.
         */
        public static final String BROWSER_LOGFILE = "webdriver.firefox.logfile";

        /**
         * System property that defines the profile that should be used as a template.
         * When the driver starts, it will make a copy of the profile it is using,
         * rather than using that profile directly.
         */
        public static final String BROWSER_PROFILE = "webdriver.firefox.profile";
    }

    public static final class Capability {

        public static final String BINARY = "firefox_binary";
        public static final String PROFILE = "firefox_profile";
        public static final String MARIONETTE = "marionette";
    }

    private static class FirefoxDriverCommandExecutor extends EventFiringSeleniumCommandExecutor {

        public FirefoxDriverCommandExecutor(URL remoteURL) {
            super(remoteURL);
        }
    }
}
