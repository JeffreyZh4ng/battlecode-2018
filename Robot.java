import bc.*;

import java.util.*;
import java.util.LinkedList;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends UnitInstance {

    private RobotTask emergencyTask = null;

    public Robot(int id) {
        super(id);
    }

    public RobotTask getEmergencyTask() {
        return emergencyTask;
    }

    public void setEmergencyTask(RobotTask emergencyTask) {
        this.emergencyTask = emergencyTask;
    }

    /**
     * Method that will add an attack target to the queue if it is seen
     * @param robotId The id of the robot sensing
     * @param radius The radius of the sensing robot
     */
    public void senseArea(int robotId, int radius) {
        MapLocation currentLocation = Player.gc.unit(robotId).location().mapLocation();
        VecMapLocation locations = Player.gc.allLocationsWithin(currentLocation, radius);

        for (int i = 0; i < locations.size(); i++) {
            MapLocation location = locations.get(i);
            if (Player.gc.hasUnitAtLocation(location)) {
                if (Player.gc.senseUnitAtLocation(location).team() != Player.gc.team()) {
                    Unit enemy = Player.gc.senseUnitAtLocation(location);
                    AttackTarget newTarget = new AttackTarget(enemy.id(), location);
                    Earth.earthAttackTargetsMap.put(enemy.id(), newTarget);
                }
            }
        }
    }

//    /**
//     * For when a robot has nothing to do, should move around so that it finds tasks or gain information this method
//     * finds a location to explore
//     * @return A random location that seems good to be explored
//     */
//    public static MapLocation getLocationToExplore() {
//        PlanetMap initialMap = Player.gc.startingMap(Player.gc.planet());
//        MapLocation randomLocation = getRandomLocation(initialMap);
//
//        //give up after a certain number of tries
//        int tries = 0;
//        while (Player.gc.canSenseLocation(randomLocation) && !(initialMap.isPassableTerrainAt(randomLocation) > 0) && tries < 100) {
//            randomLocation = getRandomLocation(initialMap);
//        }
//        return randomLocation;
//    }

//    /**
//     * Randomly chooses a location
//     * @param map The map that the location should be on
//     * @return A random location on the map
//     */
//    private static MapLocation getRandomLocation(PlanetMap map) {
//        return new MapLocation(map.getPlanet(), (int)(Math.random()*map.getWidth()),(int)(Math.random()*map.getHeight()));
//    }
//
//    public boolean moveTowardsDestination(int robotId, MapLocation destinationLocation) {
//
//        System.out.println("moving robot: " + robotId);
//
//        //get optimal location to move to
//        HashSet<MapLocation> locationsToMoveTo = getNextLocationsFromDepthMap(Player.gc.unit(robotId).location().mapLocation(), destinationLocation);
//
//        //if no location to move to, return true
//        if (locationsToMoveTo.size() == 0) {
//            System.out.println("locations to moveto size is 0");
//            return true;
//        }
//
//        //try to move to location
//        Direction directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationsToMoveTo.iterator().next());
//        if (Player.gc.canMove(robotId, directionToMove)) {
//            Player.gc.moveRobot(robotId, directionToMove);
//        }
//
//        return false;
//    }

//    /**
//     * Gets the next locations to move to in order to get to finalLocation
//     * @param initialLocation The current location of the unit to be moved
//     * @param finalLocation The target destination
//     * @return The next optimal location options to move to
//     */
//    public HashSet<MapLocation> getNextLocationsFromDepthMap(MapLocation initialLocation, MapLocation finalLocation) {
//
//        //get the depth map
//        HashMap<Integer, HashSet<MapLocation>> depthMap = getDepthMap(initialLocation, finalLocation);
//
//        //trace back path
//        HashSet<MapLocation> locationsOnPath = new HashSet<>();
//        System.out.println(depthMap.size());
//        for (int i = 0; i < depthMap.size(); i++) {
//
//            //find the integer value of the final location
//            if (depthMap.get(i).contains(finalLocation)) {
//                locationsOnPath.add(finalLocation);
//                System.out.println("i is:" + i);
//                while (i>=0) {
//                    i--;
//                    HashSet<MapLocation> nextLocationsOnPath = new HashSet<>();
//                    for (MapLocation location : locationsOnPath) {
//                        nextLocationsOnPath.addAll(getAdjacentLocationsFromHashSet(depthMap.get(i),location));
//                    }
//                    locationsOnPath = nextLocationsOnPath;
//                }
//            }
//        }
//        return locationsOnPath;
//    }

//    /**
//     * Abstract method that needs to be implemented for each unit that is a Robot. Workers will add tasks to
//     * the queue differently from attacking robots.
//     * @param task The task a robot is assigned to do
//     */
//    public abstract void addTaskToQueue(GlobalTask task);

    /**
     * checks if location both passable and appears not to have robots in it
     * @param map the map to check
     * @param location the location to check
     * @return if the location appears empty
     */
    public static boolean doesLocationAppearEmpty(PlanetMap map, MapLocation location) {

        //returns if location is onMap, passableTerrain, and if it appears unocupied by a Unit
        return map.onMap(location) && map.isPassableTerrainAt(location) == 1 &&
                (!Player.gc.canSenseLocation(location) || !Player.gc.hasUnitAtLocation(location));
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
        System.out.println("moving robot: " + robotId+"towards dest: " + destinationLocation + "from: " + Player.gc.unit(robotId).location().mapLocation());

        //if covering destination
        if (Player.gc.unit(robotId).location().mapLocation().equals(destinationLocation)) {
            for (Direction direction : getMoveDirections()) {
                if (Player.gc.canMove(robotId, direction)) {
                    Player.gc.moveRobot(robotId, direction);
                    return true;
                }
                System.out.println("IM Stuck!");
            }
            return false;

        }
        //check if adjacent
        if (Player.gc.unit(robotId).location().mapLocation().isAdjacentTo(destinationLocation)) {
            return true;
        }

        //get optimal location to move to
        MapLocation locationToMoveTo = getNextForBreadthFirstSearch(Player.gc.unit(robotId).location().mapLocation(),
                destinationLocation, Player.gc.startingMap(Player.gc.planet()));

        //if no location to move to, return true
        if (locationToMoveTo == null) {
            System.out.println("cannot get within 1 square of destination or is already at destination/within 1 square");
            return true;
        }

        //try to move to location
        Direction directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
        if (Player.gc.canMove(robotId, directionToMove)) {
            Player.gc.moveRobot(robotId, directionToMove);
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

        Queue<MapLocation> frontier = new LinkedList<>();
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


