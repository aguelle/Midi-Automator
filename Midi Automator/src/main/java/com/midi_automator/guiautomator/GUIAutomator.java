package com.midi_automator.guiautomator;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;

import org.apache.log4j.Logger;
import org.sikuli.basics.Settings;
import org.sikuli.script.Match;
import org.sikuli.script.ObserveEvent;
import org.sikuli.script.ObserverCallBack;
import org.sikuli.script.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.midi_automator.IDeActivateable;
import com.midi_automator.Resources;
import com.midi_automator.model.MidiAutomatorProperties;
import com.midi_automator.presenter.services.GUIAutomationsService;
import com.midi_automator.utils.ShellRunner;
import com.midi_automator.utils.SystemUtils;

/**
 * 
 * @author aguelle
 * 
 *         This class automates GUI interactions.
 */
@Component
@Scope("prototype")
public class GUIAutomator extends Thread implements IDeActivateable {

	static Logger log = Logger.getLogger(GUIAutomator.class.getName());

	private final float MOVE_MOUSE_DELAY = 0;
	private final boolean CHECK_LAST_SEEN = true;
	private final double SIKULIX_TIMEOUT = 10;

	private volatile boolean running = true;
	private boolean active = true;

	private GUIAutomation guiAutomation;
	private MinSimColoredScreen searchRegion = new MinSimColoredScreen();
	private Match match;
	private ObserverCallBack observerCallBack = new AppearingMatchObserver();
	private boolean fixedSearchRegion = false;

	@Autowired
	private Resources resources;

	/**
	 * Initializes the automator
	 * 
	 * @param minSimilarity
	 *            The minimum similarity for the automation
	 */
	public void init(float minSimilarity) {
		Settings.MinSimilarity = minSimilarity;
		Settings.CheckLastSeenSimilar = minSimilarity;
		Settings.MoveMouseDelay = MOVE_MOUSE_DELAY;
		Settings.CheckLastSeen = CHECK_LAST_SEEN;
	}

	@Override
	public void run() {

		if (guiAutomation.getTimeout() > 0) {
			new TimeOutWatcher().start();
		}

		// prepare search parameters
		searchRegion.setObserveScanRate(guiAutomation.getScanRate());
		searchRegion.onAppear(SystemUtils.replaceSystemVariables(guiAutomation
				.getImagePath()), observerCallBack);

		while (running) {
			try {
				Thread.sleep(100);
				if (isActive()) {
					triggerAutomation();
				}
			} catch (InterruptedException e) {
				log.error("Failure in GUIAutomator Thread sleep", e);
			}
		}
	}

	/**
	 * Terminate the automator thread
	 */
	public void terminate() {
		if (searchRegion != null) {
			searchRegion.stopObserver();
		}
		running = false;
	}

	/**
	 * Sets the GUI automation that shall be run.
	 * 
	 * @param guiAutomation
	 *            A GUI automation
	 */
	public void setGUIAutomation(GUIAutomation guiAutomation) {
		this.guiAutomation = guiAutomation;

		log.info(("(" + getName() + "): Activated Automation: " + guiAutomation
				.toString()));
	}

	/**
	 * Runs a configured GUI automation.
	 */
	private void triggerAutomation() {

		// always
		if (guiAutomation.getTrigger().equals(GUIAutomation.TRIGGER_ALWAYS)) {
			if (guiAutomation.isActive()) {
				searchAndRunAutomation();
			}
		}

		// once
		if (guiAutomation.getTrigger().equals(GUIAutomation.TRIGGER_ONCE)) {
			if (guiAutomation.isActive()) {
				if (searchAndRunAutomation()) {
					guiAutomation.setActive(false);
				}
			}
		}

		// on change
		if (guiAutomation.getTrigger().equals(
				GUIAutomation.TRIGGER_ONCE_PER_CHANGE)) {
			if (guiAutomation.isActive()) {
				if (searchAndRunAutomation()) {
					guiAutomation.setActive(false);
				}
			}
		}

		// on midi
		if (guiAutomation.getTrigger().contains(GUIAutomation.TRIGGER_MIDI)) {
			if (guiAutomation.isActive()) {
				if (searchAndRunAutomation()) {
					guiAutomation.setActive(false);
				}
			}
		}
	}

