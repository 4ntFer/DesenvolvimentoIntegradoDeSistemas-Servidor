package Server.ResoucesManager;

import Server.HandlerClient;
import Server.Singletons;

import java.util.*;

public class ClientTokensManager extends Thread{
    //TODO: Limitar tamanho da fila do cliente
    private HashMap<String, LinkedList<HandlerClient>> userRequests = new HashMap<>(); /** Usuario, fila **/
    private ArrayList<String> allUsers = new ArrayList<>();
    private MachineResoucesManager machineResoucesManager = Singletons.getResoucesManager();
    public ClientTokensManager(){

        start();
    }

    @Override
    public void run(){
        /** Implementação de RoadRobin **/

        while(true){
            for(int i = 0 ; i < allUsers.size() ; i ++){
                String user = allUsers.get(i);
                HandlerClient handlerClient = null;
                if(userRequests.get(user) != null) {
                    if (userRequests.get(user).size() != 0)
                        handlerClient = userRequests.get(user).removeFirst();
                    else userRequests.remove(user);

                    try {
                        if (handlerClient != null)
                            waitResoucesGrant(handlerClient);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    userRequests.put(user, new LinkedList<>());
                }
            }
        }
    }

    /**
     * Espera até que seja consedido o recurso ao cliente
     * @param handlerClient
     * @throws InterruptedException
     */
    private void waitResoucesGrant(HandlerClient handlerClient) throws InterruptedException {
        synchronized (this) {
            machineResoucesManager.requestMachineResouces(handlerClient);
            wait();
        }
    }

    /**
     * Insere um processamento na fila;
     * @param user Identificador do usuário
     * @param handlerClient objeto resposável pelo processamento.
     */
    public void requestProcessing(String user, HandlerClient handlerClient) {
        if(userRequests.get(user)==null){
            allUsers.add(user);
            LinkedList list = new LinkedList();
            list.add(handlerClient);
            userRequests.put(user, list);
        }else{

            userRequests.get(user).add(handlerClient);
        }
    }

    public void setMachineResoucesManager(MachineResoucesManager machineResoucesManager) {
        this.machineResoucesManager = machineResoucesManager;
    }
}
