package com.example.util;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.TimerTask;

/**
 * Base for every scheduled task that drives a {@link Robot}, so the
 * "create a Robot in the constructor, swallow AWTException" boilerplate isn't
 * copy-pasted into each task.
 */
public abstract class RobotTask extends TimerTask {

    protected final Robot robot;

    protected RobotTask() {
        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        this.robot = r;
    }
}
