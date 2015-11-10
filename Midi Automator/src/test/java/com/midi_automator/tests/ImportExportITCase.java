package com.midi_automator.tests;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.sikuli.script.FindFailed;

import com.midi_automator.tests.utils.SikuliXAutomations;
import com.midi_automator.tests.utils.MockUpUtils;

public class ImportExportITCase extends IntegrationTestCase {

	@Test
	public void helloWorldMidoFileShouldBeImported() {

		try {
			// mockup
			MockUpUtils.setMockupMidoFile("mockups/empty.mido");
			SikuliXAutomations.openMidiAutomator();

			SikuliXAutomations.openImport();

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
