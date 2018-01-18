import bc.MapLocation;
import bc.Team;
import bc.VecUnit;
import bc.Unit;

public class Ranger extends Attacker {

    public Ranger(int id) {
        super(id);
    }

    public void run() {

        runAttack();

//        if (this.getRobotTaskQueue().size() != 0) {
//            System.out.println("Successfully ran robot: " + this.getEmergencyTask() + "'s run() method!");
//            RobotTask currentTask = this.getRobotTaskQueue().peek();
//            if (executeTask(currentTask)) {
//                this.getRobotTaskQueue().poll();
//            }
//        }
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
                return move(this.getId(), commandLocation);
            case IN_COMBAT:
                return attackClosestEnemyInRange();
            default:
                return true;
        }
    }

//    /**
//     * attacks enemy robots within its range, or move towards nearest one
//     * @return if it has an attack objective
//     */
//    private boolean aggressiveAttack() {
//        //TODO: find best nearby targets and attacks
//        return true;
//    }
//
//    /**
//     * attacks with specific enemy unit and location in mind, a more aggressive strategy
//     * @param targetLocation where enemy unit is thought to be
//     * @param targetId the id of targeted enemy unit
//     * @return whether or enemy robot was killed yet
//     */
//    private boolean targetedAttack(MapLocation targetLocation, int targetId) {
//        //TODO: seek and destroy a unit
//        return true;
//    }
//
//
//    /**
//     * will find the most powerful but also weakest target to attack
//     * in other words, finds which unit to attack to most hurt the enemy
//     * @return best Target Enemy Unit or null if none found in range
//     */
//    private Unit getBestEnemyTargetInRange() {
//        //get and store this units location
//        MapLocation thisUnitsLocation = Player.gc.unit(this.getId()).location().mapLocation();
//
//        //get enemy units within attack radius
//        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(thisUnitsLocation, Player.gc.unit(this.getId()).attackRange(), Player.gc.team());
//
//        //if no enemy unit in range return false
//        if (enemyUnits.size() == 0) {
//            return null;
//        }
//
//        //find best enemy target unit in rangeg
//        Unit bestTargetEnemyUnit = enemyUnits.get(0);
//        float currentGoodness = calculateTargetGoodness(bestTargetEnemyUnit);
//        for (int i= 0; i < enemyUnits.size(); i ++) {
//
//            //check if weakest and can attack
//            if (Player.gc.canAttack(this.getId(), enemyUnits.get(i).id()) && currentGoodness < calculateTargetGoodness(enemyUnits.get(i))) {
//                bestTargetEnemyUnit = enemyUnits.get(i);
//            }
//        }
//        return bestTargetEnemyUnit;
//    }
//
//    /**
//     * idk wtf to name this
//     * @param enemyUnit the unit to calculate the value for
//     * @return a value that represents how much attacking this unit will hurt the enemy
//     */
//    private float calculateTargetGoodness(Unit enemyUnit) {
//
//        int healthWeight = 1;
//        int costWeight = 4;
//        // the objective of attacking a unit is to kill it eventually so that it can no longer be useful for the enemy
//        // for this reason, if the unit has low heath and can die soon it should probably attacked, also if the unit is
//        // more important to the enemy it should be attacked importance is realted to the cost of the unit
//        return - enemyUnit.health()*healthWeight + bc.bcUnitTypeFactoryCost(enemyUnit.factoryUnitType())*costWeight;
//    }
//
//
//
//    /**
//     * given a unit to defend, move around and kill threats to that unit
//     * @param defendedUnitId
//     * @return
//     */
//    private boolean defendUnit(int defendedUnitId) {
//        //TODO: move and defend a defined unit
//        return true;
//    }


    /**
     * Attacks the weakest enemy that it can
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    private boolean attackClosestEnemyInRange() {

        Team otherTeam = Player.gc.team() == Team.Blue ? Team.Red : Team.Blue;
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), otherTeam);
        if (enemyUnits.size() == 0) {
            return true;
        }

        if (Player.gc.isAttackReady(this.getId())) {
            Unit enemyUnit = enemyUnits.get(0);
            int closestDistanceToUnit = (int)(this.getLocation().distanceSquaredTo(enemyUnits.get(0).location().mapLocation()));;
            for (int i = 0; i < enemyUnits.size(); i++) {
                int distanceToUnit = (int)(this.getLocation().distanceSquaredTo(enemyUnits.get(i).location().mapLocation()));
                if (distanceToUnit < closestDistanceToUnit) {
                    closestDistanceToUnit = distanceToUnit;
                    enemyUnit = enemyUnits.get(i);
                }
            }

            if (Player.gc.canAttack(this.getId(), enemyUnit.id())) {
                Player.gc.attack(this.getId(), enemyUnit.id());
            }
        }

        return false;
    }

//    /**
//     * snipes location, does whole proccess and returns true once location is sniped
//     * @param snipeLocation location to snipe
//     * @return if completed this round returns true otherwise returns false
//     */
//    private boolean snipe(MapLocation snipeLocation) {
//        //TODO: sniper stuff p simple but not v important
//        //sniping ist that great so will implement latter
//        return true;
//    }
}
