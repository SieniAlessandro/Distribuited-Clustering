package it.unipi.cds.federatedLearning.node;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import it.unipi.cds.federatedLearning.Config;
import it.unipi.cds.federatedLearning.Log;

/**
 * This class is used to call the machine learning with a REST call
 * 
 */
public class ModelCaller implements Runnable{

	int values;
	
	/**
	 * Constructor
	 * @param valuesToRead the number of new values present in the file used by the machine learning algorithm
	 */
	public ModelCaller(int valuesToRead) {
		values = valuesToRead;
	}
	
	/**
	 * Start a synchronous REST call to start the machine learning algorithm used to Train a neural network and waits for the response
	 * of the rest server, check the response if it is 201 or 204 there are no problem.
	 */
	@Override
	public void run() {
		try {
			
			/*
			 * Handle the varible window size and the fading coefficent  
			 */
			double fadingCoefficent = 0;
			int sizeWindow = Config.SIZE_WINDOW;
			if(Config.SIZE_WINDOW < values) {
				fadingCoefficent = ((double)(values - Config.SIZE_WINDOW))/(double)(values);
				sizeWindow = values;
			}
			
			HttpResponse<String> response = Unirest.post("http://127.0.0.1:5000/server")
					.header("content-type", "application/json")
					.body("{\n\t\"ID\":\""+DataCollector.nodeCommunicationHandler.getNodeID()+
							"\",\n\t\"command\":\"Train\""
							+ ",\n\t\"values\":\""+values+"\","
							+ "\n\t\"Window\": \""+sizeWindow+"\""
							+ ",\n\t\"Coeff\": \""+fadingCoefficent+"\"\n}")
					.asString();
			
			Log.info("Train", "values " + values + " - sizeWindow " + sizeWindow + " - fading coefficent " +  fadingCoefficent);
			
			DataCollector.aModelIsBeingGeneratedNow = true;
			switch (response.getStatus()) {
			case 201:
				/*
				 * Model created
				 */
				DataCollector.nodeCommunicationHandler.sendModel();
				break;
			
			case 204:
				/*
				 * Correct response from the rest server but model not updated
				 */
				Log.info("Model Caller", "No need to send the new model");
				break;
			default:
				Log.info("Model Caller", "REST SERVER PROBLEM - STATUS:" + response.getStatus() + "!!");
				return;
			}			
			DataCollector.aModelIsBeingGeneratedNow = false;
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

}
