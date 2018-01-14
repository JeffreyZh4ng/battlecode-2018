package units.robots;

import bc.MapLocation;
import commandsAndRequests.Command;
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
     * Executes the given task
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
                //return fight()
            case SNIPE:
                //return snipe(commandLocation);
            default:
                return true;
        }
    }

    /**
     * attacks enemy robots within its range, or move towards nearest one
     * @return if it has an attack objective
     */
    private boolean fight() {
        return true;
    }

    /**
     * attacks with specific enemy unit and location in mind, a more aggressive strategy
     * @param targetLocation where enemy unit is thought to be
     * @param targetId the id of targeted enemy unit
     * @return whether or enemy robot was killed yet
     */
    private boolean targetedAttack(MapLocation targetLocation, int targetId) {
        return true;
    }

    /**
     * attacks the weakest enemy robot that it can attack
     * @return whether or not a robot was attacked
     */
    private boolean attackWeakestEnemyRobot() {
        return true;
    }


    /**
     * snipes location, does whole proccess and returns true once location is sniped
     * @param snipeLocation location to snipe
     * @return if compleated this round returns true otherwise returns false
     */
    private boolean snipe(MapLocation snipeLocation) {

        //snipeing ist that great so will implement latter
        return true;
    }

}
