package cds.sink;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


public class ModelMerger extends Thread {

    private int nodes;
    private SinkCommunicationModelHandler sc;
    
    public ModelMerger( int nodes, SinkCommunicationModelHandler sc) {
        this.nodes = nodes;
        this.sc = sc;
    }

    @Override
    public void run() {
        try {
            System.out.println("Model merger started!!");
            HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
                    .header("content-type", "application/json")
                    .body("{\n\t\"command\":\"Merge\"\n}")
                    .asString();
            System.out.println(response.getBody());
            sc.sendModel();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
