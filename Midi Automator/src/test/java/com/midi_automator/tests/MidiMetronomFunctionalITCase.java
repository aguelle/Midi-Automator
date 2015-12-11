package com.midi_automator.tests;

import static com.midi_automator.tests.utils.GUIAutomations.*;

import java.awt.Color;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import org.assertj.swing.fixture.FrameFixture;
import org.junit.Test;

import com.midi_automator.tests.utils.MockUpUtils;
import com.midi_automator.utils.MidiUtils;

public class MidiMetronomFunctionalITCase extends GUITestCase {

	private String deviceName;
	private int messageType = ShortMessage.NOTE_ON;
	private int channel = 16;
	private String value1stClick = "A";
	private String valueClick = "E";
	private int octave = 4;
	private int velocity = 127;

	public MidiMetronomFunctionalITCase() {
		if (System.getProperty("os.name").equals("Mac OS X")) {
			deviceName = "Bus 1";
		}

		if (System.getProperty("os.name").contains("Windows")) {
			deviceName = "LoopBe Internal MIDI";
		}
	}

	@Test
	public void nothing() {

	}

	// TODO: EDT problem?
	// @Test
	public void metronomFirstClickShouldBeShown() {

		try {

			MockUpUtils.setMockupMidoFile("mockups/empty.mido");
			MockUpUtils.setMockupPropertiesFile("mockups/empty.properties");
			startApplication();

			FrameFixture preferencesFrame = openPreferences();
			setMidiInMetronomDevice(deviceName, preferencesFrame);
			saveDialog(preferencesFrame);

			Thread.sleep(1000);

			MidiUtils.sendMidiMessage(deviceName, messageType, channel,
					value1stClick, octave, velocity);

			getFileList().background().requireEqualTo(Color.RED);

		} catch (InvalidMidiDataException | MidiUnavailableException
				| InterruptedException e) {
			e.printStackTrace();
		}
	}

	// TODO: EDT problem?
	// @Test
	public void metronomOtherClickShouldBeShown() {

		try {

			MockUpUtils.setMockupMidoFile("mockups/empty.mido");
			MockUpUtils.setMockupPropertiesFile("mockups/empty.properties");
			startApplication();

			FrameFixture preferencesFrame = openPreferences();
			setMidiInMetronomDevice(deviceName, preferencesFrame);
			saveDialog(preferencesFrame);

			Thread.sleep(1000);

			MidiUtils.sendMidiMessage(deviceName, messageType, channel,
					valueClick, octave, velocity);

			getFileList().background().requireEqualTo(Color.RED);

		} catch (InvalidMidiDataException | MidiUnavailableException
				| InterruptedException e) {
			e.printStackTrace();
		}
	}
}