package Server.ResoucesManager;

import Server.HandlerClient;
import Server.Singletons;
import com.sun.management.OperatingSystemMXBean;

public class MachineResoucesManager extends Thread{

    private static final long SIZE_60_60_MEM = 7049672752L;
    private static final long SIZE_30_30_MEM = 1379554848L;

    private static final double SIZE_60_60_CPU = 0.50f;
    private static final double SIZE_30_30_CPU = 0.25f;

    private OperatingSystemMXBean systemMXBean = Singletons.getSystemMXBean();
    private ClientTokensManager tokenManager;
    private HandlerClient buffer = null;
    @Override
    public void run() {
        while (true){
            if(buffer == null){
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }else{
                /**
                 * Verifica se há recursos para o processamento
                 */
                double necessaryCPU = 0;
                long necessaryMEM = 0;

                switch (buffer.getImageSize()){
                    case "60x60":
                        necessaryCPU = SIZE_60_60_CPU;
                        necessaryMEM = SIZE_60_60_MEM;
                        break;
                    case "30x30":
                        necessaryCPU = SIZE_30_30_CPU;
                        necessaryMEM = SIZE_30_30_MEM;
                        break;
                }

                if(
                        (1f - systemMXBean.getCpuLoad())*0.75 > necessaryCPU &&
                         Runtime.getRuntime().freeMemory()*0.75 > necessaryMEM
                ) {
                    buffer.start();
                    buffer = null;
                    tokenManager.notify();
                }else{
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Utilizado por terceiro para solicitar acesso aos recursos
     * @param handlerClient
     */
    public void requestMachineResouces(HandlerClient handlerClient){
        this.buffer = handlerClient;
        notify();
    }

    public void setTokenManager(ClientTokensManager tokenManager) {
        this.tokenManager = tokenManager;
    }

}
