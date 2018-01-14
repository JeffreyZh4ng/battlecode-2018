package units.robots;

import bc.MapLocation;
import commandsAndRequests.Command;
import commandsAndRequests.GlobalTask;
import commandsAndRequests.RobotTask;
import units.Robot;

public class Ranger extends Robot {

    public Ranger(int id) {
        super(id);
    }
    
    public void run() {
        System.out.println("Got to run method! Task queue size: " + this.robotTaskQueue.size());
        if (this.emergencyTask != null) {
            if (executeTask(this.emergencyTask)) {
                this.emergencyTask = null;
            }
            return;
        }

        if (this.robotTaskQueue.size() != 0) {
            System.out.println("Successfully ran robot: " + this.id + "'s run() method!");
            RobotTask currentTask = this.robotTaskQueue.peek();
            if (executeTask(currentTask)) {
                this.robotTaskQueue.poll();
            }

        }

    }

    /**
     * exceutes the given task
     * @param robotTask the task to complete
     * @return whether or not task is done being completed
     */
    private boolean executeTask(RobotTask robotTask) {
        Command robotCommand = robotTask.getCommand();
        MapLocation commandLocation = robotTask.getCommandLocation();
        switch (robotCommand) {
            case MOVE:
                System.out.println("Running MOVE!");
                return move(this.id, commandLocation);
            case IN_COMBAT:
                //return attack(commandLocation)
            case SNIPE:
                //return snipe(commandLocation);
            default:
                return true;
        }
    }

}
