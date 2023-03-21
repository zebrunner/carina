#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.proxy;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.proxy.IProxy;
import com.zebrunner.carina.proxy.IProxyRule;
import com.zebrunner.carina.utils.Configuration;

public class CustomProxyRule implements IProxyRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Optional<IProxy> getProxyInstance() {
        IProxy proxy = null;
        if (Configuration.getBoolean(Configuration.Parameter.BROWSERUP_PROXY)) {
            proxy = new CustomProxy();
        } else {
            LOGGER.debug("Proxy is disabled.");
        }
        return Optional.ofNullable(proxy);
    }

}
