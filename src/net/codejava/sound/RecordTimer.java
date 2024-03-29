package net.codejava.sound;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * This class counts record time/play time in the form of HH:mm:ss
 * @author www.codejava.net
 *
 */
public class RecordTimer extends Thread {
	private DateFormat dateFormater = new SimpleDateFormat("HH:mm:ss");
	private boolean isRunning = false;
	private boolean isReset = false;
	private long startTime;
	private Label labelRecordTime;
	
	RecordTimer(Label labelRecordTime) {
		this.labelRecordTime = labelRecordTime;
	}
	
	public void run() {
		isRunning = true;
		
		startTime = System.currentTimeMillis();
		
		while (isRunning) {
			try {
				Thread.sleep(250);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						labelRecordTime.setText("Record Time: " + toTimeString());
//						String s = Platform.isFxApplicationThread() ? "UI thread" : "Background thread";
//						System.out.println("I am updating the label on the: " + s);
					}
				});

			} catch (InterruptedException ex) {
				ex.printStackTrace();
				if (isReset) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							labelRecordTime.setText("Record Time: 00:00:00");
//							String s = Platform.isFxApplicationThread() ? "UI thread" : "Background thread";
//							System.out.println("I am resetting the label on the: " + s);
						}
					});

					isRunning = false;		
					break;
				}
			}
		}
	}
	
	/**
	 * Cancel counting record/play time.
	 */
	void cancel() {
		isRunning = false;		
	}
	
	/**
	 * Reset counting to "00:00:00"
	 */
	void reset() {
		isReset = true;
		isRunning = false;
	}
	
	/**
	 * Generate a String for time counter in the format of "HH:mm:ss"
	 * @return the time counter
	 */
	private String toTimeString() {
		long now = System.currentTimeMillis();
		Date current = new Date(now - startTime);
		dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		String timeCounter = dateFormater.format(current);
		return timeCounter;
	}
}