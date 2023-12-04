package Server.Integration;

public class ServerResponseBody {

    //Head
    private String user;
    private String algorithm;
    private String pixels;
    private int iteractions;
    private String starts;
    private String ends;
    private double averageCpuUsage;
    private double averageMemoryUsage;
    private double[] cpuUsages;
    private double[] memUsages;

    //Image
    private byte[] image;

    public ServerResponseBody(
            String user,
            String algorithm,
            String pixels,
            int iteractions,
            String starts,
            String ends,
            double averageCpuUsage,
            double averageMemoryUsage,
            byte[] image
    ) {


        this.user = user;
        this.algorithm = algorithm;
        this.pixels = pixels;
        this.iteractions = iteractions;
        this.starts = starts;
        this.ends = ends;
        this.averageCpuUsage = averageCpuUsage;
        this.averageMemoryUsage = averageMemoryUsage;
        this.image = image;
    }

    public ServerResponseBody(){};


    public void setUser(String user) {
        this.user = user;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setPixels(String pixels) {
        this.pixels = pixels;
    }

    public void setIteractions(int iteractions) {
        this.iteractions = iteractions;
    }

    public void setStarts(String starts) {
        this.starts = starts;
    }

    public void setEnds(String ends) {
        this.ends = ends;
    }

    public void setAverageCpuUsage(double averageCpuUsage) {
        this.averageCpuUsage = averageCpuUsage;
    }

    public void setAverageMemoryUsage(double averageMemoryUsage) {
        this.averageMemoryUsage = averageMemoryUsage;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getUser() {
        return user;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getPixels() {
        return pixels;
    }

    public int getIteractions() {
        return iteractions;
    }

    public String getStarts() {
        return starts;
    }

    public String getEnds() {
        return ends;
    }

    public double getAverageCpuUsage() {
        return averageCpuUsage;
    }

    public double getAverageMemoryUsage() {
        return averageMemoryUsage;
    }

    public void setCpuUsages(double[] cpuUsages) {
        this.cpuUsages = cpuUsages;
    }

    public void setMemUsages(double[] memUsages) {
        this.memUsages = memUsages;
    }


    public byte[] getImage() {
        return image;
    }

    public String getJson(){
            String json = "{\n" +
                    "\"user\" : " + "\""+ user +"\",\n" +
                    "\"algorithm\" : " + "\""+ algorithm +"\",\n" +
                    "\"pixels\" : " + "\""+ pixels +"\",\n" +
                    "\"iteractions\" : " + iteractions + ",\n" +
                    "\"starts\" : " + "\""+ starts +"\",\n" +
                    "\"ends\" : " + "\""+ ends +"\",\n" +
                    "\"avarageCPU\" :" + averageCpuUsage + ",\n" +
                    "\"avarageMEM\" :" + averageMemoryUsage + ",\n";

            String cpuUsage = "\"cpuUsages\" : [";
            String memUsage = "\"memUsages\" : [";
            String imageBytes = "\"imageBytes\" : [";

            for(int i = 0 ; i < this.cpuUsages.length ; i ++){
                cpuUsage += this.cpuUsages[i];
                memUsage += this.memUsages[i];

                if(i + 1 != this.cpuUsages.length){
                    cpuUsage += ", ";
                    memUsage += ", ";
                }else{
                    cpuUsage += "]";
                    memUsage += "]";
                }
            }

            json += cpuUsage +", \n" + memUsage + ", \n";

            for(int i = 0; i < image.length ; i++){
                imageBytes += image[i];

                if(i + 1 != image.length){
                    imageBytes += ", ";
                }else{
                    imageBytes += "]";
                }
            }

            json += imageBytes + "\n}";

            return json;
    }
}
