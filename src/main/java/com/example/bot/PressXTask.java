// package com.example.bot;

// import java.awt.event.KeyEvent;
// import java.time.LocalTime;

// import com.example.util.RobotActions;
// import com.example.util.RobotTask;

// /** Periodically does the "switch food slot" key combo and eats {@code food} times. */
// public class PressXTask extends RobotTask {

//     private final int food;

//     public PressXTask(int food) {
//         this.food = food;
//     }

//     @Override
//     public void run() {
//         System.out.println(LocalTime.now() + " EAT START");

//         robot.keyPress(KeyEvent.VK_CONTROL);
//         robot.keyPress(KeyEvent.VK_D);
//         robot.keyRelease(KeyEvent.VK_D);
//         robot.keyPress(KeyEvent.VK_W);
//         robot.keyRelease(KeyEvent.VK_W);
//         robot.keyRelease(KeyEvent.VK_CONTROL);

//         RobotActions.eatFood(robot, food);
//     }
// }
