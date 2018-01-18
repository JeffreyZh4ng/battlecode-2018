import bc.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The top level class of all kinds of units. Each unit has an ID and a task queue. All units have a run method
 * to execute their code
 */
public abstract class UnitInstance {

    private int id;
    private Queue<RobotTask> robotTaskQueue = new LinkedList<>();

    public UnitInstance(int id) {
        this.id = id;
    }

    /**
     * Each structure will need to know how to run specific commands. Rockets, Factories, and Blueprints
     * all run differently
     */
    public abstract void run();

    public int getId() {
        return id;
    }

    public Queue<RobotTask> getRobotTaskQueue() {
        return robotTaskQueue;
    }

    public RobotTask getTopTask() {
        if (robotTaskQueue.size() > 0) {
            return robotTaskQueue.peek();
        } else {
            return null;
        }
    }

    public RobotTask pollTask() {
        RobotTask task = this.getTopTask();
        removeTask();
        return task;
    }

    public void addTask(RobotTask robotTask) {
        robotTaskQueue.add(robotTask);
    }

    public void removeTask() {
        if (robotTaskQueue.size() > 0) {
            robotTaskQueue.poll();
        } else {
            System.out.println("WTF IS HAPPENING?!?! Tried to remove task from an empty queue! Robot: " + this.id);
        }
    }

    /**
     * Finds the closest unit from units outside of given range
     * @param minSquaredRadius the radius to check outside of -1 if want to consider all
     * @param units the units to check
     * @return the closest unit, null if no units outside of radius
     */
    public Unit getClosestUnit(int minSquaredRadius, VecUnit units) {
        if (units.size() == 0) {
            System.out.println("unit list was empty");
            return null;
        }
        Unit minDistanceUnit = null;
        int closestDistanceToUnit = -1;
        for (int i = 0; i < units.size(); i++) {
            int distanceToUnit = (int)(this.getLocation().distanceSquaredTo(units.get(i).location().mapLocation()));
            if ((minDistanceUnit == null && minSquaredRadius < distanceToUnit) ||
                    (minDistanceUnit != null && distanceToUnit < closestDistanceToUnit)) {
                closestDistanceToUnit = distanceToUnit;
                minDistanceUnit = units.get(i);
            }
        }
        return minDistanceUnit;
    }

    public MapLocation getLocation() {
        return Player.gc.unit(this.getId()).location().mapLocation();
    }

    public int getVisionRange() {
        return (int)(Player.gc.unit(this.getId()).visionRange());
    }


}
