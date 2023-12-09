package Server.ResoucesManager;

import ProcessImg.Algorithms.CGNE;
import ProcessImg.Algorithms.CGNR;
import ProcessImg.Image;
import Server.HandlerClient;
import Server.Integration.ImageProcessSolicitation;
import Server.Integration.ServerResponseBody;
import Server.MachineMonitor.MachineResoucesMonitor;
import Server.MachineMonitor.MonitorStillActiveExcpetion;
import Server.Singletons;
import com.sun.management.OperatingSystemMXBean;
import org.jblas.DoubleMatrix;
import utils.ServerResouces;

import java.io.IOException;
import java.security.AllPermission;
import java.time.LocalDateTime;
import java.util.*;

public class ClientTokensManager extends Thread {
    //TODO: Limitar tamanho da fila do cliente

    private static final long SIZE_60_60_MEM = 7149672752L;
    private static final long SIZE_30_30_MEM = 1579554848L;

    private static final double SIZE_60_60_CPU = 0.70f;
    private static final double SIZE_30_30_CPU = 0.40f;

    private OperatingSystemMXBean systemMXBean;
    private HashMap<String, LinkedList<ImageProcessSolicitation>> userRequests;// <Usuario, fila de solicitaçÕes
    private HashMap<ImageProcessSolicitation, HandlerClient> client;// <solicitação, interface com cliente>
    private ArrayList<String> allUsers; // identificador de todos os usuarios


    public ClientTokensManager(){
        userRequests = new HashMap<>();
        client = new HashMap<>();
        allUsers = new ArrayList<>();
        systemMXBean = Singletons.getSystemMXBean();

        start();
    }

    @Override
    public void run(){
            int currentUser = 0;
            int k = 10;
            while (true) {
                /** Para cada usuario**/

                if (!allUsers.isEmpty()) {
                    ImageProcessSolicitation solicitation = null;
                    LinkedList<ImageProcessSolicitation> fila = userRequests.get(allUsers.get(currentUser));

                    if (fila != null) {
                        solicitation = fila.peekFirst();
                    }

                    if (solicitation != null) {
                        client.get(solicitation).confirmAuthorization();
                        if (authorizesProcess(solicitation)) {
                            userRequests.get(allUsers.get(currentUser)).removeFirst();
                            client.get(solicitation).confirmAuthorization();
                            client.remove(solicitation);


                            if (userRequests.get(allUsers.get(currentUser)).isEmpty()) {
                                userRequests.remove(allUsers.get(currentUser));
                                allUsers.remove(currentUser);
                            }

                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            currentUser++;
                            k = 1;
                        } else {
                            try {
                                sleep(500 * k);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            if (k < 30) {
                                k++;
                            }
                        }
                    }

                    if (currentUser >= allUsers.size()) {
                        currentUser = 0;
                    }

                    System.gc();
                } else {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
    }

    private boolean authorizesProcess(ImageProcessSolicitation solicitation){
        /**
         * Verifica se há recursos para o processamento
         */
        double necessaryCPU = 0;
        long necessaryMEM = 0;

        switch (solicitation.getDimensions()) {
            case "60x60":
                necessaryCPU = SIZE_60_60_CPU;
                necessaryMEM = SIZE_60_60_MEM;
                break;
            case "30x30":
                necessaryCPU = SIZE_30_30_CPU;
                necessaryMEM = SIZE_30_30_MEM;
                break;
        }
        System.out.println((1.0 - systemMXBean.getCpuLoad()));
        if (
                (1.0 - systemMXBean.getCpuLoad()) > necessaryCPU &&
                        Runtime.getRuntime().freeMemory() > necessaryMEM
        ) {
            return true;
            } else {
                return false;
            }
    }
    public synchronized void requestsAcess(ImageProcessSolicitation solicitation, HandlerClient client){
        String user = solicitation.getUser();
        allUsers.add(user);

        if(userRequests.containsKey(user)){
            userRequests.get(user).add(solicitation);
        }else{
            LinkedList<ImageProcessSolicitation> list = new LinkedList<ImageProcessSolicitation>();
            list.add(solicitation);
            userRequests.put(user, list);
        }

        this.client.put(solicitation, client);

    }

}
