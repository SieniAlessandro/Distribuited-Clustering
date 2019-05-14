package it.unipi.cds.federatedLearning.node;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class ModelCaller implements Runnable{

	int values;
	
	public ModelCaller(int valuesToRead) {
		// TODO Auto-generated constructor stub
		values = valuesToRead;
	}
	@Override
	public void run() {
		try {
			HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
					  .header("content-type", "application/json")
					  .body("{\n\t\"command\":\"Train\",\n\t\"values\":\""+values+"\"\n}")
					  .asString();
			
			DataCollector.aModelIsBeingGeneretedNow = true;
			//System.out.println(response.getBody());
			//check della risposta
			//chiamta alla funzione di rabbitMQ se necessario
			//if(response.getBody().equals("\"Model created\"\n")) {
			switch (response.getStatus()) {
			case 201:
				//Model created
				DataCollector.nodeCommunicationHandler.sendModel();
				break;
			
			case 204:
				//Correct response from the rest server but model not updated
				System.out.println("No need to send the new model");
				break;
			default:
				System.err.println("REST SERVER BUG!!");
				return;
			}
			
			DataCollector.aModelIsBeingGeneretedNow = false;
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
