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
package com.qaprosoft.carina.core.foundation.webdriver.listener;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandCodec;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
//import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;
import org.openqa.selenium.remote.service.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
//import com.qaprosoft.carina.core.foundation.webdriver.httpclient.HttpClientFactoryCustom;

import io.appium.java_client.MobileCommand;
import io.appium.java_client.remote.AppiumCommandExecutor;
import io.appium.java_client.remote.AppiumW3CHttpCommandCodec;

//import io.appium.java_client.remote.NewAppiumSessionPayload;


/**
 * EventFiringAppiumCommandExecutor triggers event listener before/after execution of the command.
 * Please track {@link AppiumCommandExecutor} for latest changes.
 *
 * @author akhursevich
 */
@SuppressWarnings({ "unchecked" })
public class EventFiringAppiumCommandExecutor extends HttpCommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Optional<DriverService> serviceOptional;

    private EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands, DriverService service,
            URL addressOfRemoteServer,
            HttpClient.Factory httpClientFactory) {
        super(additionalCommands,
                ofNullable(service)
                        .map(DriverService::getUrl)
                        .orElse(addressOfRemoteServer),
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

    //TODO import latest java-appium client 
/*    public EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands,
            URL addressOfRemoteServer) {
        this(additionalCommands, addressOfRemoteServer, new HttpClientFactoryCustom());
    }*/

/*    public EventFiringAppiumCommandExecutor(Map<String, CommandInfo> additionalCommands,
            DriverService service) {
        this(additionalCommands, service, new HttpClientFactoryCustom());
    }*/

    public EventFiringAppiumCommandExecutor(URL addressOfRemoteServer) {
    	this(MobileCommand.commandRepository, addressOfRemoteServer, HttpClient.Factory.createDefault());
        //this(MobileCommand.commandRepository, addressOfRemoteServer, new HttpClientFactoryCustom());
    }

    private <B> B getPrivateFieldValue(String fieldName, Class<B> fieldType) {
        try {
            final Field f = getClass().getSuperclass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return fieldType.cast(f.get(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WebDriverException(e);
        }
    }

    private void setPrivateFieldValue(String fieldName, Object newValue) {
        try {
            final Field f = getClass().getSuperclass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(this, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WebDriverException(e);
        }
    }

    private Map<String, CommandInfo> getAdditionalCommands() {
        // noinspection unchecked
        return getPrivateFieldValue("additionalCommands", Map.class);
    }

    private CommandCodec<HttpRequest> getCommandCodec() {
        // noinspection unchecked
        return getPrivateFieldValue("commandCodec", CommandCodec.class);
    }

    private void setCommandCodec(CommandCodec<HttpRequest> newCodec) {
        setPrivateFieldValue("commandCodec", newCodec);
    }

    @Override
    public Response execute(Command command) throws WebDriverException {
        if (DriverCommand.NEW_SESSION.equals(command.getName())) {
            serviceOptional.ifPresent(driverService -> {
                try {
                    driverService.start();
                } catch (IOException e) {
                    throw new WebDriverException(e);
                }
            });
        }

        Response response = null;
        try {
            int retry = 10; //max attempts to repeit
            Number pause = Configuration.getInt(Parameter.EXPLICIT_TIMEOUT) / retry;
            while (retry > 0) {
                response = super.execute(command);
                if (response.getValue() instanceof WebDriverException) {
                    LOGGER.debug("CarinaCommandExecutor catched: " + response.getValue().toString());
                    String msg = response.getValue().toString();
                    if (msg.contains(SpecialKeywords.DRIVER_CONNECTION_REFUSED)
                            || msg.contains(SpecialKeywords.DRIVER_CONNECTION_REFUSED2)) {
                        LOGGER.warn("Enabled command executor retries: " + msg);
                        CommonUtils.pause(pause);
                    }
                } else {
                    // do nothing as response already contains all the information we need
                    break;
                }
                retry--;
            }
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

        //TODO [MS] this issue solved with appium java-client 8.0 +
/*        if (DriverCommand.NEW_SESSION.equals(command.getName())
                && getCommandCodec() instanceof W3CHttpCommandCodec) {
            setCommandCodec(new AppiumW3CHttpCommandCodec());
            getAdditionalCommands().forEach(this::defineCommand);
        }*/


        return response;
    }

}
