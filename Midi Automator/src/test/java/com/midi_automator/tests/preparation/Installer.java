package com.midi_automator.tests.preparation;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Region;

import com.midi_automator.tests.IntegrationTestCase;
import com.midi_automator.tests.utils.SikuliXAutomations;
import com.midi_automator.tests.utils.SikuliAutomation;

/**
 * Tests the installer and prepares the integration tests.
 * 
 * @author aguelle
 *
 */
public class Installer extends IntegrationTestCase {

	@Test
	public void midiOpenerShouldBeInstalled() {
		try {
			SikuliXAutomations.openMidiAutomatorInstaller();
			Region match = SikuliAutomation.getSearchRegion();

			// MacOS
			if (System.getProperty("os.name").equals("Mac OS X")) {

				match.dragDrop(screenshotpath + "Midi_automator_app_icon.png",
						screenshotpath + "Applications_icon.png");
				SikuliAutomation.setSearchRegion(screen);
				match = SikuliXAutomations.findMultipleStateRegion(MAX_TIMEOUT,
						"ersetzen_button.png");
				match.click();
				Thread.sleep(6000);
			}

			// Windows
			if (System.getProperty("os.name").equals("Windows 7")) {
				match = SikuliXAutomations.findMultipleStateRegion(DEFAULT_TIMEOUT,
						"NSIS_install_button_active.png",
						"NSIS_install_button.png");
				match.click();

				// wait for installer to finish
				boolean foundCloseButtonActive = false;
				while (!foundCloseButtonActive) {
					try {
						SikuliXAutomations.findMultipleStateRegion(MAX_TIMEOUT,
								"NSIS_close_button_active.png");
						foundCloseButtonActive = true;
					} catch (FindFailed e) {
					}
				}
			}

			SikuliXAutomations.closeMidiAutomatorInstaller();
		} catch (IOException | FindFailed | InterruptedException e) {
			fail(e.toString());
		}
	}
}
