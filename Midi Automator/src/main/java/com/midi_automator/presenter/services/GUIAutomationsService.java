package com.midi_automator.presenter.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.midi.MidiMessage;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.midi_automator.guiautomator.GUIAutomation;
import com.midi_automator.guiautomator.GUIAutomator;
import com.midi_automator.model.MidiAutomatorProperties;
import com.midi_automator.model.MidiAutomatorProperties.GUIAutomationKey;
import com.midi_automator.presenter.Presenter;
import com.midi_automator.utils.MidiUtils;

/**
 * Handles all GUI automations.
 * 
 * @author aguelle
 *
 */
@Service
public class GUIAutomationsService {

	private Logger log = Logger.getLogger(this.getClass().getName());

	@Autowired
	private MidiAutomatorProperties properties;

	@Autowired
	private Presenter presenter;

	@Autowired
	private MidiService midiService;

	private GUIAutomation[] guiAutomations;
	private List<GUIAutomator> guiAutomators = new ArrayList<GUIAutomator>();;

	/**
	 * Loads all GUI automations from the properties.
	 */
	public void loadProperties() {

		// initiate array with GUI automations
		Set<Entry<Object, Object>> guiAutomationProperties = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_IMAGE.toString());

		guiAutomations = new GUIAutomation[guiAutomationProperties.size()];

		for (int i = 0; i < guiAutomations.length; i++) {
			guiAutomations[i] = new GUIAutomation();
		}

		loadAutomationImageProperties();
		loadAutomationTypeProperties();
		loadAutomationTriggerProperties();
		loadAutomationMinDelayProperties();
		loadAutomationTimeoutProperties();
		loadAutomationMidiSignatureProperties();
		loadAutomationMinSimilarityProperties();
		loadAutomationScanRateProperties();
		loadAutomationIsMovableProperites();

