package com.example.bot.bear;

import com.example.config.BearConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;

/** Drinks a potion whenever the Bear health pixel is no longer the "full" color. */
public class BearHealTask extends RobotTask {

    private final BearConfig config;

    public BearHealTask(BearConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        RobotActions.healIfNeeded(robot, config.healX, config.healY, config.healColor);
    }
}
