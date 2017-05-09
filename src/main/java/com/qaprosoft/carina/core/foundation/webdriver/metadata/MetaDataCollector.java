package com.qaprosoft.carina.core.foundation.webdriver.metadata;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.qaprosoft.carina.core.foundation.utils.JsonUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by yauhenipatotski on 4/12/17.
 */
public class MetaDataCollector {

    private static final String ATTRIBUTE_JS = "var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;";

    private static ElementsInfo elementsInfo = new ElementsInfo();


    public static ElementsInfo getElementsInfo() {
        return elementsInfo;
    }

    public static void collectDataFromElement(CommandExecutor commandExecutor, Command command) {
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.setRectangle(getRectangle(commandExecutor, command));
        elementInfo.setElementsAttributes(getAttributes(commandExecutor, command));
        elementInfo.setScreenshot(getScreenshot(commandExecutor, command));
        elementsInfo.addElement(elementInfo);

    }


    public static String generateJSON() {
        return JsonUtils.toJson(elementsInfo);
    }

    private static Rectangle getRectangle(CommandExecutor commandExecutor, Command command) {
        Rectangle rectangle = new Rectangle();
        rectangle.setX(40);
        rectangle.setY(30);
        rectangle.setHeight(100);
        rectangle.setWidth(50);
        return rectangle;
    }


    private static String getScreenshot(CommandExecutor commandExecutor, Command command) {
        Command screenshotCommand = new Command(command.getSessionId(), DriverCommand.SCREENSHOT);


        Response response = executeCommand(commandExecutor, screenshotCommand);

        response.getValue();
        File file = OutputType.FILE.convertFromBase64Png((String) response.getValue());

        return file.getAbsolutePath();
    }


    private static Map<String, String> getAttributes(CommandExecutor commandExecutor, Command command) {
        ImmutableMap args = ImmutableMap.of("ELEMENT", command.getParameters().get("id"),
                "element-6066-11e4-a52e-4f735466cecf", command.getParameters().get("id"));


        Map<String, ?> params = ImmutableMap.of(
                "script", ATTRIBUTE_JS,
                "args", Lists.newArrayList(args));

        Command attributeCommand = new Command(command.getSessionId(), DriverCommand.EXECUTE_SCRIPT, params);

        return (Map<String, String>) executeCommand(commandExecutor, attributeCommand).getValue();
    }


    private static Response executeCommand(CommandExecutor commandExecutor, Command command) {
        Response response = null;
        try {
            response = commandExecutor.execute(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
