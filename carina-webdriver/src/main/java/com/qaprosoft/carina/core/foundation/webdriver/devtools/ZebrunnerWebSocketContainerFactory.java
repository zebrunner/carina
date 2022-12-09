package com.qaprosoft.carina.core.foundation.webdriver.devtools;

import static com.github.kklisura.cdt.services.utils.ConfigurationUtils.systemProperty;

import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientContainer;

import com.github.kklisura.cdt.services.factory.WebSocketContainerFactory;

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
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);
        return client;
    }
}