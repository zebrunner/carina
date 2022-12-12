package com.qaprosoft.carina.core.foundation.webdriver.devtools;

import static com.github.kklisura.cdt.services.utils.ConfigurationUtils.systemProperty;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslContextConfigurator;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.client.auth.Credentials;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientContainer;

import com.github.kklisura.cdt.services.factory.WebSocketContainerFactory;
import com.zebrunner.carina.utils.Configuration;

public class ZebrunnerWebSocketContainerFactory implements WebSocketContainerFactory {

    public static final String WEBSOCKET_INCOMING_BUFFER_PROPERTY = "com.github.kklisura.cdt.services.config.incomingBuffer";

    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

    private static final int DEFAULT_INCOMING_BUFFER_SIZE = 8 * MB;

    private static final long INCOMING_BUFFER_SIZE = systemProperty(WEBSOCKET_INCOMING_BUFFER_PROPERTY, DEFAULT_INCOMING_BUFFER_SIZE);

    public static final String INCOMING_BUFFER_SIZE_PROPERTY = "org.glassfish.tyrus.incomingBufferSize";

    @Override
    public WebSocketContainer getWebSocketContainer() {
        final ClientManager client = ClientManager.createClient(GrizzlyClientContainer.class.getName());
        client.getProperties().put(INCOMING_BUFFER_SIZE_PROPERTY, INCOMING_BUFFER_SIZE);
        client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);

        // not for production - see https://eclipse-ee4j.github.io/tyrus-project.github.io/documentation/latest/index/tyrus-proprietary-config.html
        // 8.1.1. Host verification
        // disable host verification
        SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(new SslContextConfigurator());
        sslEngineConfigurator.setHostVerificationEnabled(false);
        client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);

        String seleniumHost = Configuration.getSeleniumUrl();
        if (seleniumHost.isEmpty()) {
            seleniumHost = Configuration.getEnvArg(Configuration.Parameter.URL.getKey());
        }
        try {
            client.getProperties().put(ClientProperties.CREDENTIALS,
                    new Credentials(getField(seleniumHost, 1),
                            getField(seleniumHost, 2).getBytes("iso-8859-1")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    private String getField(String url, int position) {
        Pattern pattern = Pattern.compile(".*:\\/\\/(.*):(.*)@");
        Matcher matcher = pattern.matcher(url);

        return matcher.find() ? matcher.group(position) : "";
    }
}