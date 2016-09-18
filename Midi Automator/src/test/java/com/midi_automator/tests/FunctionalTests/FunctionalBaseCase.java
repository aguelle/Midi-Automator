package com.midi_automator.tests.FunctionalTests;

import static com.midi_automator.tests.utils.GUIAutomations.getInfoLabelText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.sikuli.script.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.midi_automator.AppConfig;
import com.midi_automator.MidiAutomator;
import com.midi_automator.presenter.Messages;
import com.midi_automator.presenter.Presenter;
import com.midi_automator.tests.utils.GUIAutomations;
import com.midi_automator.tests.utils.MockUpUtils;
import com.midi_automator.tests.utils.SikuliXGUIAutomations;
import com.midi_automator.view.frames.MainFrame;

public class FunctionalBaseCase extends AssertJSwingJUnitTestCase {

	@Rule
	public TestName name = new TestName();

	protected FrameFixture window;
	protected String currentPath;
	protected SikuliXGUIAutomations sikulix;
	protected AnnotationConfigApplicationContext ctx;

	@Autowired
	private Presenter presenter;

	@Before
	public void log() {
		System.setProperty("user.language", "en");
		System.out.println();
		System.out.println("Running Test: " + this.getClass().getSimpleName()
				+ " - " + name.getMethodName());
		System.out
				.println("====================================================");
	}

	/**
	 * Starts the application
	 */
	protected void startApplication() {

		MidiAutomator.test = true;
		MainFrame mainFrame = GuiActionRunner
				.execute(new GuiQuery<MainFrame>() {
					protected MainFrame executeInEDT() {

						if (ctx == null) {
							ctx = new AnnotationConfigApplicationContext(
									AppConfig.class);
						}

						presenter = (Presenter) ctx.getBean(Presenter.class);

						return presenter.openMainFrame();
					}
				});

		window = new FrameFixture(robot(), mainFrame);

		GUIAutomations.window = window;
		GUIAutomations.robot = robot();

		try {
			currentPath = new File(".").getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Checks for the open entry text
	 * 
	 * @param entryName
	 *            The name of the entry
	 */
	protected void checkIfOpenEntryIsDisplayed(String entryName) {
		checkInfoText(String.format(Messages.MSG_OPENING_ENTRY, entryName));
	}

	/**
	 * Checks that the open entry text for the entry is not displayed
	 * 
	 * @param entryName
	 *            The name of the entry
	 */
	protected void checkIfOpenEntryIsNotDisplayed(String entryName) {
		checkNotInfoText(String.format(Messages.MSG_OPENING_ENTRY, entryName));
	}

	/**
	 * Checks for a specific text in the info
	 * 
	 * @param text
	 *            The text to check for
	 */
	protected void checkInfoText(String text) {
		String infoMsg = text.replace("\"", "&quot;").replace("Ä", "&#196;")
				.replace("Ö", "&#214;").replace("Ü", "&#220;");
		assertThat(getInfoLabelText(), containsString(infoMsg));
	}

	/**
	 * Checks for that a specific text is not in the info
	 * 
	 * @param text
	 *            The text to check for
	 */
	protected void checkNotInfoText(String text) {
		String infoMsg = text.replace("\"", "&quot;").replace("Ä", "&#196;")
				.replace("Ö", "&#214;").replace("Ü", "&#220;");
		assertThat(getInfoLabelText(), not(containsString(infoMsg)));
	}

	@After
	public void sleep() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onSetUp() {
		sikulix = new SikuliXGUIAutomations(new Screen(0));
	}

	@Override
	protected void onTearDown() {
		MockUpUtils.recoverMidoBackup();
		MockUpUtils.recoverPropertiesBackup();

		if (presenter != null) {
			presenter.close();
		}

		if (ctx != null) {
			ctx.close();
		}
	}

	/**
	 * Checks that the info text is empty
	 */
	protected void checkEmptyInfoText() {
		assertEquals("<html> <head> </head> <body> </body> </html> ",
				getInfoLabelText());
	}
}
