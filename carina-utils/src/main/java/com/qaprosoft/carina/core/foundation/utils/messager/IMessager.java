package com.qaprosoft.carina.core.foundation.utils.messager;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReportMessage is used for reporting informational and error messages both
 * using needed loggers.
 *
 * @author brutskov
 */

public interface IMessager {

    Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    String getPattern();

    Logger getLogger();

    default String getMessage(String... args) {
        return create(args);
    }

    /**
     * Logs info message using message pattern and incoming parameters.
     *
     * @param args
     *            for insert into patterns
     * @return generated message
     */
    default String info(String... args) {
        String message = create(args);
        getLogger().info(message);
        return message;
    }

    /**
     * Logs error message and adds message to TestNG report.
     *
     * @param args
     *            for insert into patterns
     * @return generated message
     */
    default String error(String... args) {
        String message = create(args);
        Reporter.log(message);
        getLogger().error(message);
        return message;
    }

    /**
     * Generates error message using message pattern and incoming parameters.
     *
     * @param args
     *            for insert into pattern
     * @return generated message
     */
    default String create(String... args) {
        String message = "";
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    Matcher matcher = CRYPTO_PATTERN.matcher(args[i]);
                    if (matcher.find()) {
                        int start = args[i].indexOf(':') + 1;
                        int end = args[i].indexOf('}');
                        args[i] = StringUtils.replace(args[i], matcher.group(), StringUtils.repeat('*', end - start));
                    }
                }
            }
            message = String.format(getPattern(), (Object[]) args);
        } catch (Exception e) {
            getLogger().error("Report message creation error!", e);
        }
        return message;
    }
}
