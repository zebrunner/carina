package com.qaprosoft.carina.core.foundation.webdriver;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.Response;

public class ExtendedCommandExecutor implements CommandExecutor{
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
		Response response = commandExecutor.execute(command);
		return response;
	}

}
