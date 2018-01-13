package units;

import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;
import commandsAndRequests.Globals;
import commandsAndRequests.GlobalTask;
import commandsAndRequests.RobotTask;

import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends Unit{

    public RobotTask emergencyTask = null;
    public PriorityQueue<GlobalTask> robotTaskQueue;

    private static final PlanetMap initialEarthMap = Globals.gameController.startingMap(Planet.Earth);

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
     * checks if location both passable and appears not to have robots in it
     * @param map
     * @param location
     * @return
     */
    public static boolean doesLocationAppearEmpty(PlanetMap map, MapLocation location) {
        return map.onMap(location) && map.isPassableTerrainAt(location) == 1 &&
                (!Globals.gameController.canSenseLocation(location) || !Globals.gameController.hasUnitAtLocation(location));
    }


    /**
     *
     * @return all directions besides center
     */
    public static Direction[] getMoveDirections() {
        Direction[] moveDirections = new Direction[8];
        for (int i = 0; i < 8; i++) {
            moveDirections[i] = Direction.swigToEnum(i+1);
        }
        return moveDirections;
    }

    /**
     *
     * @param robotId robot to move
     * @param destinationLocation
     * @return if the robot has reached within on square of its destination or cannot get to destination at all
     */
    public boolean move(int robotId, MapLocation destinationLocation) {
        MapLocation locationToMoveTo = getNextForBreadthFirstSearch(Globals.gameController.unit(robotId).location().mapLocation(), destinationLocation, initialEarthMap);
        System.out.println(Globals.gameController.unit(robotId).location().mapLocation());
        System.out.println(locationToMoveTo);
        if (locationToMoveTo==null) {
            System.out.println("cannot get within1square of destination or is already at destination/within 1 square");
            return true;
        }
        Direction directionToMove = Globals.gameController.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
        if (Globals.gameController.canMove(robotId, directionToMove)) {
            Globals.gameController.moveRobot(robotId, directionToMove);
        }
        return false;
    }

    /**
     * uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation current location of object to move
     * @param destinationLocation
     * @param map
     * @return the next place to step
     */
    public static MapLocation getNextForBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {

        Direction[] moveDirections = getMoveDirections();

        Queue<MapLocation> frontier = new LinkedBlockingQueue<>();
        frontier.add(startingLocation);
        HashMap<String, MapLocation> cameFrom = new HashMap<>();
        cameFrom.put(startingLocation.toString(), startingLocation);

        while (!frontier.isEmpty()) {
            System.out.println("frintier not empty: " + frontier.size());
            MapLocation currentLocation = frontier.poll();
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if ( doesLocationAppearEmpty(map, nextLocation)&& !cameFrom.containsKey(nextLocation.toString())) {
                    System.out.println("adding to frinter: "+nextLocation);
                    frontier.add(nextLocation);
                    cameFrom.put(nextLocation.toString(), currentLocation);
                }
            }
        }
        MapLocation resultLocation = null;
        MapLocation currentLocation = destinationLocation;
        if (!cameFrom.containsKey(destinationLocation.toString())) {
            for (Direction moveDirection : moveDirections) {
                if (doesLocationAppearEmpty(map, destinationLocation.add(moveDirection))) {
                    currentLocation = destinationLocation.add(moveDirection);
                }
            }
        }
        while (!currentLocation.equals(startingLocation)) {
            System.out.println("not equal: " + currentLocation + "dest: " + startingLocation);
            resultLocation = currentLocation;
            currentLocation = cameFrom.get(currentLocation.toString());
        }
        return resultLocation;

        //return null;
    }
}


