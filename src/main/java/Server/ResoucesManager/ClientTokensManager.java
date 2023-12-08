package Server.ResoucesManager;

import Server.HandlerClient;
import Server.Singletons;
import com.sun.management.OperatingSystemMXBean;

import java.util.*;

public class ClientTokensManager extends Thread {
    //TODO: Limitar tamanho da fila do cliente

    private static final long SIZE_60_60_MEM = 7049672752L;
    private static final long SIZE_30_30_MEM = 1379554848L;

    private static final double SIZE_60_60_CPU = 0.50f;
    private static final double SIZE_30_30_CPU = 0.25f;

    private OperatingSystemMXBean systemMXBean;
    private HashMap<String, LinkedList<HandlerClient>> userRequests;// <Usuario, fila>
    private ArrayList<String> allUsers; // identificador de todos os usuarios

    public ClientTokensManager() {
        systemMXBean = Singletons.getSystemMXBean();
        userRequests= new HashMap<>();
        allUsers = new ArrayList<>();

        this.start();
    }

    @Override
    public void run(){
        /** Implementação de RoadRobin **/
        boolean flag = false;
        while (true) {
            flag = false;
            System.out.println("ativa");
            for (int i = 0; i < allUsers.size(); i++) {
                String user = allUsers.get(i);
                HandlerClient handlerClient = null;
                if (userRequests.get(user) != null) {
                    if (userRequests.get(user).size() != 0)
                        handlerClient = userRequests.get(user).removeFirst();
                    else userRequests.remove(user);

                    try {
                        if (handlerClient != null)
                            waitResoucesGrant(handlerClient);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    userRequests.put(user, new LinkedList<>());
                }

                flag = true;
            }

            if(flag == false){
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }



    /**
     * Espera até que seja consedido o recurso ao cliente, chamado ao longo da execução da thread
     *
     * @param handlerClient
     * @throws InterruptedException
     */
    private void waitResoucesGrant(HandlerClient handlerClient) throws InterruptedException {
        HandlerClient buffer = handlerClient;

        /**
         * Verifica se há recursos para o processamento
         */
        double necessaryCPU = 0;
        long necessaryMEM = 0;

        switch (buffer.getImageSize()) {
            case "60x60":
                necessaryCPU = SIZE_60_60_CPU;
                necessaryMEM = SIZE_60_60_MEM;
                break;
            case "30x30":
                necessaryCPU = SIZE_30_30_CPU;
                necessaryMEM = SIZE_30_30_MEM;
                break;
        }

        while (buffer != null) {
            if (
                    (1f - systemMXBean.getCpuLoad()) * 0.75 > necessaryCPU &&
                            Runtime.getRuntime().freeMemory() * 0.75 > necessaryMEM
            ) {
                buffer.start();
                buffer = null;
            } else {
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Insere um processamento na fila;
     *
     * @param user          Identificador do usuário
     * @param handlerClient objeto resposável pelo processamento.
     */
    public synchronized void requestProcessing(String user, HandlerClient handlerClient) {
        if (userRequests.get(user) == null) {
            allUsers.add(user);
            LinkedList list = new LinkedList();
            list.add(handlerClient);
            userRequests.put(user, list);
        } else {

            userRequests.get(user).add(handlerClient);
        }

        notify();
    }
}
