package Server;
import Server.ResoucesManager.ClientTokensManager;
import Server.ResoucesManager.MachineResoucesManager;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class Singletons {
    private static ClientTokensManager tokensManager = null;
    private static MachineResoucesManager resoucesManager = null;
    private static OperatingSystemMXBean systemMXBean = null;
    public static synchronized ClientTokensManager getTokensManager(){
        if(tokensManager == null){
            tokensManager = new ClientTokensManager();
        }

        return tokensManager;
    }

    public static synchronized MachineResoucesManager getResoucesManager(){
        if(resoucesManager == null){
            resoucesManager = new MachineResoucesManager();
            resoucesManager.setTokenManager(getTokensManager());
        }

        return resoucesManager;
    }

    public static synchronized OperatingSystemMXBean getSystemMXBean(){
        if(systemMXBean == null){
            systemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        }

        return systemMXBean;
    }
}