		stopGUIAutomations();
		startGuiAutomations();
	}

	/**
	 * Starts all GUI automations
	 */
	private void startGuiAutomations() {

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < guiAutomations.length; i++) {

					GUIAutomator guiAutomator = new GUIAutomator();
					guiAutomator.setName("GUIAutomator " + i);
					guiAutomator.setGUIAutomation(guiAutomations[i]);
					guiAutomators.add(guiAutomator);
					guiAutomator.start();
				}
				return null;
			}
		};
		worker.execute();
	}

	/**
	 * Loads all midi signatures.
	 */
	private void loadAutomationMidiSignatureProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_MIDI_SIGNATURE
						.toString());
		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			String value = (String) property.getValue();

			if (value.equals(MidiAutomatorProperties.VALUE_NULL)) {
				value = null;
			}

			guiAutomations[index].setMidiSignature(value);
		}
	}

	/**
	 * Loads all movable options.
	 */
	private void loadAutomationIsMovableProperites() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_IS_MOVABLE.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			boolean isMovable = Boolean.valueOf((String) property.getValue());
			guiAutomations[index].setMovable(isMovable);

		}
	}

	/**
	 * Loads all minimum similarities.
	 */
	private void loadAutomationMinSimilarityProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_MIN_SIMILARITY
						.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			float value = Float.valueOf((String) property.getValue());
			guiAutomations[index].setMinSimilarity(value);

		}
	}

	/**
	 * Loads all scan rates.
	 */
	private void loadAutomationScanRateProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_SCAN_RATE.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			float value = Float.valueOf((String) property.getValue());
			guiAutomations[index].setScanRate(value);

		}
	}

	/**
	 * Loads all time outs.
	 */
	private void loadAutomationTimeoutProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_TIMEOUT.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			long timeout = Long.valueOf((String) property.getValue());
			guiAutomations[index].setTimeout(timeout);

		}
	}

	/**
	 * Loads all minimum delays.
	 */
	private void loadAutomationMinDelayProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_MIN_DELAY.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			long minDelay = Long.valueOf((String) property.getValue());
			guiAutomations[index].setMinDelay(minDelay);

		}
	}

	/**
	 * Loads all triggers.
	 */
	private void loadAutomationTriggerProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_TRIGGER.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			String trigger = (String) property.getValue();
			guiAutomations[index].setTrigger(trigger);

			if (trigger.contains(GUIAutomation.CLICKTRIGGER_MIDI)) {

				String midiDeviceKey = MidiAutomatorProperties.KEY_MIDI_IN_AUTOMATION_TRIGGER_DEVICE
						+ "_" + index;

				String midiDeviceName = trigger.replace(
						GUIAutomation.CLICKTRIGGER_MIDI, "");

				midiService.loadMidiDeviceByFunctionKey(midiDeviceKey,
						midiDeviceName);
			}

		}
	}

	/**
	 * Loads all types.
	 */
	private void loadAutomationTypeProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_TYPE.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			guiAutomations[index].setType((String) property.getValue());

		}
	}

	/**
	 * Loads all images.
	 */
	private void loadAutomationImageProperties() {

		Set<Entry<Object, Object>> propertiesSet = properties
				.entrySet(GUIAutomationKey.GUI_AUTOMATION_IMAGE.toString());

		for (Entry<Object, Object> property : propertiesSet) {

			int index = MidiAutomatorProperties
					.getIndexOfPropertyKey((String) property.getKey());
			guiAutomations[index].setImagePath((String) property.getValue());

		}
	}

	/**
	 * Terminates all GUI automations
	 */
	public void stopGUIAutomations() {

		for (int i = 0; i < guiAutomators.size(); i++) {
			GUIAutomator guiAutomator = guiAutomators.get(i);
			guiAutomator.setActive(false);
			guiAutomator.terminate();

			log.debug("Terminate GUI automation: "
					+ guiAutomator.getGuiAutomation());
		}
		guiAutomators.clear();
	}

	/**
	 * Activates all GUI automations that listen to the given message
	 * 
	 * @param message
	 *            The midi message
	 */
	public void activateAutomationsByMidiMessage(MidiMessage message) {

		String signature = MidiUtils.messageToString(message);

		// activate midi automations
		for (GUIAutomator guiAutomator : guiAutomators) {
			guiAutomator.activateMidiAutomations(signature);
		}

	}

	/**
	 * Sets all GUI automations.
	 * 
	 * @param guiAutomations
	 *            The GUIAutomations to store
	 */
	public void saveGUIAutomations(GUIAutomation[] guiAutomations) {

		removeGUIAutomations();
		this.guiAutomations = guiAutomations;

		for (int index = 0; index < guiAutomations.length; index++) {

			saveToProperties(GUIAutomationKey.GUI_AUTOMATION_IMAGE.toString(),
					index, guiAutomations[index].getImagePath());

			saveToProperties(GUIAutomationKey.GUI_AUTOMATION_TYPE.toString(),
					index, guiAutomations[index].getType());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_TRIGGER.toString(), index,
					guiAutomations[index].getTrigger());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_MIN_DELAY.toString(),
					index, guiAutomations[index].getMinDelay());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_TIMEOUT.toString(), index,
					guiAutomations[index].getTimeout());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_MIDI_SIGNATURE.toString(),
					index, guiAutomations[index].getMidiSignature());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_MIN_SIMILARITY.toString(),
					index, guiAutomations[index].getMinSimilarity());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_SCAN_RATE.toString(),
					index, guiAutomations[index].getScanRate());

			saveToProperties(
					GUIAutomationKey.GUI_AUTOMATION_IS_MOVABLE.toString(),
					index, guiAutomations[index].isMovable());
		}
	}

	/**
	 * Saves an automation property to the the properties file.
	 * 
	 * @param key
	 *            The key of the property
	 * @param index
	 *            The index of the automation
	 * @param value
	 *            The value of the property
	 */
	private void saveToProperties(String key, int index, Object value) {

		String strValue = String.valueOf(value);

		if (strValue.equals("null") || strValue.equals("")) {
			strValue = MidiAutomatorProperties.VALUE_NULL;
		}

		properties.setProperty(key + MidiAutomatorProperties.INDEX_SEPARATOR
				+ index, strValue);
		presenter.storePropertiesFile();
	}

	/**
	 * Removes all GUI automations.
	 */
	public void removeGUIAutomations() {

		guiAutomations = null;

		for (GUIAutomationKey key : GUIAutomationKey.values()) {
			properties.removeKeys(key.toString());
		}

		presenter.storePropertiesFile();
	}

	/**
	 * Activate per change triggered automations
	 */
	public void activateAllOncePerChangeAutomations() {
		for (GUIAutomator guiAutomator : guiAutomators) {
			guiAutomator.activateOncePerChangeAutomations();
		}
	}

	/**
	 * De-/Activates the GUI automators.
	 * 
	 * @param active
	 *            <TRUE> activate GUI automation, <FALSE> deactivate GUI
	 *            automation
	 */
	public void setGUIAutomatorsToActive(boolean active) {
		for (GUIAutomator guiAutomator : guiAutomators) {
			guiAutomator.setActive(active);
		}
	}

	public GUIAutomation[] getGuiAutomations() {
		return guiAutomations;
	}

}