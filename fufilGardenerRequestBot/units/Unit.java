package units;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;

import commandsAndRequests.RobotTask;

import java.util.PriorityQueue;

/**
 * The top level class of all kinds of units. Each unit has an ID and a task queue. All units have a run method
 * to execute their code
 */
public abstract class Unit {
    public int id;
    public Queue<RobotTask> robotTaskQueue = new LinkedBlockingQueue<>();


    public Unit(int id) {
        this.id = id;
    }

    /**
     * Each structure will need to know how to run specific commands. Rockets, Factories, and Blueprints
     * all run differently
     */
    public abstract void run();
}
