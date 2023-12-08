package Server;
import Server.ResoucesManager.ClientTokensManager;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class Singletons {
    private static ClientTokensManager tokensManager = null;
    private static OperatingSystemMXBean systemMXBean = null;
    public static ClientTokensManager getTokensManager(){
        if(tokensManager == null){
            tokensManager = new ClientTokensManager();
        }

        return tokensManager;
    }

    public static OperatingSystemMXBean getSystemMXBean(){
        if(systemMXBean == null){
            systemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        }

        return systemMXBean;
    }
}
