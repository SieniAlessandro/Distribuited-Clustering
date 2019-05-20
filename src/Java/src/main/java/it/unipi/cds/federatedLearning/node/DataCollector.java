package it.unipi.cds.federatedLearning.node;

import it.unipi.cds.federatedLearning.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to start a simulated connection with simulated sensor nodes
 * in order to collect data and to start the process that communicates with the sink
 * using RabbitMQ. 
 * Here are stored some constants used to decide how many values will be stored, how many thread 
 * (simulating the sensors) will start and the various threshold used to decide if the machine 
 * learning algorithm, that generates the neural network, will be started
 *
 */
public class DataCollector {

	/*
	 * Read Data variables
	 */
	public final static int threshold = 100;
	public final static int newValues = 20;
	/*
	 * Testing variables
	 */
	public final static int numberOfThreads = 100; 
	public final static int numberOfWrites = 10;
	
	public static boolean aModelIsBeingGeneratedNow = false;
	public static NodeCommunicationModelHandler nodeCommunicationHandler;

	public static void main(String[] args) throws InterruptedException {
		try {
			nodeCommunicationHandler = new NodeCommunicationModelHandler(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.error("Sink", "Provide RabbitMQ Server's ip address as argument");
		}
		ArrayList<Thread> threads = new ArrayList<>();
		RepositoryHandler repository = new RepositoryHandler(threshold, newValues);
		for(int i = 0; i < numberOfThreads; i++) {
			/*
			 * the last parameter is used for to specify if there is an infinite number of writes or not
			 */
			Runnable r = new DataGenerator(repository, numberOfWrites, false);
			Thread t = new Thread(r);
			t.start();
			threads.add(t);
		}
		/*
		 * USED ONLY IF THE NUMBER OF WRITES IS NOT INFINITE
		 */
		for ( Thread t : threads )
			t.join();

		try {
			DataCollector.nodeCommunicationHandler.callFunction("Leave");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
