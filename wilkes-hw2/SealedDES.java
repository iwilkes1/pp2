////////////////////////////////////////////////////////////////////////////////
//
//    $Id: SealedDES.java,v 1.1 2008/09/10 20:21:47 randal Exp $
//
//    Randal C. Burns, modified by Ian Wilkes on 3/10/15
//    Department of Computer Science
//    Johns Hopkins University
//
//    $Source: /home/randal/repository/public_html/420/src/SealedDES.java,v $
//    $Date: 2008/09/10 20:21:47 $        
//    $Revision: 1.1 $
//
////////////////////////////////////////////////////////////////////////////////

import javax.crypto.*;

import java.security.*;

import javax.crypto.spec.*;

import java.util.Random;
import java.io.PrintStream;


class SealedDES implements Runnable
{
	// Cipher for the class
	Cipher des_cipher;

	// Key for the class
	SecretKeySpec the_key = null;

	// Byte arrays that hold key block
	byte[] deskeyIN = new byte[8];
	byte[] deskeyOUT = new byte[8];

	private int threadID;
	private long maxKey;
	private long startPoint;
	private long endPoint;
	private SealedObject sldObj;
	private long runStart;
	private SealedDES deccipher;
	
	
	// Constructor: initialize the cipher
	public SealedDES () 
	{
		try 
		{
			des_cipher = Cipher.getInstance("DES");
		} 
		catch ( Exception e )
		{
			System.out.println("Failed to create cipher.  Exception: " + e.toString() +
					" Message: " + e.getMessage()) ; 
		}
	}
	
	/**
	 * Constructor for the parallel implementation of DES key brute forcing.
	 * @param threadID the thread id.
	 * @param startPoint the starting key to explore.  
	 * @param endPoint the last key to explore
	 * @param encryptKey the key used by all 
	 * @param runStart the starting time of thread.
	 */
	public SealedDES(int threadID, long startPoint, long endPoint, long encryptKey, long runStart) {
		this.runStart = runStart;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.threadID = threadID;
		
		try 
		{
			des_cipher = Cipher.getInstance("DES");
		} 
		catch ( Exception e )
		{
			System.out.println("Failed to create cipher.  Exception: " + e.toString() +
					" Message: " + e.getMessage()) ; 
		}
		SealedDES enccipher = new SealedDES ();
		enccipher.setKey(encryptKey);
		
		// Generate a sample string
		String plainstr = "Johns Hopkins afraid of the big bad wolf?";

		// Encrypt
		this.sldObj = enccipher.encrypt ( plainstr );
		
		// Create a simple cipher
		deccipher = new SealedDES ();
		
	}
	

