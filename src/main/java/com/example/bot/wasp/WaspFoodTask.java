package com.example.bot.wasp;

import com.example.config.WaspConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;

/** Eats food {@code config.food} times each cycle. */
public class WaspFoodTask extends RobotTask {

    private final WaspConfig config;

    public WaspFoodTask(WaspConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        RobotActions.eatFood(robot, config.food);
    }
}
