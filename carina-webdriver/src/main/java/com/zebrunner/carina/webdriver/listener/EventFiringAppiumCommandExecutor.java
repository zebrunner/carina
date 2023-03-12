/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.webdriver.listener;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;
import static org.openqa.selenium.remote.DriverCommand.NEW_SESSION;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandCodec;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.ProtocolHandshake;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.ResponseCodec;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.service.DriverService;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.zebrunner.carina.utils.R;

import io.appium.java_client.MobileCommand;
import io.appium.java_client.remote.AppiumCommandExecutor;
import io.appium.java_client.remote.AppiumProtocolHandshake;
import io.appium.java_client.remote.AppiumW3CHttpCommandCodec;

/**
 * EventFiringAppiumCommandExecutor triggers event listener before/after execution of the command.
 * Please track {@link AppiumCommandExecutor} for latest changes.
 *
 * @author akhursevich
 */
@SuppressWarnings({ "unchecked" })
public class EventFiringAppiumCommandExecutor extends HttpCommandExecutor {
    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private final Optional<DriverService> serviceOptional;

    private EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands, DriverService service,
            URL addressOfRemoteServer,
            HttpClient.Factory httpClientFactory) {
        super(additionalCommands,
                ClientConfig.defaultConfig()
                        .baseUrl(Require.nonNull("Server URL", ofNullable(service)
                                .map(DriverService::getUrl)
                                .orElse(addressOfRemoteServer)))
                        //todo reuse parameter from Configuration.Parameter class
                        .readTimeout(Duration.ofSeconds(R.CONFIG.getLong("read_timeout"))),
                httpClientFactory);
        serviceOptional = ofNullable(service);
    }

    public EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands, DriverService service,
            HttpClient.Factory httpClientFactory) {
        this(additionalCommands, checkNotNull(service), null, httpClientFactory);
    }

    public EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands,
            URL addressOfRemoteServer, HttpClient.Factory httpClientFactory) {
        this(additionalCommands, null, checkNotNull(addressOfRemoteServer), httpClientFactory);
    }

    public EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands,
            URL addressOfRemoteServer) {
        this(additionalCommands, addressOfRemoteServer, HttpClient.Factory.createDefault());
    }

    public EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands,
            DriverService service) {
        this(additionalCommands, service, HttpClient.Factory.createDefault());
    }

    public EventFiringAppiumCommandExecutor(URL addressOfRemoteServer) {
    	this(MobileCommand.commandRepository, addressOfRemoteServer, HttpClient.Factory.createDefault());
    }

    @SuppressWarnings("SameParameterValue")
    private <B> B getPrivateFieldValue(
            Class<? extends CommandExecutor> cls, String fieldName, Class<B> fieldType) {
        try {
            final Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return fieldType.cast(f.get(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WebDriverException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void setPrivateFieldValue(
            Class<? extends CommandExecutor> cls, String fieldName, Object newValue) {
        try {
            final Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(this, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WebDriverException(e);
        }
    }


    private Map<String, CommandInfo> getAdditionalCommands() {
        // noinspection unchecked
        return getPrivateFieldValue(HttpCommandExecutor.class, "additionalCommands", Map.class);
    }

    private CommandCodec<HttpRequest> getCommandCodec() {
        // noinspection unchecked
        return getPrivateFieldValue(HttpCommandExecutor.class, "commandCodec", CommandCodec.class);
    }

    private void setCommandCodec(CommandCodec<HttpRequest> newCodec) {
        setPrivateFieldValue(HttpCommandExecutor.class, "commandCodec", newCodec);

    }

    private void setResponseCodec(ResponseCodec<HttpResponse> codec) {
        setPrivateFieldValue(HttpCommandExecutor.class, "responseCodec", codec);
    }

    private HttpClient getClient() {
        return getPrivateFieldValue(HttpCommandExecutor.class, "client", HttpClient.class);
    }

    private Response createSession(Command command) throws IOException {
        if (getCommandCodec() != null) {
            throw new SessionNotCreatedException("Session already exists");
        }

        ProtocolHandshake.Result result = new AppiumProtocolHandshake().createSession(
                getClient().with((httpHandler) -> (req) -> {
                    req.setHeader(IDEMPOTENCY_KEY_HEADER, UUID.randomUUID().toString().toLowerCase());
                    return httpHandler.execute(req);
                }), command);
        Dialect dialect = result.getDialect();
        if (!(dialect.getCommandCodec() instanceof W3CHttpCommandCodec)) {
            throw new SessionNotCreatedException("Only W3C sessions are supported. "
                    + "Please make sure your server is up to date.");
        }
        setCommandCodec(new AppiumW3CHttpCommandCodec());
        refreshAdditionalCommands();
        setResponseCodec(dialect.getResponseCodec());
        return result.createResponse();
    }

    private void refreshAdditionalCommands() {
        getAdditionalCommands().forEach(this::defineCommand);
    }

    @Override
    public Response execute(Command command) throws WebDriverException {
        if (DriverCommand.NEW_SESSION.equals(command.getName())) {
            serviceOptional.ifPresent(driverService -> {
                try {
                    driverService.start();
                } catch (IOException e) {
                    throw new WebDriverException(e.getMessage(), e);
                }
            });
        }

        try {
            return NEW_SESSION.equals(command.getName()) ? createSession(command) : super.execute(command);
        } catch (Throwable t) {
            Throwable rootCause = Throwables.getRootCause(t);
            if (rootCause instanceof ConnectException
                    && rootCause.getMessage().contains("Connection refused")) {
                throw serviceOptional.map(service -> {
                    if (service.isRunning()) {
                        return new WebDriverException("The session is closed!", rootCause);
                    }

                    return new WebDriverException("The appium server has accidentally died!", rootCause);
                }).orElseGet((Supplier<WebDriverException>) () -> new WebDriverException(rootCause.getMessage(), rootCause));
            }
            // [VD] never enable throwIfUnchecked as it generates RuntimeException and corrupt TestNG main thread!
            // throwIfUnchecked(t);
            throw new WebDriverException(t);
        } finally {
            if (DriverCommand.QUIT.equals(command.getName())) {
                serviceOptional.ifPresent(DriverService::stop);
            }
        }
    }
}

