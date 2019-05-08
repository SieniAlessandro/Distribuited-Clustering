package cds.node;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import cds.node.NodeCommunicationModelHandler;

public class ModelCaller implements Runnable{

	private NodeCommunicationModelHandler communicationHandler;
	
	public ModelCaller(NodeCommunicationModelHandler nc) {
		// TODO Auto-generated constructor stub
		communicationHandler = nc;
	}
	@Override
	public void run() {
		try {
			HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
					  .header("content-type", "application/json")
					  .body("{\n\t\"command\":\"Train\"\n}")
					  .asString();
			//System.out.println(response.getBody());
			//check della risposta
			//chiamta alla funzione di rabbitMQ se necessario
			//if(response.getBody().equals("\"Model created\"\n")) {
			switch (response.getStatus()) {
			case 201:
				//Model created
				communicationHandler.sendModel();				
				break;
			
			case 204: 
				//Correct response from the rest server but model not updated
				System.out.println("No need to send the new model");
				break;
			default:
				System.err.println("REST SERVER BUG!!");
				break;
			}
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
