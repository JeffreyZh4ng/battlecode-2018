import bc.*;

public abstract class Attacker extends Robot {

    public Attacker(int id) {
        super(id);
    }

    public int getAttackRange() {
        return (int)(Player.gc.unit(this.getId()).attackRange());
    }

    public MapLocation getAttackTarget() {
        Planet planet = this.getLocation().getPlanet();
        if (planet == Planet.Earth) {
            return Earth.earthAttackTarget;
        } else {
            return Mars.marsAttackTarget;
        }
    }

    public void removeAttackTarget() {
        Planet planet = this.getLocation().getPlanet();
        if (planet == Planet.Earth) {
            Earth.earthAttackTarget = null;
        } else {
            Mars.marsAttackTarget = null;
        }
    }

    public void setAttackTarget(MapLocation attackTarget) {
        Planet planet = this.getLocation().getPlanet();
        if (planet == Planet.Earth) {
            Earth.earthAttackTarget = attackTarget;
        } else {
            Mars.marsAttackTarget = attackTarget;
        }
    }

    /**
     * If attacker is not in combat, it moves to and attacks the global attackTarget otherwise it attacks the closest enemy in range
     */
    public void runAttacker() {

        updateTask();
        senseForEnemyUnits();

        if (this.getEmergencyTask() != null) {
            if (executeTask(this.getEmergencyTask())) {
                System.out.println("Unit: " + this.getId() + " Finished emergency task!");
                this.setEmergencyTask(null);
            }

        } else if (this.getCurrentTask() != null) {
            if (executeTask(this.getCurrentTask())) {
                if (this.getCurrentTask().getCommand() == Command.IN_COMBAT) {
                    hasAttackLocationBeenChecked();
                }
                System.out.println("Unit: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
                System.out.println("Removing global attack target! Setting to null");
                this.setCurrentTask(null);
            }

        } else {
            System.out.println("Unit: " + this.getId() + " wandering!");
            this.wander();
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
        // System.out.println("Unit: " + this.getId() + " " + robotCommand);

        switch (robotCommand) {
            case MOVE:
                return this.move(this.getId(), commandLocation);
            case IN_COMBAT:
                return runBattleAction();
            case LOAD_ROCKET:
                return loadRocket(commandLocation);
            default:
                System.out.println("Critical error occurred in unit: " + this.getId());
                return true;
        }
    }

    /**
     * Updated the current task. If the robot has no current task and the attack location is not null, set a new
     * task to move to the attack location
     */
    private void updateTask() {
        if (getAttackTarget() != null && this.getCurrentTask() != null && this.getCurrentTask().getTaskId() == -1) {
            this.setCurrentTask(new RobotTask(-1, Command.MOVE, getAttackTarget()));
        }
    }

    /**
     * Senses nearby for enemy units. If any are found, set the emergency task to in combat. If the global
     * attack location is also null, set the current location to the enemy unit's location
     */
    private void senseForEnemyUnits() {
        VecUnit enemyUnits = this.getEnemyUnitsInRange(this.getVisionRange());

        if (enemyUnits != null && enemyUnits.size() > 0) {
            if (getAttackTarget() == null) {
                Unit enemyUnit = getClosestUnit(-1, enemyUnits);
                setAttackTarget(enemyUnit.location().mapLocation());
                System.out.println("Unit: " + this.getId() + " Setting global attack target to: " + enemyUnit.location().mapLocation().toString());
            }

            setEmergencyTask(new RobotTask(-1, Command.IN_COMBAT, this.getLocation()));
        }
    }

    /**
     * Helper method that will remove the global attack location if the current robot is not in combat and if
     * it has the global attack target location in attack sight
     */
    private void hasAttackLocationBeenChecked() {
        MapLocation currentLocation = this.getLocation();
        if (getAttackTarget() != null) {

            int distanceToAttackLocation = (int)(currentLocation.distanceSquaredTo(getAttackTarget()));
            if (distanceToAttackLocation < this.getAttackRange()) {
                removeAttackTarget();
                System.out.println("Global target has been removed!");
            }

        }
    }

    /**
     * Will load the rocket at the given location
     * @return
     */
    public boolean loadRocket(MapLocation mapLocation) {
        return true;
    }

    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    public abstract boolean runBattleAction();
}
