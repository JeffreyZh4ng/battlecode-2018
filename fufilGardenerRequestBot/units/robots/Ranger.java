package units.robots;

import bc.MapLocation;
import com.sun.javafx.collections.MapListenerHelper;
import commandsAndRequests.Command;
import commandsAndRequests.Globals;
import commandsAndRequests.RobotTask;
import units.Robot;
import bc.VecUnit;
import bc.Unit;

import java.util.Map;

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
     * finds the weakest enemy robot that it can attack
     * @return the location of the weakest enemy that can be attacked or null if none
     */
    private MapLocation getWeakestEnemyRobotInRange() {

        //get and store this units location
        MapLocation thisUnitsLocation = Globals.gameController.unit(this.id).location().mapLocation();

        //get enemy units within attack radius
        VecUnit enemyUnits = Globals.gameController.senseNearbyUnitsByTeam(thisUnitsLocation,
                Globals.gameController.unit(this.id).attackRange(), Globals.gameController.team());

        //if no enemy unit in range return false
        if (enemyUnits.size() == 0) {
            return null;
        }

        //find weakest enemy unit in range
        Unit weakestEnemyUnit = enemyUnits.get(0);
        for (int i= 0; i < enemyUnits.size(); i ++) {

            //check if weakest and can attack
            if (Globals.gameController.canAttack(this.id, enemyUnits.get(i).id()) && enemyUnits.get(i).health()<weakestEnemyUnit.health()) {
                weakestEnemyUnit = enemyUnits.get(i);
            }
        }
        return weakestEnemyUnit.location().mapLocation();
    }



    /**
     * snipes location, does whole proccess and returns true once location is sniped
     * @param snipeLocation location to snipe
     * @return if completed this round returns true otherwise returns false
     */
    private boolean snipe(MapLocation snipeLocation) {

        //sniping ist that great so will implement latter
        return true;
    }

}
