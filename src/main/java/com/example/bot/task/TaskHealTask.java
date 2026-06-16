package com.example.bot.task;

import com.example.config.TaskConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;

/** Drinks a potion whenever the Task health pixel is no longer the "full" color. */
public class TaskHealTask extends RobotTask {

    private final TaskConfig config;

    public TaskHealTask(TaskConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        RobotActions.healIfNeeded(robot, config.healX, config.healY, config.healColor);
    }
}
