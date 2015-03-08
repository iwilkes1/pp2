import java.util.Random;

/**
 * Class to flip coins in parallel.
 * @author Ian Wilkes
 *
 */
public class CoinFlipper implements Runnable {

	private int thread_id;
	private static int flips;
	private static int threads; 
	private int numFlips;
	
	private volatile static int heads;
	private volatile static int tails;
	
	private Random flipper;
	
	/**
	 * Constructor for the coin flipping class
	 * @param id the thread id.
	 * @param numFlips the number of flips of this thread.
	 */
	public CoinFlipper(int id, int numFlips) {
		this.thread_id = id;
		this.numFlips = numFlips;
		this.flipper = new Random();
	}
	
	/**
	 * Main Method to flip coins.
	 * @param args The first argument is the number of threads to use, the second is
	 * the number of coin flips to execute.
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.out.println("Usage: <threads> <coin flips>");
			return;
		}
		threads = Integer.parseInt(args[0]);
		flips = Integer.parseInt(args[1]);

		CoinFlipper.heads = 0;
		CoinFlipper.tails = 0;
		
		Thread[] threadsToRun = new Thread[threads];

		long startTime = System.currentTimeMillis();
		
		// Start Threads
		for (int i = 0; i < threads; i++) {
			threadsToRun[i] = new Thread(new CoinFlipper(i, flips/threads));
			threadsToRun[i].start();
		}
		
		// wait for threads to finish
		for (int i = 0; i < threads; i++) {
			try {
				threadsToRun[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		
		// print results
		long endTime = System.currentTimeMillis();
		System.out.println("Heads: " + CoinFlipper.heads 
			+ " in " + flips + " flips" + 
			"\nTime taken: " + (endTime - startTime) + "ms");
	}
	@Override
	public void run() {
		for (int i = 0; i < this.numFlips; i++) {
			if (flipper.nextInt(2) == 1) {
				synchronized(CoinFlipper.class) {CoinFlipper.heads++;}
			} else {
				synchronized(CoinFlipper.class) {CoinFlipper.tails++;}
			}
		}
	}

}
