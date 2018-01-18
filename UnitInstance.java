import bc.Location;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The top level class of all kinds of units. Each unit has an ID and a task queue. All units have a run method
 * to execute their code
 */
public abstract class UnitInstance {

    private int id;
    private RobotTask currentTask;

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

    public boolean isIdle() {
        return currentTask == null;
    }

    public RobotTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(RobotTask currentTask) {
        this.currentTask = currentTask;
    }

    public void removeTask() {
        currentTask = null;
    }

    public MapLocation getLocation() {
        return Player.gc.unit(this.getId()).location().mapLocation();
    }

    public int getVisionRange() {
        return (int)(Player.gc.unit(this.getId()).visionRange());
    }


}
