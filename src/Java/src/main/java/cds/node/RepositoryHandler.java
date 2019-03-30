package cds.node;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class RepositoryHandler {

	//Values for the Machine Learning
	private int threshold; //The first time the ML will be called only if there are a number of values higher than a certain threshold
	private int oldNumberOfSamples;
	private int newValues; //When we have a number of data higher than the threshold, ML called when there are a certain number of new values
	//Util constants
	private final String samplePath = "../data/collectedData";
	private final String readySamplesPath = "../data/readyData";
	private static AtomicInteger numberOfSamples = new AtomicInteger(0);
	
	//Variable used to manage concurrency like in a FairLock
	private boolean isLocked = false;
	private Thread lockingThread = null;
	private List<String> waitingThreads = new ArrayList<>();
	
	public RepositoryHandler(int threshold, int newValues) {
		this.threshold = threshold;
		this.newValues = newValues;
		this.oldNumberOfSamples = 0;
	}
	
	public void lock() throws InterruptedException{
		//Qui bisogna metterci il monitor del thread o 
		//un queueobject che lo rappresenta insomma, 
		//però credo che vada bene così 
//		System.out.println("Sono nel lock!");
		String activeThread = Thread.currentThread().getName();
//		System.out.println(activeThread);
		
		synchronized(this) {
//			System.out.println("In the synchronized part");
			waitingThreads.add(activeThread);
			
			while(isLocked || waitingThreads.get(0) != activeThread) {
//				System.out.println("In the synchronized part the while cicle!");				
				synchronized(activeThread) {
					try {
						activeThread.wait();
					}catch(InterruptedException e){
						waitingThreads.remove(activeThread);
						throw e;
					}
				}
			}
			waitingThreads.remove(activeThread);
			isLocked = true;
			lockingThread = Thread.currentThread();
//			System.out.println("I get the lock");
		}
	}
	
	public void unlock() {
//		System.out.println("Unlock attempt");
		if(!isLocked || this.lockingThread != Thread.currentThread()) {
			throw new IllegalMonitorStateException("Calling thread has not locked this lock");
		}
		isLocked = false;
		lockingThread = null;
		if(waitingThreads.size() > 0) {
			String sleepingThread = waitingThreads.get(0);
			synchronized (sleepingThread) {
				sleepingThread.notify();
			}
		}
	}
	
	public void write(Double sensedData) throws InterruptedException{
		this.lock();
		
		numberOfSamples.incrementAndGet();
		
		try(
			FileWriter fw = new FileWriter(samplePath, true);
				)
		{
			System.out.println(Thread.currentThread().getName() + " inserted is data in the repository");
			fw.append(sensedData.toString() + "\n");
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Number of data collected : " + numberOfSamples.get());
		
		this.unlock();
	}
	
	public void read(int numberOfReads) throws InterruptedException {
		while(numberOfReads > 0){
		
			this.lock();

			int instantNumberOfSamples = numberOfSamples.get();
			
			if((oldNumberOfSamples == 0 && instantNumberOfSamples >= threshold) 
					|| (oldNumberOfSamples > 0 && instantNumberOfSamples - oldNumberOfSamples >= newValues ) 
			){
				try(FileWriter fw = new FileWriter(readySamplesPath, true);
						){
					String readyData = new String (Files.readAllBytes(Paths.get(samplePath)));
					System.out.println(readyData);
					if(oldNumberOfSamples == 0)
						fw.write(readyData);
					else
						fw.append(readyData);
					//Call the function to send the data 
					numberOfReads--;
					oldNumberOfSamples = instantNumberOfSamples;
					
					//We flush the file with the sample not ready to reduce redundancy of the data
					try(FileWriter fw2 = new FileWriter(samplePath);){
						fw2.write("");
					}catch(IOException ex) {
						System.out.println(ex.getMessage());
					}
					
				}catch(IOException e) {
					System.out.println(e.getMessage());
				}
			}
			
			this.unlock();
		
		}
	}
}