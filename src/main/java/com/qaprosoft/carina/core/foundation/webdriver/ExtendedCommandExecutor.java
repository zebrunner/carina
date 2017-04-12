package com.qaprosoft.carina.core.foundation.webdriver;

import com.qaprosoft.carina.core.foundation.webdriver.metadata.MetaDataCollector;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;

import java.io.IOException;

public class ExtendedCommandExecutor implements CommandExecutor {
    protected static final Logger LOGGER = Logger.getLogger(ExtendedCommandExecutor.class);

    private CommandExecutor commandExecutor;

    protected CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    protected void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public ExtendedCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public Response execute(Command command) throws IOException {
        if (command.getName().equals(DriverCommand.CLICK_ELEMENT)|| command.getName().equals(DriverCommand.EXECUTE_SCRIPT)) {
            //Here we are interested only in the send keys event (viz., type action)
            MetaDataCollector.collectDataFromElement(commandExecutor, command);
        }
        Response response = commandExecutor.execute(command);
        return response;
    }

}
