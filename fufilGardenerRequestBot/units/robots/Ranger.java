package units.robots;

import commandsAndRequests.GlobalTask;
import units.Robot;

public class Ranger extends Robot {

    public Ranger(int id) {
        super(id);
    }

    public void execute() {
        if (this.robotTaskQueue.isEmpty()) {
            //searchForKarbonite();
        } else {
            performTask();
        }
    }

    private void performTask() {

    }
}
