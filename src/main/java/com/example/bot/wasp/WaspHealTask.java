package com.example.bot.wasp;

import com.example.config.WaspConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;

/** Drinks a potion whenever the Wasp health pixel is no longer the "full" color. */
public class WaspHealTask extends RobotTask {

    private final WaspConfig config;

    public WaspHealTask(WaspConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        RobotActions.healIfNeeded(robot, config.healX, config.healY, config.healColor);
    }
}
