import bc.*;

import java.util.ArrayList;

public abstract class Attacker extends Robot {

    private int focusedTargetId;

    public Attacker(int id) {
        super(id);
        focusedTargetId = -1;
    }

    public int getAttackRange() {
        return (int)(Player.gc.unit(this.getId()).attackRange());
    }

    public int getFocusedTargetId() {
        return focusedTargetId;
    }

    public void setFocusedTargetId(int focusedTargetId) {
        this.focusedTargetId = focusedTargetId;
    }

    /**
     * If attacker is not in combat, it moves to and attacks the global attackTarget otherwise it attacks the closest enemy in range
     */
    public void runAttacker() {

        updateTargets();

        if (this.getEmergencyTask() != null) {
            executeEmergencyTask();

        } else if (this.hasTasks()) {
            executeCurrentTask();

        } else {
            wanderToGlobalAttack();
        }
    }

    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    public abstract boolean runBattleAction();

    /**
     * A special movement that will move a robot in combat. Does not take into account a path.
     * @param isTowardsTarget If you want to move the robot towards the enemy location
     * @param enemyLocation The location of the enemy
     */
    public void inCombatMove(boolean isTowardsTarget, MapLocation enemyLocation) {
        int directionToCheck = this.getLocation().directionTo(enemyLocation).swigValue();

        if (!isTowardsTarget) {
            directionToCheck += 4;
        }

        // Adds the direction to/away from the enemy and the adjacent directions
        ArrayList<Integer> directionList = new ArrayList<>();
        directionList.add(directionToCheck);
        directionList.add(directionToCheck + 1);
        directionList.add(directionToCheck - 1);

        // Changes any value that is outside of the direction range
        for (int i = 0; i < directionList.size(); i++) {
            int directionValue = directionList.get(i);

            if (directionValue > 7) {
                directionList.set(i, directionValue - 8);

            } else if (directionValue < 0) {
                directionList.set(i, directionValue + 8);
            }
        }

        for (int i = 0; i < directionList.size(); i++) {
            Direction direction = Direction.swigToEnum(directionList.get(i));
            if (Player.gc.canMove(this.getId(), direction)) {
                Player.gc.moveRobot(this.getId(), direction);

                System.out.println("Moved attacker: " + direction);
                return;
            }
        }

        System.out.println("Could not move the attacker: " + this.getId() + " in combat!");
    }

    /**
     * Senses nearby for enemy units. If any are found, set the emergency task to in combat. Will check if any
     * nearby units are in the global focused attack list. If not, it will pick the nearest unit and add it
     * to the global focused attack list. Will set its own focused target to the unit.
     */
    public void updateTargets() {
        VecUnit enemyUnits = this.getEnemyUnitsInRange();
        if (enemyUnits != null && enemyUnits.size() > 0) {

            System.out.println("Attacker: " + this.getId() + " saw enemies!");
            if (this.getEmergencyTask() == null || this.getEmergencyTask().getCommand() != Command.IN_COMBAT) {

                // This checks if you were the first to see the enemy location. If you were, the broadcast the location
                if (this.hasTasks() && this.getCurrentTask().getCommand() != Command.ALERTED) {
                    broadcastFocusedTarget();
                }
                setEmergencyTaskToInCombat();
            }

            findBestTarget(enemyUnits);

        } else {
            System.out.println("Attacker: " + this.getId() + " Did not see any enemies");

            // If the robot does not sense any enemies, but it is still in combat, the enemy it was in combat
            // with has been killed
            if (this.getEmergencyTask() != null && this.getEmergencyTask().getCommand() == Command.IN_COMBAT) {
                System.out.println("Attacker has left combat. Checking if a global attack location has been seen");
                this.setEmergencyTask(null);
            }

            checkGlobalAttackLocation();
        }
    }

    /**
     * Method that will set the emergency task to in combat. When it does this it also pops off the tops task
     * in the task queue.
     */
    private void setEmergencyTaskToInCombat() {
        System.out.println("Attacker: " + this.getId() + " setting emergency task to IN COMBAT!");
        this.setEmergencyTask(new RobotTask(-1, Command.IN_COMBAT, this.getLocation()));

        // Optimally this if statement should never be needed but its here to guard against exceptions
        if (this.hasTasks()) {
            this.pollCurrentTask();
        }
    }

