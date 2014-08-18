
package com.sponsorpay.sdk.android.testapp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.text.format.Time;

import com.sponsorpay.utils.SPLoggerListener;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.SponsorPayLogger.Level;

public class TelnetLogger implements Runnable, SPLoggerListener{

	private static final String TAG = "TelnetLogger"; 
	
	private Socket mSocket;
	private PrintWriter mPrintWriter;
	private BufferedReader mBufferedReader;
	private ServerSocket mSocketServer;
	
	private Time mTime = new Time();
	
	@Override
	public void run() {
		createServer();
	}
	
	private void createServer() {
		try {
			mSocketServer = new ServerSocket();
			mSocketServer.setReuseAddress(false);
			mSocketServer.bind(new InetSocketAddress(8088));
			mSocket = mSocketServer.accept();

			SponsorPayLogger.i(TAG, "Client Connected");

			mPrintWriter = new PrintWriter(mSocket.getOutputStream(), true);
			mPrintWriter.println("\nUse \"exit\" to close the connection\n");

			mBufferedReader = new BufferedReader(new InputStreamReader(
					mSocket.getInputStream()));

			while (!mBufferedReader.readLine().equals("exit")) {
				// do nothing, just waiting for the proper exit command
			}
			SponsorPayLogger.i(TAG, "Exiting the logger");
			closeServer();
		} catch (Exception e) {
			SponsorPayLogger.e(TAG, "Error with the server", e);
		}
	}

	private void closeServer() {
		try {
			if (mSocket != null) {
				mSocket.shutdownInput();
				mSocket.close();
			}
			mSocketServer.close();
			mSocket = null;
			mSocketServer = null;
			if (mPrintWriter != null) {
				mPrintWriter.close();
				mPrintWriter = null;
				mBufferedReader.close(); 
			}
			SponsorPayLogger.i(TAG, "Server closed");
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, "error", e);
			mSocket = null;
			mSocketServer = null;
		}
	}
	
	@Override
	public void log(Level level, String tag, String message, Exception exception) {
		if (mPrintWriter != null) {
			mTime.setToNow();
			
			String text = mTime.format2445()
					+ " "
					+ level.name()
					+ " ["
					+ tag
					+ "]  - "
					+ message
					+ (exception != null ? " - Exception: "
							+ exception.getLocalizedMessage() : "");

			mPrintWriter.println(text);
		}
	}

}