	/**
	 * Activates all automation that shall be run only once per change.
	 */
	public void activateOncePerChangeAutomations() {

		if (guiAutomation.getTrigger().equals(
				GUIAutomation.TRIGGER_ONCE_PER_CHANGE)) {
			log.info("Activate automation once per change:" + guiAutomation);
			guiAutomation.setActive(true);
		}
	}

	/**
	 * Activates all automation that shall be run by midi message.
	 * 
	 * @param midiSignature
	 *            The midi signature that shall invoke the automation
	 */
	public void activateMidiAutomations(String midiSignature) {

		String trigger = guiAutomation.getTrigger();
		String signature = guiAutomation.getMidiSignature();

		if (signature != null && midiSignature != null) {
			if ((trigger.contains(GUIAutomation.TRIGGER_MIDI) && (signature
					.equals(midiSignature)))) {
				log.info("Activated automation " + guiAutomation
						+ " by MIDI message: " + midiSignature);
				guiAutomation.setActive(true);
			}
		}
	}

	/**
	 * Searches for the region and runs the desired action if the automation are
	 * active.
	 *
	 * @return <TRUE> if the automation was run, <FALSE> if not found or
	 *         automation not active
	 */
	private boolean searchAndRunAutomation() {

		boolean wasRun = false;
		String imagePath = guiAutomation.getImagePath();

		// Run automation without searching if no screenshot was given
		if (imagePath.equals(MidiAutomatorProperties.VALUE_NULL)) {

			runAutomation(match);
			wasRun = true;

		} else {

			if (searchForScreenshot()) {
				// do not run automation if it was deactivated in the meantime
				if (isActive()) {
					runAutomation(match);
					wasRun = true;
				}
			} else {
				log.info("("
						+ getName()
						+ "): Could not find match on screen for \""
						+ SystemUtils.replaceSystemVariables(guiAutomation
								.getImagePath()) + "\"");
			}

		}

		return wasRun;
	}

	/**
	 * Search for the given screenshot
	 *
	 * @return <TRUE> if it was found, <FALSE> if it was not found within the
	 *         time period.
	 */
	private boolean searchForScreenshot() {

		Region lastFound = guiAutomation.getLastFoundRegion();

		// reduce search region
		if (lastFound != null && !guiAutomation.isMovable()
				&& !fixedSearchRegion) {

			searchRegion.x = lastFound.x;
			searchRegion.y = lastFound.y;
			searchRegion.w = lastFound.w;
			searchRegion.h = lastFound.h;

			fixedSearchRegion = true;
		}

		log.debug("("
				+ getName()
				+ "): Search for match of \""
				+ SystemUtils.replaceSystemVariables(guiAutomation
						.getImagePath()) + "\" " + ", minimum smimilarity: "
				+ String.format("%.2g%n", Settings.MinSimilarity)
				+ ", scan rate: " + searchRegion.getObserveScanRate());

		return searchRegion.observe(SIKULIX_TIMEOUT);
	}

	/**
	 * Runs automation the automation action on the found region.
	 *
	 * @param match
	 *            The found region.
	 */
	private void runAutomation(Match match) {
		try {
			Thread.sleep(guiAutomation.getMinDelay());
		} catch (InterruptedException e) {
			log.error("Delay before GUI automation run failed.", e);
		}

		guiAutomation.setLastFoundRegion(match);

		// set focus
		setFocusToProgram(guiAutomation.getFocusedProgram());

		// left click
		if (guiAutomation.getType().equals(GUIAutomation.CLICKTYPE_LEFT)
				&& match != null) {
			match.click();
		}

		// right click
		if (guiAutomation.getType().equals(GUIAutomation.CLICKTYPE_RIGHT)
				&& match != null) {
			match.rightClick();
		}

		// double click
		if (guiAutomation.getType().equals(GUIAutomation.CLICKTYPE_DOUBLE)
				&& match != null) {
			match.doubleClick();
		}

		// key stroke
		if (guiAutomation.getType().equals(GUIAutomation.TYPE_SENDKEY)) {
			pressAndReleaseKeys(guiAutomation.getKeyCodes());
		}
	}

