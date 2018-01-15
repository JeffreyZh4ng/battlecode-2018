import bc.MapLocation;
import bc.bc;
import bc.VecUnit;
import bc.Unit;

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
    private boolean aggressiveAttack() {
        //TODO: find best nearby targets and attacks
        return true;
    }

    /**
     * attacks with specific enemy unit and location in mind, a more aggressive strategy
     * @param targetLocation where enemy unit is thought to be
     * @param targetId the id of targeted enemy unit
     * @return whether or enemy robot was killed yet
     */
    private boolean targetedAttack(MapLocation targetLocation, int targetId) {
        //TODO: seek and destroy a unit
        return true;
    }


    /**
     * will find the most powerful but also weakest target to attack
     * in other words, finds which unit to attack to most hurt the enemy
     * @return best Target Enemy Unit or null if none found in range
     */
    private Unit getBestEnemyTargetInRange() {
        //get and store this units location
        MapLocation thisUnitsLocation = Player.gc.unit(this.id).location().mapLocation();

        //get enemy units within attack radius
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(thisUnitsLocation, Player.gc.unit(this.id).attackRange(), Player.gc.team());

        //if no enemy unit in range return false
        if (enemyUnits.size() == 0) {
            return null;
        }

        //find best enemy target unit in rangeg
        Unit bestTargetEnemyUnit = enemyUnits.get(0);
        float currentGoodness = calculateTargetGoodness(bestTargetEnemyUnit);
        for (int i= 0; i < enemyUnits.size(); i ++) {

            //check if weakest and can attack
            if (Player.gc.canAttack(this.id, enemyUnits.get(i).id()) && currentGoodness < calculateTargetGoodness(enemyUnits.get(i))) {
                bestTargetEnemyUnit = enemyUnits.get(i);
            }
        }
        return bestTargetEnemyUnit;
    }

    /**
     * idk wtf to name this
     * @param enemyUnit the unit to calculate the value for
     * @return a value that represents how much attacking this unit will hurt the enemy
     */
    private float calculateTargetGoodness(Unit enemyUnit) {

        int healthWeight = 1;
        int costWeight = 4;
        // the objective of attacking a unit is to kill it eventually so that it can no longer be useful for the enemy
        // for this reason, if the unit has low heath and can die soon it should probably attacked, also if the unit is
        // more important to the enemy it should be attacked importance is realted to the cost of the unit
        return - enemyUnit.health()*healthWeight + bc.bcUnitTypeFactoryCost(enemyUnit.factoryUnitType())*costWeight;
    }



    /**
     * given a unit to defend, move around and kill threats to that unit
     * @param defendedUnitId
     * @return
     */
    private boolean defendUnit(int defendedUnitId) {
        //TODO: move and defend a defined unit
        return true;
    }

    /**
     * attacks the weakest enemy that it can
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    private boolean attackWeakestEnemyInRange() {
        if (Player.gc.unit(this.id).attackHeat()<10) {

            Unit weakestEnemy = getWeakestEnemyInRange();
            if (weakestEnemy == null) {
                return true;
            }

            Player.gc.attack(this.id, getWeakestEnemyInRange().id());
            return false;
        }
        return false;
    }

    /**
     * finds the weakest enemy robot that it can attack
     * @return the weakest enemy that can be attacked or null if none
     */
    private Unit getWeakestEnemyInRange() {

        //get and store this units location
        MapLocation thisUnitsLocation = Player.gc.unit(this.id).location().mapLocation();

        //get enemy units within attack radius
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(thisUnitsLocation, Player.gc.unit(this.id).attackRange(), Player.gc.team());

        //if no enemy unit in range return false
        if (enemyUnits.size() == 0) {
            return null;
        }

        //find weakest enemy unit in rangeg
        Unit weakestEnemyUnit = enemyUnits.get(0);
        for (int i= 0; i < enemyUnits.size(); i ++) {

            //check if weakest and can attack
            if (Player.gc.canAttack(this.id, enemyUnits.get(i).id()) && enemyUnits.get(i).health()<weakestEnemyUnit.health()) {
                weakestEnemyUnit = enemyUnits.get(i);
            }
        }
        return weakestEnemyUnit;
    }



    /**
     * snipes location, does whole proccess and returns true once location is sniped
     * @param snipeLocation location to snipe
     * @return if completed this round returns true otherwise returns false
     */
    private boolean snipe(MapLocation snipeLocation) {
        //TODO: sniper stuff p simple but not v important
        //sniping ist that great so will implement latter
        return true;
    }

}
