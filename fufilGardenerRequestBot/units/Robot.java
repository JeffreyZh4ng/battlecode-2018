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
     * @param map the map to check
     * @param location the location to check
     * @return if the location appears empty
     */
    public static boolean doesLocationAppearEmpty(PlanetMap map, MapLocation location) {

        //returns if location is onMap, passableTerrain, and if it appears unocupied by a Unit
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
     * move a robot
     * @param robotId robot to move
     * @param destinationLocation
     * @return if the robot has reached within on square of its destination or cannot get to destination at all
     */
    public boolean move(int robotId, MapLocation destinationLocation) {

        //if can move this turn
        if (Globals.gameController.unit(robotId).movementHeat() < 10) {
            System.out.println("moving robot: " + robotId);

            //get optimal location to move to
            MapLocation locationToMoveTo = getNextForBreadthFirstSearch(Globals.gameController.unit(robotId).location().mapLocation(), destinationLocation, initialEarthMap);

            //if no location to move to, return true
            if (locationToMoveTo == null) {
                System.out.println("cannot get within 1 square of destination or is already at destination/within 1 square");
                return true;
            }

            //try to move to location
            Direction directionToMove = Globals.gameController.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
            if (Globals.gameController.canMove(robotId, directionToMove)) {
                Globals.gameController.moveRobot(robotId, directionToMove);
            }
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

        // while there are more locations to check
        while (!frontier.isEmpty()) {

            //get next direction to check around
            MapLocation currentLocation = frontier.poll();

            //check if locations around frontier location have alredy been added to came from and if they are empty
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (doesLocationAppearEmpty(map, nextLocation) && !cameFrom.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    cameFrom.put(nextLocation.toString(), currentLocation);
                }
            }
        }


        MapLocation resultLocation = null;
        MapLocation currentLocation = destinationLocation;
        //if could not path to check if already one away from destination else find adjacent destination
        if (!cameFrom.containsKey(destinationLocation.toString())) {
            if (startingLocation.isAdjacentTo(destinationLocation)) {
                return null;
            } else {
                for (Direction moveDirection : moveDirections) {
                    if (doesLocationAppearEmpty(map, destinationLocation.add(moveDirection))) {
                        currentLocation = destinationLocation.add(moveDirection);
                    }
                }
            }
        }

        //trace back from destination to start
        if (currentLocation == null) {
            return null;
        }
        while (!currentLocation.equals(startingLocation)) {
            resultLocation = currentLocation;
            currentLocation = cameFrom.get(currentLocation.toString());
            if (currentLocation == null) {
                return null;
            }
        }

        return resultLocation;
    }
}


