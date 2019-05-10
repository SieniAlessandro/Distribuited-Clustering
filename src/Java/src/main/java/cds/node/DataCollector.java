package cds.node;

public class DataCollector {

	//Read Data variables
	public final static int threshold = 100;
	public final static int newValues = 20;
	//Testing variables
	public final static int numberOfThreads = 100; 
	public final static int numberOfWrites = 5;
	
	public static boolean aModelIsBeingGeneretedNow = false;
	
	public static void main(String[] args) {
		RepositoryHandler repository = new RepositoryHandler(threshold, newValues);

		for(int i = 0; i < numberOfThreads; i++) {
			Runnable r = new DataGenerator(repository, numberOfWrites);
			new Thread(r).start();
		}		
	}

}
