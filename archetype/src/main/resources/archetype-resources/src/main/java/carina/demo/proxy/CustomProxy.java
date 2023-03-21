#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.proxy;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.zebrunner.carina.proxy.IProxy;
import com.zebrunner.carina.proxy.IProxyInfo;
import com.zebrunner.carina.proxy.ProxyInfo;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.NetworkUtil;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.common.CommonUtils;

/**
 * Custom proxy implementation based on BrowserUp proxy
 */
public class CustomProxy implements IProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected final BrowserUpProxy proxy;
    protected IProxyInfo proxyInfo = null;

    public CustomProxy() {
        proxy = new BrowserUpProxyServer();
        proxy.setTrustAllServers(true);
        proxy.setChainedProxy(InetSocketAddress.createUnresolved(R.CONFIG.get("proxy_chain_host"), R.CONFIG.getInt("proxy_chain_port")));
        // disable MITM in case we do not need it
        proxy.setMitmDisabled(Configuration.getBoolean(Configuration.Parameter.BROWSERUP_MITM));
    }

    @Override
    public IProxyInfo start(int port) {
        if (proxy.isStarted()) {
            throw new IllegalStateException("Proxy already started.");
        }
        LOGGER.info("Starting BrowserUp proxy...");
        proxy.start(port);

        CommonUtils.pause(1);
        proxyInfo = new ProxyInfo(NetworkUtil.getIpAddress(), proxy.getPort());
        return proxyInfo;
    }

    @Override
    public void stop() {
        if (!proxy.isStarted()) {
            throw new IllegalStateException("Proxy was not started.");
        }
        // isStarted returns true even if proxy was already stopped
        try {
            LOGGER.debug("stopProxy starting...");
            proxy.stop();
        } catch (IllegalStateException e) {
            LOGGER.info("Seems like proxy was already stopped.");
            LOGGER.info(e.getMessage());
        } finally {
            proxyInfo = null;
            LOGGER.debug("stopProxy finished...");
        }
    }

    @Override
    public IProxyInfo getInfo() {
        if (!isStarted()) {
            throw new IllegalStateException("Proxy was not started.");
        }
        return proxyInfo;
    }

    @Override
    public boolean isStarted() {
        return proxy.isStarted();
    }

    /**
     * Get object of BrowserUp proxy
     *
     * @return {@link BrowserUpProxy}
     */
    public BrowserUpProxy getProxy() {
        return proxy;
    }

    @Override
    public String toString() {
        return "CustomProxy";
    }

}
