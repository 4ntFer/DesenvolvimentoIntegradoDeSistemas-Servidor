package Server;

import ProcessImg.Algorithms.CGNE;
import ProcessImg.Algorithms.CGNR;
import ProcessImg.Image;
import Server.Integration.HttpRequest;
import Server.Integration.ImageProcessSolicitation;
import Server.Integration.ServerResponseBody;
import Server.MachineMonitor.MachineResoucesMonitor;
import Server.MachineMonitor.MonitorStillActiveExcpetion;
import com.google.gson.Gson;
import com.sun.management.OperatingSystemMXBean;
import org.jblas.DoubleMatrix;
import utils.ServerResouces;

import javax.xml.xpath.XPath;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HandlerClient extends Thread{
    private Socket clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private OperatingSystemMXBean OSMXbean;
    private ImageProcessSolicitation solicitation = null;
    private boolean authirizated = false;

    /**
     * @param clientSocket Socket do cliente.
     * @throws IOException
     */
    public HandlerClient(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        OSMXbean = Singletons.getSystemMXBean();
        System.out.println(super.getId()+ ":" + "New connection accepted.");

        start();
    }

    @Override
    public void run(){
        HttpRequest request = null;
        try {
            System.out.println(super.getId()+ ":" + "Reading request");
            request = readRequest();



            if(request != null){
                //System.out.println(new String(request.getBody()));
                String json = new String(request.getBody());
                byte[] image;
                if(!json.equals("")) {
                    ImageProcessSolicitation solicitation =
                            new Gson().fromJson(
                                    json,
                                    ImageProcessSolicitation.class
                            );

                    System.out.println(super.getId()+ ":" + "Request is valide");

                    /**
                     * solicita acesso
                     */
                    Singletons.getTokensManager().requestsAcess(solicitation, this);
                    int k = 10;
                    System.out.println(super.getId()+ ":" + "Wainting authorize");
                    while(!authirizated){
                        sleep(100*k);
                        System.out.println(super.getId()+ ":" + "Wainting authorize");
                        if(k>1){k--;}
                    }

                    /**
                     * Processando a imagem
                     */

                    ServerResponseBody responseBody = getResponse(solicitation);
                    System.out.println(super.getId()+ ": " + "Send 200 OK response with image");
                    sendResponse(responseBody.getJson());

                }else{
                    System.out.println(super.getId()+ ":" + "Request is not valide");
                    System.out.println(super.getId()+ ":" + "Send 404 reponse");
                    sendReponse404();
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(super.getId()+ ": " + "Finalizing thread");
    }
    private void sendReponse404() throws IOException {
        StringBuilder responseMetadata = new StringBuilder();

        responseMetadata.append("HTTP/1.1 404 Not Found\r\n");
        responseMetadata.append("Access-Control-Allow-Origin: *\r\n");

        responseMetadata.append("\r\n");

        outputStream.write(responseMetadata.toString().getBytes(StandardCharsets.UTF_8));
    }
    private void sendResponse(String body) throws IOException {
        String contentType = "application/json";
        int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        StringBuilder responseMetadata = new StringBuilder();

        responseMetadata.append("HTTP/1.1 200 OK\r\n");
        responseMetadata.append("Content-Type: "+ contentType +"\r\n");
        responseMetadata.append("Content-Length: "+ contentLength + "\r\n");
        responseMetadata.append("Access-Control-Allow-Origin: *\r\n");

        responseMetadata.append("\r\n");
        responseMetadata.append(body);

        outputStream.write(responseMetadata.toString().getBytes(StandardCharsets.UTF_8));
    }

    private HttpRequest readRequest() throws IOException {
        /**
         * Lendo mensagem HTTP.
         */

        StringBuilder result = new StringBuilder();

        do {
            result.append((char) inputStream.read());
        } while (inputStream.available() > 0);

        //System.out.println(result.toString());
        return parseData(new ByteArrayInputStream(
                result.toString().getBytes(StandardCharsets.UTF_8)
        ));
    }

    private ServerResponseBody getResponse(ImageProcessSolicitation solicitation){

        /**
         * Atributos para a incialização do model da resposta.
         */
        String user = solicitation.getUser();
        String algorithm = solicitation.getAlgorithm();
        String pixels = solicitation.getDimensions();
        String starts;
        String ends;
        double averageCpuUsage;
        double averageMemoryUsage;
        double[] cpuUsages;
        double[] memUsages;
        Image image;

        ServerResponseBody response = new ServerResponseBody();

        /**
         * Demais variaveis.
         */
        MachineResoucesMonitor machineResoucesMonitor = new MachineResoucesMonitor(OSMXbean);

        try {

            String[] aux = LocalDateTime.now().toString().split("T");
            starts = aux[0] + " " + aux[1];


            machineResoucesMonitor.start();

            image = getImage(solicitation, response); /**Executing algorithm**/

            machineResoucesMonitor.stopRunning();


            aux = LocalDateTime.now().toString().split("T");
            ends = aux[0] + " " + aux[1];

            averageCpuUsage = machineResoucesMonitor.getAvarageCpuUsage();
            averageMemoryUsage = machineResoucesMonitor.getAvarageMemoryUsage();
            cpuUsages = new double[machineResoucesMonitor.getCpuUsage().size()];
            memUsages = new double[machineResoucesMonitor.getMemoryUsage().size()];

            for(int i = 0 ; i < cpuUsages.length ; i ++){
                cpuUsages[i] = machineResoucesMonitor.getCpuUsage().get(i);
                memUsages[i] = machineResoucesMonitor.getMemoryUsage().get(i);
            }

            System.out.println(
                    super.getId() + ":\n" +
                    "\tStarts: " + starts + ", ends: "+ ends + "\n" +
                    "\tAvarage CPU usage: " + averageCpuUsage*100 + ", Avarage Memory Usage: " + averageMemoryUsage*100 + "\n" +
                    "\tIteractions number: " + response.getIteractions()
            );

            response.setUser(solicitation.getUser());
            response.setAlgorithm(solicitation.getAlgorithm());
            response.setPixels(solicitation.getDimensions());
            response.setStarts(starts);
            response.setEnds(ends);
            response.setAverageMemoryUsage(averageMemoryUsage);
            response.setAverageCpuUsage(averageCpuUsage);
            response.setCpuUsages(cpuUsages);
            response.setMemUsages(memUsages);
            response.setImage(image.toByteArray());

            saveResponse(
                    solicitation.getUser(),
                    response.getMetaDatasJson(),
                    response.getStarts().substring(0,10) +"-"+ Calendar.getInstance().getTimeInMillis(),
                    image
            );

            return response;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MonitorStillActiveExcpetion e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param solicitation Solicitação de processamento de imagem enviada pelo cliente
     * @param response A resposta que deverá ser retornada ao servidor (Irá preencher o campo de numero de iterações)
     * @return
     */
    private Image getImage(ImageProcessSolicitation solicitation, ServerResponseBody response) throws IOException {
        String matrixPath = "res/MatrixesModels/" + solicitation.getMatrixModel() + ".csv";

        System.out.println(super.getId() + ": " +" Opening matrix file.");
        double[][] matrix = ServerResouces.getMatrixData(matrixPath);

        DoubleMatrix H = new DoubleMatrix(matrix);
        DoubleMatrix g = new DoubleMatrix(solicitation.getSignal());

        DoubleMatrix result = new DoubleMatrix();

        int dimensionX = Integer.valueOf(solicitation.getDimensions().split("x")[0]);
        int dimensionY = Integer.valueOf(solicitation.getDimensions().split("x")[1]);

        int iteractions = 0;

        Image image;

        //TODO: GAIN!

        switch (solicitation.getAlgorithm()){
            case "CGNE":

                System.out.println(super.getId() + ": Executing CGNE");
                iteractions =  CGNE.algorithm(H,g, result);
                System.out.println(super.getId() + ": CGNE finalized");

                break;

            case "CGNR":

                System.out.println(super.getId() + ": Executing CGNR");
                iteractions = CGNR.algorithm(H, g, result);
                System.out.println(super.getId() + ": CGNR finalized");

                break;
        }

        result = result.reshape(dimensionX, dimensionY);

        image = new Image(result);

        response.setIteractions(iteractions);

        return image;
    }

    /**
     * @return Requisição. null caso a mensagem http seja vazia.
     * @throws IOException
     */


    /**
     * Desserealiza a mensasagem HTTP.
     *
     * Body só pode conter um json! (REVISITAR E CORRIGIR)
     *
     * @param data um ByteArrayInputStream iniciado com os bytes
     *             da string da mensagem.
     * @return Retorna a mensagem contida no data
     *         encapsualda em um objeto HttpRequest.
     * @throws IOException
     */
    private HttpRequest parseData(InputStream data) {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(data)
        );

        /**
         * Lendo Headers.
         *
         * No momento que encontra \n, para para ler o body.
         */

        try {
            String firstLine = null;
            firstLine = bufferedReader.readLine();

            String method = ""; // Será sempre GET
            String url = ""; // Sempre um PNG

            if(firstLine != null){
                String[] lineValues = firstLine.split("\\s+");

                method = lineValues[0];
                if(lineValues.length > 1){
                    url = lineValues[1];
                }


                // Coletando cabeçalhos
                Map<String, String> headers = new HashMap<String,String>();
                String headerLine;
                boolean foundBody = false;
                while((headerLine = bufferedReader.readLine()) != null && !foundBody){
                    if(!headerLine.trim().isEmpty()){
                        lineValues = headerLine.split(":\\s");

                        if(lineValues.length >= 2) {
                            String key = lineValues[0];
                            String value = lineValues[1];

                            headers.put(key, value);
                        }
                    }else{
                        //Pula uma linha
                        foundBody = true;
                    }

                }
                /**
                 * Lendo body.
                 */

                String body =  "{";
                String bodyLine = "";
                while((bodyLine = bufferedReader.readLine()) != null){
                    if(!bodyLine.trim().isEmpty())
                        body = body + bodyLine;

                }

                if(body == "{"){
                    body = "";
                }

                return  new HttpRequest(method, url, headers, body);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void saveResponse(String user, String metadata, String time, Image image) throws IOException {
        String path = "res/PreviousProcessing/" + user + "";
        File dir = new File(path);

        if(!dir.exists()){
            dir.mkdirs();
        }

        dir = new File(path + "/" + time);
        dir.mkdirs();

        image.saveImage(path +"/"+ time);
        Files.write(Path.of((path +"/"+ time +"/meta.txt")), metadata.getBytes());
    }

    public String getImageSize(){
        return solicitation.getDimensions();
    }

    public void confirmAuthorization(){
        authirizated = true;
    }


}
