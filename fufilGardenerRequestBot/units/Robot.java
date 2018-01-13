package units;

import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;
import commandsAndRequests.Globals;
import commandsAndRequests.GlobalTask;
import commandsAndRequests.RobotTask;
import planets.Earth;

import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends Unit{

    public RobotTask emergencyTask = null;

    /**
     * Constructor that will set the id of the robot when it is created
     * @param id The id of the robot
     */
    public Robot(int id) {
        super(id);
    }

    /**
     * Every robot will be able to send a request to the factory if it sees an enemy and needs an attacking
     * robot produced
     */
    public void sendRequestToFactory() {
        // TODO: Need to write an algorithm that will detect how many enemies are nearby. Based on this number,
        // TODO: the robot will send a request to the nearest factory to produce "X" number of attacking units.
    }

//    /**
//     * Abstract method that needs to be implemented for each unit that is a Robot. Workers will add tasks to
//     * the queue differently from attacking robots.
//     * @param task The task a robot is assigned to do
//     */
//    public abstract void addTaskToQueue(GlobalTask task);
//
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
    /*public static void moveWorkers() {
        System.out.println("moving workers");
        //TODO: find optimal next locations, consider if robot in path is moving, find optimal order of execution, run moves
        //for now will find path bassed only on impassable object and move immediately
        for (int workerId: Earth.earthWorkerMap.keySet()) {
            if(Globals.gameController.unit(workerId).movementHeat() < 10) {
                MapLocation locationToMoveTo = getNextForBreadthFirstSearch(Globals.gameController.unit(workerId).location().mapLocation(), Earth.earthWorkerMap.get(workerId).destinationLocation, initialEarthMap);
                System.out.println(Globals.gameController.unit(workerId).location().mapLocation());
                System.out.println(locationToMoveTo);
                Direction directionToMove = Globals.gameController.unit(workerId).location().mapLocation().directionTo(locationToMoveTo);
                if (Globals.gameController.canMove(workerId, directionToMove)) {
                    Globals.gameController.moveRobot(workerId, directionToMove);
                }
            }
        }
    }*/


    private static final Direction[] moveDirections = {Direction.North, Direction.Northeast, Direction.East, Direction.Southeast, Direction.South, Direction.Southwest, Direction.West, Direction.Northwest};
    private static final PlanetMap initialEarthMap = Globals.gameController.startingMap(Planet.Earth);
    private MapLocation destinationLocation = new MapLocation(Planet.Earth,0,0); // will be chagned latter

    /**
     * uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation current location of object to move
     * @param destinationLocation
     * @param map
     * @return the next place to step
     */
    public static MapLocation getNextForBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {

        Queue<MapLocation> frontier = new LinkedBlockingQueue<>();
        frontier.add(startingLocation);
        HashMap<MapLocation, MapLocation> came_from = new HashMap<>();
        came_from.put(startingLocation, startingLocation);

        while (!frontier.isEmpty()) {
            System.out.println("frintier not empty: " + frontier.size());
            MapLocation currentLocation = frontier.poll();
            for (Direction nextDirection : moveDirections) {
                //System.out.println("checking direction: " + nextDirection);
                MapLocation nextLocation = currentLocation.add(nextDirection);
                //System.out.println("nextLocation: " + nextLocation);
                if (map.onMap(nextLocation) && map.isPassableTerrainAt(nextLocation) == 1 && (!Globals.gameController.canSenseLocation(nextLocation) || !Globals.gameController.hasUnitAtLocation(nextLocation)) && !came_from.containsKey(nextLocation)) {//probaly broken becuase of java object comparison stuff
                    System.out.println("adding to frinter: "+nextLocation);
                    frontier.add(nextLocation);
                    came_from.put(nextLocation, currentLocation);
                }
            }
        }
        MapLocation resultLocation = null;
        MapLocation currentLocation = startingLocation;
        while (!currentLocation.equals(startingLocation)) {
            System.out.println("not equal: " + currentLocation + "dest: " + startingLocation);
            resultLocation = currentLocation;
            currentLocation = came_from.get(currentLocation);
        }
        return resultLocation;

        //return null;
    }
}


