package it.unipi.cds.federatedLearning.node;

import it.unipi.cds.federatedLearning.Config;
import it.unipi.cds.federatedLearning.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	public final static int THRESHOLD = Config.SIZE_WINDOW;
	public static int newValuesThreshold = (int) (THRESHOLD*Config.PERCENTAGE_OLD_VALUES);
	/*
	 * Testing variables
	 */
	public final static int numberOfThreads = 100; 
	public final static int numberOfWrites = 10;
	
	public static boolean aModelIsBeingGeneratedNow = false;
	public static NodeCommunicationModelHandler nodeCommunicationHandler;
	
    /*
     * The executor and the array with tasks are kept in private fields
     */
    private ExecutorService myExecutor;
    private DataGenerator generator;

    
	public DataCollector() {
		RepositoryHandler repository = new RepositoryHandler(THRESHOLD, newValuesThreshold);

		myExecutor = Executors.newFixedThreadPool(numberOfThreads);
		generator = new DataGenerator(repository, numberOfWrites, false);
	}




	public static void main(String[] args) throws InterruptedException {
		try {
			nodeCommunicationHandler = new NodeCommunicationModelHandler(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.error("Sink", "Provide RabbitMQ Server's ip address as argument");
		}
		
		DataCollector dc = new DataCollector();
		
		for(int i = 0; i < numberOfThreads; i++) {
			/* 
			 *We use execute instead of submit because we are not interested in the future variable related to the thread
			 *since the Class DataGenerator is an implementation of runnable instead callable and we don't check when the single
			 *threads are terminated
			 */
			dc.myExecutor.execute(dc.generator);
		}
		/*
		 * Waiting the termination of all threads
		 */
		dc.myExecutor.shutdown();
		dc.myExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		try {
			DataCollector.nodeCommunicationHandler.callFunction("Leave");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
