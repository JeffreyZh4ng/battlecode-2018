package units.robots;

import commandsAndRequests.GlobalTask;
import units.Robot;

public class Ranger extends Robot {

    public Ranger(int id) {
        super(id);
    }

    public void run() {
        if (this.robotTaskQueue.isEmpty()) {
            //searchForKarbonite();
        } else {
            performTask();
        }
    }

    private void performTask() {

    }
}
