package net.codejava.sound;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.scene.Cursor;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * A Sound Recorder program in Java Swing.
 * @author www.codejava.net
 *
 */
public class Controller {
	@FXML
	private Button playButton;
	@FXML
	private Button recordButton;
	@FXML
	private Label timeLabel;

	private SoundRecordingUtil recorder = new SoundRecordingUtil();
	private AudioPlayer player = new AudioPlayer();
	private Thread playbackThread;
	private Thread recordThread;
	private RecordTimer timer;

	private boolean isRecording = false;
	private boolean isPlaying = false;

	private String saveFilePath;

	// Icons used for buttons
	private Image iconRecord = new Image("/net/codejava/sound/images/Record.gif");
	private Image iconStop = new Image("/net/codejava/sound/images/Stop.gif");
	private Image iconPlay = new Image("/net/codejava/sound/images/Play.gif");

	@FXML
	void initialize() {
		playButton.setDisable(true);
		playButton.setGraphic(new ImageView(iconPlay));
		recordButton.setGraphic(new ImageView(iconRecord));
	}

	/**
	 * Handle click events on the buttons.
	 */
	@FXML
	public void onButtonClicked(ActionEvent event) {
		Button button = (Button) event.getSource();
		if (button == recordButton) {
			if (!isRecording) {
				startRecording();
			} else {
				stopRecording();
			}
		} else if (button == playButton) {
			if (!isPlaying) {
				playBack();
			} else {
				stopPlaying();
			}
		}
	}

	/**
	 * Start recording sound, the time will count up.
	 */
	private void startRecording() {
		recordThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					isRecording = true;
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							recordButton.setText("Stop");
							recordButton.setGraphic(new ImageView(iconStop));
							playButton.setDisable(true);
//							String s = Platform.isFxApplicationThread() ? "UI thread" : "Background thread";
//							System.out.println("Start of recording: I am updating the buttons on the: " + s);
						}
					});

					recorder.start();

				} catch (LineUnavailableException ex) {
					Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Could not start recording sound!");
					alert.showAndWait();
					ex.printStackTrace();
				}
			}
		});
		recordThread.start();
		timer = new RecordTimer(timeLabel);
		timer.start();
	}

	/**
	 * Stop recording and save the sound into a WAV file
	 */
	private void stopRecording() {
		isRecording = false;
		try {
			timer.cancel();
			recordButton.setText("Record");
			recordButton.setGraphic(new ImageView(iconRecord));

			Scene scene = recordButton.getScene();
			scene.setCursor(Cursor.WAIT);

			recorder.stop();

			scene.setCursor(Cursor.DEFAULT);

			saveFile();

		} catch (IOException ex) {
			Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Could not stop recording sound!");
			alert.showAndWait();
			ex.printStackTrace();
		}
	}

	/**
	 * Start playing back the sound.
	 */
	private void playBack() {
		timer = new RecordTimer(timeLabel);
		timer.start();
		isPlaying = true;
		System.out.println("File path name: " + saveFilePath);
		playbackThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							playButton.setText("Stop");
							playButton.setGraphic(new ImageView(iconStop));
							recordButton.setDisable(true);
//							String s = Platform.isFxApplicationThread() ? "UI thread" : "Background thread";
//							System.out.println("Playback: I am updating playButton to Stop on the: " + s);
						}
					});
//					System.out.println("Attempting to play file: " + saveFilePath);
					player.play(saveFilePath);
//					System.out.println("Resetting timer...");
					timer.reset();

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							playButton.setText("Play");
							playButton.setGraphic(new ImageView(iconPlay));
							recordButton.setDisable(false);
//							String s = Platform.isFxApplicationThread() ? "UI thread" : "Background thread";
//							System.out.println("Playback: I am updating playButton to Play on the: " + s);
						}
					});

					isPlaying = false;

				} catch (UnsupportedAudioFileException ex) {
					ex.printStackTrace();
				} catch (LineUnavailableException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}
		});

		playbackThread.start();
	}

	/**
	 * Stop playing back.
	 */
	private void stopPlaying() {
		timer.reset();
		timer.interrupt();
		player.stop();
		playbackThread.interrupt();
	}

	/**
	 * Save the recorded sound into a WAV file.
	 */
	private void saveFile() {
		// Can use file chooser
		FileChooser fileChooser1 = new FileChooser();
		fileChooser1.setTitle("Save Recording");
		File file = fileChooser1.showSaveDialog(Main.getPrimaryStage());
		System.out.println(file);

		saveFilePath = file.getPath();
		if (!saveFilePath.toLowerCase().endsWith(".wav")) {
			saveFilePath += ".wav";
		}

		File wavFile = new File(saveFilePath);

		// or just save based on date and time
//		String date =  new SimpleDateFormat("hh#mm#ss_MM-dd-yyyy").format(new Date());
//		String filename = "test-" + date + ".wav ";
//		String workingDirectory = System.getProperty("user.dir");
//		saveFilePath = "";
//		saveFilePath = workingDirectory + File.separator + "recordings" + File.separator + filename;
//		System.out.println("Final filepath : " + saveFilePath);
//		File wavFile = new File(saveFilePath);

		try {
			recorder.save(wavFile);

			Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Saved recorded sound to: " + saveFilePath);
			alert.showAndWait();

			playButton.setDisable(false);

		} catch (IOException ex) {
			Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Could not save to sound file!");
			alert.showAndWait();
			ex.printStackTrace();
		}

	}

}