	/**
	 * Sets the focus to given program
	 *
	 * @param program
	 */
	private void setFocusToProgram(String program) {

		String[] cmd = new String[0];

		if (program != null) {
			if (!program.equals("")
					&& !program.equals(MidiAutomatorProperties.VALUE_NULL)) {

				if (System.getProperty("os.name").contains("Mac")) {
					cmd = new String[2];
					cmd[0] = "open";
					cmd[1] = program;
				}

				if (System.getProperty("os.name").contains("Windows")) {
					cmd = new String[3];
					cmd[0] = "cscript";
					cmd[1] = resources.getWorkingDirectory() + File.separator
							+ "VBS" + File.separator + "setFocus.vbs";
					cmd[2] = parsePIDfromFocusedWindowsProgram(program);
				}

				ShellRunner shellRunner = new ShellRunner(cmd);

				String cmdString = "";
				for (String command : cmd) {
					cmdString = cmdString + command + " ";
				}
				log.info("Focusing program by running: " + cmdString);

				shellRunner.run();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Parses the PID from the focused program String on Windows.
	 * 
	 * @param focusedProgram
	 *            The focused program from the GUI
	 * @return the PID, <NULL> if PID does not exist
	 */
	private String parsePIDfromFocusedWindowsProgram(String focusedProgram) {

		String[] split = focusedProgram
				.split(GUIAutomationsService.PIDSEPARATOR);

		if (split.length > 1) {
			return split[1];
		} else {
			return null;
		}
	}

	/**
	 * Sends the key of the automation with modifiers.
	 *
	 * @param keyCodes
	 *            The key codes to press and release i order
	 */
	private void pressAndReleaseKeys(int[] keyCodes) {

		if (keyCodes != null) {
			try {
				Robot robot = new Robot();
				for (int keyCode : keyCodes) {

					robot.keyPress(keyCode);
					log.info("(" + getName() + "): Pressing key "
							+ KeyEvent.getKeyText(keyCode));
				}

				for (int keyCode : keyCodes) {

					robot.keyRelease(keyCode);
					log.info("(" + getName() + "): Releasing key "
							+ KeyEvent.getKeyText(keyCode));
				}

			} catch (AWTException e) {
				log.error("Error generating Robot object.", e);
			}
		}
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	public GUIAutomation getGuiAutomation() {
		return guiAutomation;
	}

	/**
	 * The observer call back stores the found match on appear.
	 *
	 * @author aguelle
	 *
	 */
	class AppearingMatchObserver extends ObserverCallBack {

		@Override
		public void appeared(ObserveEvent e) {

			match = e.getMatch();

			log.info("("
					+ getName()
					+ "): Found match on screen for \""
					+ SystemUtils.replaceSystemVariables(guiAutomation
							.getImagePath()) + "\"");
		}
	}

	/**
	 * Takes the running time and stops the automation if the timeout exceeded.
	 *
	 * @author aguelle
	 *
	 */
	class TimeOutWatcher extends Thread {

		private long startingTime;
		private boolean running = true;

		@Override
		public void run() {

			startingTime = System.currentTimeMillis();

			while (running) {
				long runningTime = System.currentTimeMillis() - startingTime;

				if (runningTime > guiAutomation.getTimeout()) {

					running = false;

					log.info("(" + getName()
							+ "): Automation timed out after: " + runningTime);

					terminate();
				}
			}
		}
	}
}
