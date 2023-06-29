#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.lang.invoke.MethodHandles;
import java.net.URI;

@ClientEndpoint
public final class WebsocketClientEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Session userSession = null;
    private MessageHandler messageHandler;

    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events
     *
     * @param userSession the userSession which is opened
     */
    @OnOpen
    public void onOpen(Session userSession) {
        LOGGER.info("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events
     *
     * @param userSession the userSession which is getting closed.
     * @param reason      the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOGGER.info("Closing WebSocket. Code:'{}', Reason: '{}'", reason.getCloseCode(), reason.getReasonPhrase());
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(Object bytes) {
        LOGGER.info("Message: {}", bytes);
        this.messageHandler.handleMessage(bytes);
    }

    /**
     * Register message handler
     *
     * @param msgHandler see {@link MessageHandler}
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message
     *
     * @param message message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public interface MessageHandler {

        void handleMessage(Object message);
    }
}