    /**
     * Method that will allow the current robot to broadcast the location of an enemy to nearby units. The units
     * that receive the broadcast will move to towards the location and set their emergency task to if they see the
     * enemy.
     */
    private void broadcastFocusedTarget() {
        VecUnit nearbyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), 20, Player.team);

        for (int i = 0; i < nearbyUnits.size(); i++) {

            // Checks if the nearby unit is not a worker, healer, or itself.
            Unit nearbyUnit = nearbyUnits.get(i);

            if (nearbyUnit.team() == Player.team && nearbyUnit.unitType() != UnitType.Worker && nearbyUnit.unitType() != UnitType.Healer &&
                    nearbyUnit.unitType() != UnitType.Factory && nearbyUnit.unitType() != UnitType.Rocket && nearbyUnit.id() != this.getId()) {

                // If the current nearby unit's task is not already ALERTED, and if the task isn't part of a global task, poll it
                UnitInstance friendlyAttacker = Earth.earthAttackerMap.get(nearbyUnit.id());
                if (friendlyAttacker.hasTasks() && friendlyAttacker.getCurrentTask().getCommand() != Command.ALERTED &&
                        friendlyAttacker.getCurrentTask().getTaskId() == -1) {

                    friendlyAttacker.pollCurrentTask();
                }

                System.out.println("Attacker: " + this.getId() + " has alerted " + friendlyAttacker.getId());
                friendlyAttacker.addTaskToQueue(new RobotTask(-1, Command.ALERTED, this.getLocation()));
            }
        }

        System.out.println("Attacker: " + this.getId() + " Finished trying to alert other units");
    }

    /**
     * Method that will check the enemy units nearby, if one of the nearby units is in the global focused targets,
     * set it to you this attackers focused attack
     */
    public void findBestTarget(VecUnit enemyUnits) {
        ArrayList<Integer> enemyUnitIds = new ArrayList<>();
        for (int i = 0; i < enemyUnits.size(); i++) {
            enemyUnitIds.add(enemyUnits.get(i).id());
        }

        // If your current focused attack target is not within the enemy units in your vision range, pick a new target
        if (!enemyUnitIds.contains(this.getFocusedTargetId())) {
            for (int i = 0; i < enemyUnits.size(); i++) {

                int enemyUnitId = enemyUnits.get(i).id();
                if (Earth.earthFocusedTargets.contains(enemyUnitId)) {
                    focusedTargetId = enemyUnitId;

                    System.out.println("Attacker: " + this.getId() + " is targeting new enemy unit: " + enemyUnitId);
                    return;
                }
            }

            int enemyId = this.getClosestEnemy(enemyUnits).id();
            Earth.earthFocusedTargets.add(enemyId);
            focusedTargetId = enemyId;

            System.out.println("Attacker: " + this.getId() + " creating new focused attack target: " + enemyId);
        }

    }

    /**
     * Helper method that will remove the global attack location if it has left combat and it can see the latest
     * attack location.
     */
    private void checkGlobalAttackLocation() {
        if (!Earth.earthMainAttackStack.empty()) {

            MapLocation location = Earth.earthMainAttackStack.peek();
            if (this.getLocation().distanceSquaredTo(location) < this.getVisionRange()) {
                System.out.println("Global attack location: " + Player.locationToString(location) + " has been checked!");
                Earth.earthMainAttackStack.pop();
            }
        }
    }

    /**
     * Helper method that will run the emergency task for this unit. If it is the stall command, it will not
     * get rid of the emergency task.
     */
    private void executeEmergencyTask() {
        if (executeTask(this.getEmergencyTask())) {
            System.out.println("Worker: " + this.getId() + " Finished emergency task!");
            this.setEmergencyTask(null);
        }
    }

    /**
     * Helper method that will run the workers current tasks. If it finished one, it will sense for enemy robots.
     * If any are found, set the emergency task to in combat and execute the attack command
     */
    private void executeCurrentTask() {
        if (this.hasTasks()) {
            System.out.println("Attacker: " + this.getId() + " on task " + this.getCurrentTask().getCommand());
        }

        if (this.hasTasks() && executeTask(this.getCurrentTask())) {
            System.out.println("Attacker: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
            if (this.getCurrentTask().getCommand() == Command.WANDER && Earth.earthMainAttackStack.size() > 0 &&
                    this.getCurrentTask().getCommandLocation().equals(Earth.earthMainAttackStack.peek())) {
                Earth.earthMainAttackStack.pop();
            }
            this.pollCurrentTask();

            if (getEnemyUnitsInRange().size() > 0) {
                updateTargets();
                executeTask(this.getEmergencyTask());
            } else {
                executeCurrentTask();
            }
        }
    }

    /**
     * Executes the task from the task queue
     * @param robotTask The task the robot has to complete
     * @return If the task was completed or not
     */
    private boolean executeTask(RobotTask robotTask) {
        Command robotCommand = robotTask.getCommand();
        MapLocation commandLocation = robotTask.getCommandLocation();

        switch (robotCommand) {
            case MOVE:
                return this.pathManager(commandLocation);
            case WANDER:
                System.out.println("willwanderid: " + this.getId() + "my loc: " + this.getLocation() + " wanderLoc: " + commandLocation);
                return this.pathManager(commandLocation);
            case ALERTED:
                return this.pathManager(commandLocation);
            case IN_COMBAT:
                return runBattleAction();
            case STALL:
                this.requestUnitToLoad(commandLocation);
                return false;
            default:
                System.out.println("Critical error occurred in attacker: " + this.getId());
                return true;
        }
    }

    /**
     * Method that will set the current task to wander to the global attack location. If there are no mare global
     * locations in the queue, it will wander randomly
     */
    private void wanderToGlobalAttack() {
        if (!Earth.earthMainAttackStack.isEmpty()) {
            MapLocation attackLocation = Earth.earthMainAttackStack.peek();

            System.out.println("Attacker: " + this.getId() + " moving to global attack location: " + Player.locationToString(attackLocation));
            this.addTaskToQueue(new RobotTask(-1, Command.WANDER, attackLocation));
            System.out.println("id: " + this.getId() + "addingal my loc: " + this.getLocation() + " wanderLoc: " + attackLocation);
        } else {
            VecMapLocation mapLocations = Player.gc.allLocationsWithin(this.getLocation(), this.getAttackRange());

            MapLocation wanderLocation = null;
            while (wanderLocation == null) {
                System.out.println("Attacker: " + this.getId() + " trying to find a random location!");
                int randomLocation = (int)(Math.random() * mapLocations.size());

                if (Player.isLocationEmpty(mapLocations.get(randomLocation))) {
                    wanderLocation = mapLocations.get(randomLocation);
                }
            }

            System.out.println("Attacker: " + this.getId() + " wandering to random location!");
            this.addTaskToQueue(new RobotTask(-1, Command.WANDER, wanderLocation));
            System.out.println("id: " + this.getId() + "addingl my loc: " + this.getLocation() + " wanderLoc: " + wanderLocation);
        }
    }
}
