package com.midi_automator.tests;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import org.junit.Test;
import org.sikuli.script.FindFailed;

import com.midi_automator.tests.utils.SikuliXAutomations;
import com.midi_automator.tests.utils.MockUpUtils;
import com.midi_automator.utils.MidiUtils;

public class MidiRemoteOutITCase extends IntegrationTestCase {

	private String deviceName;
	private MidiDevice device;
	private Receiver receiver;
	private String deviceScreenshot;
	private String receivedSignature;
	private int remoteTimeout = 3000;

	public MidiRemoteOutITCase() {
		if (System.getProperty("os.name").equals("Mac OS X")) {
			deviceName = "Bus 1";
			deviceScreenshot = "Bus_1.png";
		}

		if (System.getProperty("os.name").equals("Windows 7")) {
			deviceName = "LoopBe Internal MIDI";
			deviceScreenshot = "LoopBe_Internal_MIDI.png";
		}

		try {
			device = MidiUtils.getMidiDevice(deviceName, "IN");
			device.open();
			receiver = new MidiINReceiver();
			MidiUtils.setReceiverToDevice(device, receiver);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void masterOutMessageShouldBeSent() {

		try {
			// mockup
			MockUpUtils.setMockupMidoFile("mockups/Hello_World_12_empty.mido");

			SikuliXAutomations.openMidiAutomator();
			SikuliXAutomations.setAndSavePreferencesComboBox(
					"combo_box_midi_master_out.png", deviceScreenshot);
			SikuliXAutomations.openEntryByDoubleClick(
					"Hello_World_1_entry_active.png",
					"Hello_World_1_entry_inactive.png",
					"Hello_World_1_entry.png");

			// check if midi master message was sent
			Thread.sleep(remoteTimeout);
			if (!"channel 1: CONTROL CHANGE 102 value: 0"
					.equals(receivedSignature)) {
				fail(receivedSignature
						+ " is wrong master signature for index 0.");
			}

			SikuliXAutomations.openEntryByDoubleClick(
					"Hello_World_2_entry_active.png",
					"Hello_World_2_entry_inactive.png",
					"Hello_World_2_entry.png");

			// check if midi master message was sent
			Thread.sleep(remoteTimeout);
			if (!"channel 1: CONTROL CHANGE 102 value: 1"
					.equals(receivedSignature)) {
				fail(receivedSignature
						+ " is wrong master signature for index 1.");
			}

		} catch (FindFailed | IOException | InterruptedException e) {
			fail(e.toString());
		} finally {
			try {
				SikuliXAutomations.closeMidiAutomator();
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	class MidiINReceiver implements Receiver {

		@Override
		public void send(MidiMessage message, long timeStamp) {

			receivedSignature = MidiUtils.messageToString(message);
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
		}
	}
}
