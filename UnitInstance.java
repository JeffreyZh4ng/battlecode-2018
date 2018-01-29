import bc.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The top level class of all kinds of units. Each unit has an ID and a task queue. All units have a run method
 * to execute their code
 */
public abstract class UnitInstance {

    private int id;
    private Queue<RobotTask> taskQueue;
    private UnitType unitType;
    private RobotTask emergencyTask;

    public UnitInstance(int id) {
        this.id = id;
        taskQueue = new LinkedList<>();
        unitType = Player.gc.unit(id).unitType();
        emergencyTask = null;
    }

    /**
     * Each structure will need to know how to run specific commands. Rockets, Factories, and Blueprints
     * all run differently
     */
    public abstract void run();

    public int getId() {
        return id;
    }

    public int getVisionRange() {
        return (int)(Player.gc.unit(this.getId()).visionRange());
    }

    public boolean hasTasks() {
        return (!taskQueue.isEmpty());
    }

    public MapLocation getLocation() {
        return Player.gc.unit(this.getId()).location().mapLocation();
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public RobotTask getCurrentTask() {
        return taskQueue.peek();
    }

    public void addTaskToQueue(RobotTask task) {
        taskQueue.add(task);
    }

    public void pollCurrentTask() {
        taskQueue.poll();
    }

    public RobotTask getEmergencyTask() {
        return emergencyTask;
    }

    public void setEmergencyTask(RobotTask emergencyTask) {
        this.emergencyTask = emergencyTask;
    }

    /**
     * Gets all the enemy units in the range of this unit instance with the given range
     * @return A vecUnit of all the enemy units in range
     */
    public VecUnit getEnemyUnitsInRange() {
        Team otherTeam = Player.gc.team() == Team.Blue ? Team.Red : Team.Blue;
        return Player.gc.senseNearbyUnitsByTeam(this.getLocation(), this.getVisionRange(), otherTeam);
    }

    /**
     * Helper method that will get the id of the closest enemy unit
     * @param enemyUnits The list of all enemy units in vision range
     * @return The id of the closest enemy unit
     */
    public Unit getClosestEnemy(VecUnit enemyUnits) {
        Unit closestEnemy = enemyUnits.get(0);
        int closestDistance = (int)(this.getLocation().distanceSquaredTo(enemyUnits.get(0).location().mapLocation()));

        for (int i = 0; i < enemyUnits.size(); i++) {
            MapLocation enemyUnitLocation = enemyUnits.get(i).location().mapLocation();
            if (this.getLocation().distanceSquaredTo(enemyUnitLocation) < closestDistance) {
                closestEnemy = enemyUnits.get(i);
            }
        }

        return closestEnemy;
    }
}
