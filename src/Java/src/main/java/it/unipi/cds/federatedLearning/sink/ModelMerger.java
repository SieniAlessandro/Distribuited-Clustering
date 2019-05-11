package it.unipi.cds.federatedLearning.sink;

import it.unipi.cds.federatedLearning.Log;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * ModelMerger waits for the RestServer's response and the send a new model to nodes
 */
public class ModelMerger extends Thread {

    private int nodes;
    private SinkCommunicationModelHandler sc;

    /**
     * @param nodes current number of nodes
     * @param sc instance of a SinkCommunicationModelHandler
     */
    ModelMerger(int nodes, SinkCommunicationModelHandler sc) {
        this.nodes = nodes;
        this.sc = sc;
    }

    /**
     * Post a request to the RestServer with command Merge and wait for the response. If the code status is CREATED,
     * then send the merged model to the nodes
     */
    @Override
    public void run() {
        try {
            Log.info("ModelMerger", "Model merger started");
            HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
                    .header("content-type", "application/json")
                    .body("{\n\t\"command\":\"Merge\",\n\t\"nodes\":\""+nodes+"\"\n}")
                    .asString();
            switch (response.getStatus()) {
                case 201:
                    sc.sendModel();
                    break;
                case 500:
                    Log.error("ModelMerger", "Merging failed");
                    break;
                default:
                    Log.error("ModelMerger", "Unsupported Response Status: " + response.getStatus());
                    break;
            }
        } catch (UnirestException e) {
            Log.error("ModelMerger", e.toString());
        }
    }
}
