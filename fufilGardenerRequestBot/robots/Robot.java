package robots;

import bc.Direction;
import commandsAndRequests.Globals;
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

//    /**
//     * Method that will return a random direction when called. Intended to be used for preliminary testing
//     * @return One of 8 random directions
//     */
//    public static Direction pickRandomDirection() {
//        int randomInt = (int)(Math.random()*8 + 1);
//        return Direction.swigToEnum(randomInt);
//    }
//
//    /**
//     * Subject to change. The isOccupiable method is bugged in the API
//     * @param robotId The id of the robot
//     * @return An available direction. Null if none are available
//     */
//    public static Direction returnAvailableDirection(int robotId) {
//        for (int i = 0; i < 8; i++) {
//            if (Globals.gameController.canMove(robotId, Direction.swigToEnum(i))) {
//                return Direction.swigToEnum(i);
//            }
//        }
//        return null;
//    }
//    /**
//     * Iterates through map locations and prints karbonite values for all locations
//     */
//    public static void printKarboniteValues() {
//        long width = gameController.startingMap(Planet.Earth).getWidth();
//        long height = gameController.startingMap(Planet.Earth).getHeight();
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                System.out.println(gameController.startingMap(Planet.Earth).initialKarboniteAt(new MapLocation(Planet.Earth, x, y)));
//            }
//        }
//    }

    /**
     * finds next optimal locations for each robot to move to and moves them to that location
     */
    public static void moveWorkers() {
        // find optimal next locations, consider if robot in path is moving, find optimal order of execution
        // execute move actions
    }

    /**
     * Abstract method that needs to be implemented for each unit that is a Robot. Workers will add tasks to
     * the queue differently from attacking robots.
     * @param task The task a robot is assigned to do
     * @return If the task was successfully assigned to the robots task queue
     */
    public abstract boolean addTaskToQueue(Task task);
}
