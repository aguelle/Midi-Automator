package com.midi_automator.tests;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.sikuli.script.FindFailed;

import com.midi_automator.tests.utils.SikuliXAutomations;
import com.midi_automator.tests.utils.MockUpUtils;

public class DeleteFileITCase extends IntegrationTestCase {

	@Test
	public void deleteMenuShouldBeDisabledIfListIsEmpty() {

		try {
			SikuliXAutomations.openMidiAutomator();
			SikuliXAutomations.openPopupMenu("midi_automator.png", null, null,
					LOW_SIMILARITY);

			// check for disabled delete menu entry
			SikuliXAutomations.checkResult("delete_inactive.png");

		} catch (FindFailed | IOException e) {
			fail(e.toString());
		} finally {
			try {
				SikuliXAutomations.closeMidiAutomator();
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void helloWorldEntryShouldBeDeleted() {

		try {
			// mockup
			MockUpUtils.setMockupMidoFile("mockups/Hello_World.mido");
			SikuliXAutomations.openMidiAutomator();

			// delete entry
			SikuliXAutomations.deleteEntry("Hello_World_entry.png",
					"Hello_World_entry_active.png",
					"Hello_World_entry_inactive.png");

			// check if entry was deleted
			try {
				SikuliXAutomations.findMultipleStateRegion(MIN_TIMEOUT,
						"Hello_World_entry.png",
						"Hello_World_entry_active.png",
						"Hello_World_entry_inactive.png");
				fail("Hello World entry still found.");
			} catch (FindFailed e) {
			}

			// check if deletion was saved
			SikuliXAutomations.closeMidiAutomator();
			SikuliXAutomations.openMidiAutomator();
			SikuliXAutomations.checkResult("midi_automator.png");

		} catch (FindFailed | IOException e) {
			fail(e.toString());
		} finally {
			try {
				SikuliXAutomations.closeMidiAutomator();
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}
}
