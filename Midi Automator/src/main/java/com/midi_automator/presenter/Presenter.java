package com.midi_automator.presenter;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.MidiUnavailableException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.midi_automator.Messages;
import com.midi_automator.MidiAutomator;
import com.midi_automator.Resources;
import com.midi_automator.model.MidiAutomatorProperties;
import com.midi_automator.model.Model;
import com.midi_automator.presenter.services.ItemListService;
import com.midi_automator.presenter.services.GUIAutomationsService;
import com.midi_automator.presenter.services.InfoMessagesService;
import com.midi_automator.presenter.services.MidiItemChangeNotificationService;
import com.midi_automator.presenter.services.MidiMetronomService;
import com.midi_automator.presenter.services.MidiExecuteService;
import com.midi_automator.presenter.services.MidiService;
import com.midi_automator.view.windows.MainFrame.MainFrame;

@Controller
public class Presenter {

	static Logger log = Logger.getLogger(Presenter.class.getName());

	@Autowired
	private Resources resources;
	@Autowired
	private MidiAutomatorProperties properties;
	@Autowired
	private Model model;

	@Autowired
	private MainFrame mainFrame;

	@Autowired
	private MidiService midiService;
	@Autowired
	private GUIAutomationsService guiAutomationsService;
	@Autowired
	private ItemListService fileListService;
	@Autowired
	private MidiExecuteService midiRemoteOpenService;
	@Autowired
	private MidiMetronomService midiMetronomService;
	@Autowired
	private MidiItemChangeNotificationService midiNotificationService;
	@Autowired
	private InfoMessagesService infoMessagesService;

	public Presenter() {
	}

	/**
	 * Opens the main program frame.
	 */
	public MainFrame openMainFrame() {

		String fileName = model.getPersistenceFileName();

		String errMidoFileNotFound = String.format(
				Messages.MSG_FILE_LIST_NOT_FOUND, fileName);
		String errMidoFileNotReadable = String.format(
				Messages.MSG_FILE_LIST_NOT_READABLE, fileName);
		String errMidoFileIsTooBig = String.format(
				Messages.MSG_FILE_LIST_TOO_BIG, fileName);

		Messages.put(Messages.KEY_ERROR_MIDO_FILE_NOT_FOUND,
				errMidoFileNotFound);
		Messages.put(Messages.KEY_ERROR_ITEM_FILE_IO, errMidoFileNotReadable);
		Messages.put(Messages.KEY_ERROR_MIDO_FILE_TOO_BIG, errMidoFileIsTooBig);

		mainFrame.init();

		loadProperties();
		fileListService.reloadSetList();

		return mainFrame;
	}

	/**
	 * Loads the properties file.
	 * 
	 * @throws MidiUnavailableException
	 */
	public void loadProperties() {

		loadPropertiesFile();
		midiRemoteOpenService.loadProperties();
		midiMetronomService.loadProperties();
		midiNotificationService.loadProperties();
		fileListService.loadProperties();
		guiAutomationsService.loadProperties();
		mainFrame.reload();
	}

	/**
	 * Resets all loaded automation and indexes.
	 */
	public void reset() {
		midiService.unloadAllMidiDevices();
		guiAutomationsService.stopGUIAutomations();
		fileListService.resetCurrentIndex();
	}

	/**
	 * Closes the application
	 */
	public void close() {
		mainFrame.setExiting(true);
		reset();
		disposeAllFrames();
	}

	/**
	 * Find the active frame before creating and dispatching the event
	 */
	private void disposeAllFrames() {
		for (Frame frame : Frame.getFrames()) {
			if (frame.isActive()) {
				WindowEvent windowClosing = new WindowEvent(frame,
						WindowEvent.WINDOW_CLOSING);
				frame.dispatchEvent(windowClosing);
			}
		}
	}

	/**
	 * Returns if the application is in test mode
	 * 
	 * @return <TRUE> if the application is in development mode, <FALSE> if the
	 *         application is not in development mode
	 */
	public boolean isInTestMode() {
		return MidiAutomator.test;
	}

	/**
	 * Loads the properties file.
	 */
	private void loadPropertiesFile() {

		try {

			infoMessagesService.removeInfoMessage(Messages
					.get(Messages.KEY_ERROR_PROPERTIES_FILE_NOT_FOUND));
			infoMessagesService.removeInfoMessage(Messages
					.get(Messages.KEY_ERROR_PROPERTIES_FILE_NOT_READABLE));

			properties.load();
			properties.migrate();
			properties.load();

		} catch (FileNotFoundException e) {

			String error = String.format(Messages.MSG_FILE_NOT_FOUND,
					properties.getPropertiesFilePath());
			infoMessagesService.setInfoMessage(
					Messages.KEY_ERROR_PROPERTIES_FILE_NOT_FOUND, error);

		} catch (IOException e) {

			String error = String.format(Messages.MSG_FILE_COULD_NOT_BE_OPENED,
					properties.getPropertiesFilePath());
			infoMessagesService.setInfoMessage(
					Messages.KEY_ERROR_PROPERTIES_FILE_NOT_READABLE, error);
		}
	}

	/**
	 * Tries to store the properties file.
	 */
	public void storePropertiesFile() {

		try {
			infoMessagesService.removeInfoMessage(Messages
					.get(Messages.KEY_ERROR_PROPERTIES_FILE_NOT_READABLE));

			properties.store();

		} catch (IOException e) {

			String error = String.format(Messages.MSG_FILE_COULD_NOT_BE_OPENED,
					properties.getPropertiesFilePath());
			infoMessagesService.setInfoMessage(
					Messages.KEY_ERROR_PROPERTIES_FILE_NOT_READABLE, error);
		}
	}
}