	// Decrypt the SealedObject
	//
	//   arguments: SealedObject that holds on encrypted String
	//   returns: plaintext String or null if a decryption error
	//     This function will often return null when using an incorrect key.
	//
	public String decrypt ( SealedObject cipherObj )
	{
		try 
		{
			return (String)cipherObj.getObject(the_key);
		}
		catch ( Exception e )
		{
			//      System.out.println("Failed to decrypt message. " + ". Exception: " + e.toString()  + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}

	// Encrypt the message
	//
	//  arguments: a String to be encrypted
	//  returns: a SealedObject containing the encrypted string
	//
	public SealedObject encrypt ( String plainstr )
	{
		try 
		{
			des_cipher.init ( Cipher.ENCRYPT_MODE, the_key );
			return new SealedObject( plainstr, des_cipher );
		}
		catch ( Exception e )
		{
			System.out.println("Failed to encrypt message. " + plainstr +
					". Exception: " + e.toString() + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}

	//  Build a DES formatted key
	//
	//  Convert an array of 7 bytes into an array of 8 bytes.
	//
	private static void makeDESKey(byte[] in, byte[] out)  
	{
		out[0] = (byte) ((in[0] >> 1) & 0xff);
		out[1] = (byte) ((((in[0] & 0x01) << 6) | (((in[1] & 0xff)>>2) & 0xff)) & 0xff);
		out[2] = (byte) ((((in[1] & 0x03) << 5) | (((in[2] & 0xff)>>3) & 0xff)) & 0xff);
		out[3] = (byte) ((((in[2] & 0x07) << 4) | (((in[3] & 0xff)>>4) & 0xff)) & 0xff);
		out[4] = (byte) ((((in[3] & 0x0F) << 3) | (((in[4] & 0xff)>>5) & 0xff)) & 0xff);
		out[5] = (byte) ((((in[4] & 0x1F) << 2) | (((in[5] & 0xff)>>6) & 0xff)) & 0xff);
		out[6] = (byte) ((((in[5] & 0x3F) << 1) | (((in[6] & 0xff)>>7) & 0xff)) & 0xff);
		out[7] = (byte) (   in[6] & 0x7F);

		for (int i = 0; i < 8; i++) {
			out[i] = (byte) (out[i] << 1);
		}
	}

	// Set the key (convert from a long integer)
	public void setKey ( long theKey )
	{
		try 
		{
			// convert the integer to the 8 bytes required of keys
			deskeyIN[0] = (byte) (theKey        & 0xFF );
			deskeyIN[1] = (byte)((theKey >>  8) & 0xFF );
			deskeyIN[2] = (byte)((theKey >> 16) & 0xFF );
			deskeyIN[3] = (byte)((theKey >> 24) & 0xFF );
			deskeyIN[4] = (byte)((theKey >> 32) & 0xFF );
			deskeyIN[5] = (byte)((theKey >> 40) & 0xFF );
			deskeyIN[6] = (byte)((theKey >> 48) & 0xFF );

			// theKey should never be larger than 56-bits, so this should always be 0
			deskeyIN[7] = (byte)((theKey >> 56) & 0xFF );

			// turn the 56-bits into a proper 64-bit DES key
			makeDESKey(deskeyIN, deskeyOUT);

			// Create the specific key for DES
			the_key = new SecretKeySpec ( deskeyOUT, "DES" );
		}
		catch ( Exception e )
		{
			System.out.println("Failed to assign key" +  theKey +
					". Exception: " + e.toString() + ". Message: " + e.getMessage()) ;
		}
	}


	// Program to brute force DES keys.
	public static void main ( String[] args )
	{
		if ( 2 != args.length )
		{
			System.out.println ("Usage: java SealedDES #threads key_size_in_bits");
			return;
		}

		// create object to printf to the console
		PrintStream p = new PrintStream(System.out);

		// Get the arguments
		int numThreads = Integer.parseInt(args[0]);
		long keybits = Long.parseLong ( args[1] );

		long maximumKey = ~(0L);
		maximumKey = maximumKey >>> (64 - keybits);

		// Get a number between 0 and 2^64 - 1
		Random generator = new Random ();
		long key =  generator.nextLong();

		// Mask off the high bits so we get a short key
		key = key & maximumKey;
		System.out.println("Secret key: " + key);

		// Get and store the current time -- for timing
		long runstart;
		runstart = System.currentTimeMillis();
		
		// initialize an array of threads to be executed. 
		Thread[] runningThreads = new Thread[numThreads];
		
		// start the threads running.
		for (int i = 0; i < numThreads; i++) {
			runningThreads[i] = new Thread( new SealedDES(i, maximumKey * i/numThreads, maximumKey * (i+ 1)/numThreads, key, runstart));
			runningThreads[i].start();
		}
		// finish the threads.
		for (int i = 0; i < numThreads; i++) {
			try {
				runningThreads[i].join();
			} catch (InterruptedException e) {
				System.out.println("failed join on index: " + i);
				e.printStackTrace();
				return;
			}
		}

		// Output search time
		long elapsed = System.currentTimeMillis() - runstart;
		long keys = maximumKey + 1;
		//System.out.println(keybits + "," + elapsed + "," + numThreads);
		System.out.println ( "Completed search of " + keys + " keys at " + elapsed + " milliseconds.");
	}

	@Override
	public void run() {

		// Search for the right key
		for ( long i = startPoint; i < endPoint; i++ )
		{
			// Set the key and decipher the object
			deccipher.setKey ( i );
			String decryptstr = deccipher.decrypt ( sldObj );
			// Does the object contain the known plaintext
			if (( decryptstr != null ) && ( decryptstr.indexOf ( "Hopkins" ) != -1 ))
			{
				//  Remote printlns if running for time.
				System.out.println("Thread: " + this.threadID + " found decrypt key: " + i + " producing message: " + decryptstr);
			}
			
			// Update progress every once in awhile.
			//  Remote printlns if running for time.
			if ( i % 100000 == 0 )
			{ 
				long elapsed = System.currentTimeMillis() - runStart;
				System.out.println ( "Thread: " + this.threadID + " searched key number " + i + " at " + elapsed + " milliseconds.");
			}
			
		}

	}
}

