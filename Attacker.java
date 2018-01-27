import bc.*;

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

    /**
     * If attacker is not in combat, it moves to and attacks the global attackTarget otherwise it attacks the closest enemy in range
     */
    public void runAttacker() {

        updateTargets();

        if (this.getEmergencyTask() != null) {
            if (this.getEmergencyTask().getCommand() == Command.STALL) {
                return;
            }
            executeTask(this.getEmergencyTask());

        } else if (this.hasTasks()) {
            executeCurrentTask();

        } else {
            // wanderToGlobalAttack();
        }
    }

    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    public abstract boolean runBattleAction();

    /**
     * Senses nearby for enemy units. If any are found, set the emergency task to in combat. Will check if any
     * nearby units are in the global focused attack list. If not, it will pick the nearest unit and add it
     * to the global focused attack list. Will set its own focused target to the unit.
     */
    public void updateTargets() {
        VecUnit enemyUnits = this.getEnemyUnitsInRange();
        if (enemyUnits != null && enemyUnits.size() > 0) {

            if (this.getEmergencyTask() == null || this.getEmergencyTask().getCommand() != Command.IN_COMBAT) {

                // This checks if you were the first to see the enemy location. If you were, the broadcast the location
                if (this.getCurrentTask().getCommand() != null && this.getCurrentTask().getCommand() != Command.ALERTED) {
                    broadcastFocusedTarget();
                }
                setEmergencyTaskToInCombat();
            }

            findBestTarget(enemyUnits);

        } else {

            // If the robot does not sense any enemies, but it is still in combat, the enemy it was in combat
            // with has been killed
            if (this.getEmergencyTask() != null && this.getEmergencyTask().getCommand() == Command.IN_COMBAT) {
                this.setEmergencyTask(null);
            }

            System.out.println("Attacker: " + this.getId() + " Did not see any enemies");
        }
    }

    /**
     * Method that will set the emergency task to in combat. When it does this it also pops off the tops task
     * in the task queue.
     */
    private void setEmergencyTaskToInCombat() {
        System.out.println("Attacker: " + this.getId() + " setting emergency task to IN COMBAT!");
        this.setEmergencyTask(new RobotTask(-1, Command.IN_COMBAT, this.getLocation()));
        this.pollCurrentTask();
    }

    /**
     * Method that will allow the current robot to broadcast the location of an enemy to nearby units. The units
     * that receive the broadcast will move to towards the location and set their emergency task to if they see the
     * enemy.
     */
    private void broadcastFocusedTarget() {
        VecUnit nearbyUnits = Player.gc.senseNearbyUnits(this.getLocation(), 5);
        Team team = Player.gc.team();

        for (int i = 0; i < nearbyUnits.size(); i++) {

            Unit nearbyUnit = nearbyUnits.get(i);
            if (nearbyUnit.team() == team && nearbyUnit.unitType() != UnitType.Worker && nearbyUnit.unitType() != UnitType.Healer) {

                UnitInstance friendlyAttacker = Earth.earthAttackerMap.get(nearbyUnit.id());
                if (friendlyAttacker.hasTasks() && friendlyAttacker.getCurrentTask().getCommand() != Command.ALERTED) {
                    friendlyAttacker.pollCurrentTask();
                }

                System.out.println("Attacker: " + this.getId() + " has alerted " + friendlyAttacker.getId());
                friendlyAttacker.addTaskToQueue(new RobotTask(-1, Command.ALERTED, this.getLocation()));
            }
        }
    }

    /**
     * Method that will check the enemy units nearby, if one of the nearby units is in the global focused targets,
     * set it to you this attackers focused attack
     */
    private void findBestTarget(VecUnit enemyUnits) {

        // If you don't have a target right now, or your target is no longer in the global focused target map, pick a new one
        if (focusedTargetId == -1 || !Earth.earthFocusedTargets.contains(focusedTargetId)) {
            for (int i = 0; i < enemyUnits.size(); i++) {

                int enemyUnitId = enemyUnits.get(i).id();
                if (Earth.earthFocusedTargets.contains(enemyUnitId)) {
                    focusedTargetId = enemyUnitId;

                    System.out.println("Attacker: " + this.getId() + " is targeting enemy unit: " + enemyUnitId);
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
     * Helper method that will run the workers current tasks. If it finished one, it will sense for enemy robots.
     * If any are found, set the emergency task to in combat and execute the attack command
     */
    private void executeCurrentTask() {
        if (this.hasTasks()) {
            System.out.println("Attacker: " + this.getId() + " on task " + this.getCurrentTask().getCommand());
        }

        if (this.hasTasks() && executeTask(this.getCurrentTask())) {
            System.out.println("Attacker: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
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
                return this.pathManager(commandLocation);
            case ALERTED:
                return this.pathManager(commandLocation);
            case IN_COMBAT:
                return runBattleAction();
            case STALL:
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
        if (!Earth.earthMainAttackQueue.isEmpty()) {
            MapLocation attackLocation = Earth.earthMainAttackQueue.peek();

            System.out.println("Attacker: " + this.getId() + " moving to global attack location!");
            this.addTaskToQueue(new RobotTask(-1, Command.WANDER, attackLocation));

        } else {
            VecMapLocation mapLocations = Player.gc.allLocationsWithin(this.getLocation(), this.getVisionRange());

            MapLocation wanderLocation = null;
            while (wanderLocation == null) {
                int randomLocation = (int)(Math.random() * mapLocations.size());

                if (Player.isLocationEmpty(mapLocations.get(randomLocation))) {
                    wanderLocation = mapLocations.get(randomLocation);
                }
            }

            System.out.println("Attacker: " + this.getId() + " wandering!");
            this.addTaskToQueue(new RobotTask(-1, Command.WANDER, wanderLocation));
        }
    }
}
