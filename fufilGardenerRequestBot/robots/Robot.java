package robots;

import commandsAndRequests.Task;

import java.util.PriorityQueue;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot {

    private int id;
    private PriorityQueue<Task> robotTaskQueue;

    /**
     * Constructor that will set the id of the robot when it is created
     * @param id The id of the robot
     */
    public Robot(int id) {
        this.id = id;
    }

    /**
     * Every robot will be able to send a request to the factory if it sees an enemy and needs an attacking
     * robot produced
     * @return If the request was successfully sent to the factory
     */
    public boolean sendRequestToFactory() {
        return true;
    }

    /**
     * Abstract method that needs to be implemented for each unit that is a Robot. Workers will add tasks to
     * the queue differently from attacking robots.
     * @param task The task a robot is assigned to do
     * @return If the task was successfully assigned to the robots task queue
     */
    public abstract boolean addTaskToQueue(Task task);
}
