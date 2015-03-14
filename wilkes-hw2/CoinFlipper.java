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

	private volatile int heads;
	private volatile int tails;

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

		this.heads = 0;
		this.tails = 0;
	}

	/**
	 * Main Method to flip coins.
	 * @param args The first argument is the number of threads to use, the second is
	 * the number of coin flips to execute.
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Usage: <threads> <coin flips> <iterations>");
			return;
		}
		threads = Integer.parseInt(args[0]);
		flips = Integer.parseInt(args[1]);
		int numIterations = Integer.parseInt(args[2]);
		
		Thread[] threadsToRun = new Thread[threads];
		CoinFlipper[] individualFlippers = new CoinFlipper[threads];

		int totalHeads = 0;
		long startTime = System.currentTimeMillis();

		for (int iterations = 0; iterations < numIterations; iterations++) {
			totalHeads = 0;

			// Start Threads
			for (int i = 0; i < threads; i++) {
				individualFlippers[i] = new CoinFlipper(i, flips/threads);
				threadsToRun[i] = new Thread(individualFlippers[i]);
				threadsToRun[i].start();
			}

			// wait for threads to finish
			for (int i = 0; i < threads; i++) {
				try {
					threadsToRun[i].join();
					totalHeads += individualFlippers[i].heads;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}
		// print results
		long endTime = System.currentTimeMillis();
		//System.out.println((endTime - startTime) + "," + threads + "," + numIterations);
 		System.out.println("Heads: " + totalHeads  + 
				"\nTime taken: " + (endTime - startTime) + "ms");
		
	}
	@Override
	public void run() {
		for (int i = 0; i < this.numFlips; i++) {
			if (flipper.nextInt(2) == 1) {

				heads++;

			} else {

				tails++;

			}
		}
	}

}
