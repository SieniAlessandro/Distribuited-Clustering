package cds.node;

public class DataCollector {

	//Read Data variables
	private final static int threshold = 10;
	private final static int newValues = 5;
	//Testing variables
	private final static int numberOfThreads = 10; 
	private final static int numberOfWrites = 10;
	private final static int numberOfReads = (numberOfThreads*numberOfWrites-threshold)/newValues+1;

	public static void main(String[] args) {
		RepositoryHandler repository = new RepositoryHandler(threshold, newValues);
		
		for(int i = 0; i < numberOfThreads; i++) {
			Runnable r = new DataGenerator(repository, numberOfWrites);
			new Thread(r).start();
		}
		
		try {
			repository.read(numberOfReads);
		}catch(InterruptedException e) {
			System.out.println(e.getMessage());
			System.out.println("Data Collector has been interrupted");
		}
		
	}

}
