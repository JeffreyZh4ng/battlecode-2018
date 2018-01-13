package robots;

import commandsAndRequests.Task;
import planets.Earth;

public class Ranger extends Robot {

    public Ranger(int id) {
        super(id);
    }

    @Override
    public void addTaskToQueue(Task task) {
        //Earth.earthTaskMap.put()
    }

    public boolean execute() {
        if (this.robotTaskQueue.isEmpty()) {
            //searchForKarbonite();
        } else {
            performTask();
        }
        return true;
    }

    private void performTask() {

    }
}
