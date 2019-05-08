package cds.node;

public class DataCollector {

	//Read Data variables
	private final static int threshold = 100;
	private final static int newValues = 20;
	//Testing variables
	private final static int numberOfThreads = 100; 
	private final static int numberOfWrites = 5;

	public static void main(String[] args) {
		RepositoryHandler repository = new RepositoryHandler(threshold, newValues);

		for(int i = 0; i < numberOfThreads; i++) {
			Runnable r = new DataGenerator(repository, numberOfWrites);
			new Thread(r).start();
		}		
	}

}
