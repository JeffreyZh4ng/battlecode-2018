package units;

import commandsAndRequests.RobotTask;

import java.util.PriorityQueue;

public abstract class Unit {
    public int id;
    public PriorityQueue<RobotTask> robotTaskQueue;


    public Unit(int id) {
        this.id = id;
    }

    /**
     * Each structure will need to know how to run specific commands. Rockets, Factories, and Blueprints
     * all run differently
     */
    public abstract void run();
}
