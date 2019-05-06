package cds.sink;

import cds.Model;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ModelMerger extends Thread {

    private SinkCommunicationModelHandler sinkToNode;
    private int nodes;

    public ModelMerger( SinkCommunicationModelHandler sinkToNode, int nodes ) {
        this.sinkToNode = sinkToNode;
        this.nodes = nodes;
    }

    @Override
    public void run() {
        try {
            HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
                    .header("content-type", "application/json")
                    .body("{\n\t\"command\":\"Merge\"\n\t\"nodes\":\"" + nodes + "}")
                    .asString();
            System.out.println(response);

            sinkToNode.publishNewModel( getMergedModel());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private Model getMergedModel() {
        String json = "";
        try {
            json = new String ( Files.readAllBytes( Paths.get("MergedModel.json") ) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        Model mergedModel = new Model(-1, json);
        return mergedModel;
    }
}
