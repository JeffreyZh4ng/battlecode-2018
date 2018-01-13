package robots;

import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;
import commandsAndRequests.Globals;
import commandsAndRequests.Task;
import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot {

    private int id;
    public PriorityQueue<Task> robotTaskQueue;

    private static final Direction[] moveDirections = {Direction.North, Direction.Northeast, Direction.East, Direction.Southeast, Direction.South, Direction.Southwest, Direction.West, Direction.Northwest};
    PlanetMap initialEarthMap = Globals.gameController.startingMap(Planet.Earth);

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
     * Abstract method that needs to be implemented for each unit that is a Robot. Workers will add tasks to
     * the queue differently from attacking robots.
     * @param task The task a robot is assigned to do
     * @return If the task was successfully assigned to the robots task queue
     */
    public abstract void addTaskToQueue(Task task);


    /**
     * finds next optimal locations for each robot to move to and moves them to that location
     */
    public static void moveWorkers() {
        //TODO: find optimal next locations, consider if robot in path is moving, find optimal order of execution, execute moves
        //for now will find path bassed only on impassable object and move immediately
    }

    /**
     * uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation current location of object to move
     * @param destinationLocation
     * @param map
     * @return the next place to step
     */
    public static MapLocation getNextForBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {
        try {
            Queue<MapLocation> frontier = new PriorityQueue<>();
            frontier.add(startingLocation);
            HashMap<MapLocation,MapLocation> came_from = new HashMap<>();
            came_from.put(startingLocation, null);

            while(!frontier.isEmpty()) {
                MapLocation currentLocation = frontier.poll();
                for(Direction nextDirection : moveDirections) {
                    MapLocation nextLocation = currentLocation.add(nextDirection);
                    /*if(map.onMap(nextLocation) && map.isPassableTerrainAt(nextLocation)&& came_from.get(nextLocation)!=null) {
                        frontier.add(nextLocation);
                        came_from.put(nextLocation,currentLocation);
                    }*/
                }
            }
            MapLocation resultLocation = null;
            MapLocation currentLocation = destinationLocation;
            while(currentLocation != startingLocation) {
                resultLocation = currentLocation;
                currentLocation = came_from.get(currentLocation);
            }
            return resultLocation;
        } catch (Exception error) {
            System.out.println(error);
        }
        return null;
    }
}


