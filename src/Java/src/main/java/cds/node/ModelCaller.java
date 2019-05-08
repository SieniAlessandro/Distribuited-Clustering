package cds.node;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import cds.node.NodeCommunicationModelHandler;

public class ModelCaller implements Runnable{

	@Override
	public void run() {
		try {
			HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
					  .header("content-type", "application/json")
					  .body("{\n\t\"command\":\"Train\"\n}")
					  .asString();
			System.out.println(response.getBody());
			//check della risposta
			//chiamta alla funzione di rabbitMQ se necessario
			if(response.getBody().equals("Model created")) {
				NodeCommunicationModelHandler.sendModelToSink();
			}
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
