package cds.node;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

public class RepositoryHandler {

	//Values for the Machine Learning
	private int threshold; //The first time the ML will be called only if there are a number of values higher than a certain threshold
	private int oldNumberOfSamples;

	private int newValues; //When we have a number of data higher than the threshold, ML called when there are a certain number of new values
	//Util constants
	private final String dataFolder = "../data/";
	private final String samplePath = dataFolder + "collectedData.txt";
	private final String readySamplesPath = dataFolder + "readyData.txt";
	private static AtomicInteger numberOfSamples = new AtomicInteger(0);
	
	//Variable used to manage concurrency like in a FairLock
	private boolean isLocked = false;
	private Thread lockingThread = null;
	private List<String> waitingThreads = new ArrayList<>();
	//private List<QueueObject> waitingThreads = new ArrayList<QueueObject>();
	private NodeCommunicationModelHandler communicationHandler;
	
	public RepositoryHandler(int threshold, int newValues) {
		this.threshold = threshold;
		this.newValues = newValues;
		this.oldNumberOfSamples = 0;
		try {
				File folder = new File(dataFolder);
				if(!folder.exists())
					folder.mkdir();
				File cd = new File(samplePath);
				File rd = new File(readySamplesPath);
				if(!cd.exists())
					cd.createNewFile();
				if(!rd.exists())
					rd.createNewFile();
		}catch(IOException e) {
			System.out.println(e.getMessage());
			return;
		}
		this.communicationHandler = new NodeCommunicationModelHandler("localhost");
	}
	
	public void lock() throws InterruptedException{
		//Qui bisogna metterci il monitor del thread o 
		//un queueobject che lo rappresenta insomma, 
		//però credo che vada bene così
//		System.out.println("Sono nel lock!");
		
		String activeThread = Thread.currentThread().getName();
//		System.out.println(activeThread);
		
//		QueueObject queueObject = new QueueObject();
		
		synchronized(this) {
//			System.out.println("In the synchronized part");
//			System.out.println(activeThread + " says: Is Locked? " + isLocked);
			waitingThreads.add(activeThread);
//			waitingThreads.add(queueObject);
			
//			while(isLocked || waitingThreads.get(0) != queueObject) {
			while(isLocked || waitingThreads.get(0) != activeThread) {
//				System.out.println(activeThread + " First of the queue: " + waitingThreads.get(0) + " isLocked? " + isLocked + " seconda condizione:" + !waitingThreads.get(0).equals(activeThread));
//				System.out.println("In the synchronized part the while cicle!");				
				synchronized(activeThread) {
//				synchronized (queueObject) {
					try {
						activeThread.wait();
//						System.out.println(activeThread + " MI SONO SVEGLIATO, isLocked è: " + this.isLocked);
//						queueObject.wait();
					}catch(InterruptedException e){
//						waitingThreads.remove(queueObject);
						waitingThreads.remove(activeThread);
						throw e;
					}
				}
			}
			waitingThreads.remove(activeThread);
//			waitingThreads.remove(queueObject);
			isLocked = true;
			lockingThread = Thread.currentThread();
//			System.out.println("I get the lock");
		}
	}
	
	public void unlock() {
		if(!isLocked || this.lockingThread != Thread.currentThread()) {
			throw new IllegalMonitorStateException("Calling thread has not locked this lock");
		}
		this.isLocked = false;
//		System.out.println("Unlocking this isLocked: " + this.isLocked);
		while(isLocked) {}
		lockingThread = null;
		if(waitingThreads.size() > 0) {
			String sleepingThread = waitingThreads.get(0);
//			QueueObject queueObject = waitingThreads.get(0);
			synchronized (sleepingThread) {
//				System.out.println("Wake up " + sleepingThread);
				sleepingThread.notify();
//			synchronized (queueObject) {
//				queueObject.notify();
			}
		}
//		System.out.println("Unlock done");
	}
	
	public void write(Double sensedDataX, Double sensedDataY) throws InterruptedException{
		
		this.lock();
				
		int instantNumberOfSamples = numberOfSamples.incrementAndGet();
		
		try(
			FileWriter fw = new FileWriter(samplePath, true);
				)
		{
			System.out.println(Thread.currentThread().getName() + " inserted its data in the repository");
			fw.append(sensedDataX.toString() + "," + sensedDataY.toString() + "\n");
		}catch(IOException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		System.out.println("Number of data collected : " + numberOfSamples.get());
		
		if((oldNumberOfSamples == 0 && instantNumberOfSamples >= threshold) 
				|| (oldNumberOfSamples > 0 && instantNumberOfSamples - oldNumberOfSamples >= newValues )
		){
			this.read();
			oldNumberOfSamples = instantNumberOfSamples;
		}
		
		this.unlock();
	}
	
	public void read() throws InterruptedException {
		try(FileWriter fw = new FileWriter(readySamplesPath, true);
				){
	
			String readyData = new String (Files.readAllBytes(Paths.get(samplePath)));
			
			System.out.println(readyData);
			
			if(oldNumberOfSamples == 0)
				fw.write(readyData);
			else
				fw.append(readyData);
			//Call the function to send the data
			Runnable caller = new ModelCaller(this.communicationHandler);
			new Thread(caller).start();
			
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
}