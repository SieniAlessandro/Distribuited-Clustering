package it.unipi.cds.federatedLearning.node;

import java.io.IOException;
import java.util.ArrayList;

public class DataCollector {

	//Read Data variables
	public final static int threshold = 100;
	public final static int newValues = 20;
	//Testing variables
	public final static int numberOfThreads = 100; 
	public final static int numberOfWrites = 5;
	
	public static boolean aModelIsBeingGeneretedNow = false;
	public static NodeCommunicationModelHandler nodeCommunicationHandler = new NodeCommunicationModelHandler("localhost");
	
	public static void main(String[] args) throws InterruptedException {
		RepositoryHandler repository = new RepositoryHandler(threshold, newValues);
		ArrayList<Thread> threads = new ArrayList<>();
		for(int i = 0; i < numberOfThreads; i++) {
			Runnable r = new DataGenerator(repository, numberOfWrites);
			Thread t = new Thread(r);
			t.start();
			threads.add(t);
		}
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
