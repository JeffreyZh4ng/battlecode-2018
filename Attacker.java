import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Attacker extends Robot {

    public Attacker(int id) {
        super(id);
    }

    public int getAttackRange() {
        return (int)(Player.gc.unit(this.getId()).attackRange());
    }

    public static MapLocation getAttackTarget() {
        if (Player.gc.planet() == Planet.Earth) {
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

    public static HashMap<String, MapLocation> mapToAttackTarget = null;

    public static void createMapToAttackTarget() {
        ArrayList<Direction> moveDirections = Player.getMoveDirections();
        MapLocation attackTarget = getAttackTarget();
        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(attackTarget);
        HashMap<String, MapLocation> cameFrom = new HashMap<>();
        cameFrom.put(attackTarget.toString(), attackTarget);

        while (!frontier.isEmpty()) {

            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (doesLocationAppearEmpty(Player.gc.startingMap(Player.gc.planet()), nextLocation) && !cameFrom.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    cameFrom.put(nextLocation.toString(), currentLocation);
                }
            }
        }
    }

//    public boolean moveToAttackTarget() {
//
//    }
//
//    public ArrayList<MapLocation> getPathToAttackTarget() {
//
//    }

    /**
     * If attacker is not in combat, it moves to and attacks the global attackTarget otherwise it attacks the closest enemy in range
     */
    public void runAttacker() {

        updateTask();
        senseForEnemyUnits();

        if (this.getEmergencyTask() != null) {
            if (executeTask(this.getEmergencyTask())) {
                System.out.println("Attacker: " + this.getId() + " Finished emergency task!");

                if (this.getCurrentTask() != null && this.getEmergencyTask().getCommand() == Command.STALL) {
                    GlobalTask globalTask = Earth.earthTaskMap.get(this.getEmergencyTask().getTaskId());
                    globalTask.finishedTask(this.getId(), this.getEmergencyTask().getCommand());

                    return;
                }
                this.setEmergencyTask(null);
            }

        } else if (this.getCurrentTask() != null) {
            if (executeTask(this.getCurrentTask())) {
                if (this.getCurrentTask().getCommand() == Command.IN_COMBAT) {
                    hasAttackLocationBeenChecked();
                }

                int taskId = this.getCurrentTask().getTaskId();
                if (taskId != -1) {
                    Earth.earthTaskMap.get(taskId).finishedTask(this.getId(), this.getCurrentTask().getCommand());
                } else {
                    System.out.println("Attacker: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
                    this.setCurrentTask(null);
                }
        }

        } else {
            System.out.println("Attacker: " + this.getId() + " doing nothing!");
//            this.wander();
//            System.out.println("Unit: " + this.getId() + " wandering!");
//            this.explore();
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
                return this.pathManager(commandLocation);
            case STALL:
                return true;
            case IN_COMBAT:
                return runBattleAction();
            case LOAD_ROCKET:
                return loadRocket(commandLocation);
            default:
                System.out.println("Critical error occurred in attacker: " + this.getId());
                return true;
        }
    }

    /**
     * Updated the current task. If the global attack target is not null, check if the robot has a current task.
     * If it has a current task, check if the task has an id of -1. If it does, replace the task with the
     * updated attack target task. If the current robot has a task with id != -1, ignore it.
     */
    private void updateTask() {
        if (getAttackTarget() != null) {
            if (this.getCurrentTask() != null && this.getCurrentTask().getTaskId() == -1) {
                this.setCurrentTask(new RobotTask(-1, Command.MOVE, getAttackTarget()));
            } else if (this.getCurrentTask() == null) {
                this.setCurrentTask(new RobotTask(-1, Command.MOVE, getAttackTarget()));
            }

        } else if (getAttackTarget() == null && this.getCurrentTask() != null && this.getCurrentTask().getTaskId() == -1) {
            System.out.println("Attacker: " + this.getId() + " The global target has been cleared, removing task");
            this.removeTask();
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
