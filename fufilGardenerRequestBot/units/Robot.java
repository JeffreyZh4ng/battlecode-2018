package units;

import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;
import commandsAndRequests.Globals;
import commandsAndRequests.RobotTask;

import java.util.*;
import java.util.LinkedList;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends Unit {

    public RobotTask emergencyTask = null;
    private static final PlanetMap initialMap = Globals.gc.startingMap(Globals.gc.planet());

    public Robot(int id) {
        super(id);
    }

    /**
     * for when a robot has nothing to do, should move around so that it finds tasks or gain information this method
     * finds a location to explore
     * @return a random location that seems good to be explored
     */
    public static MapLocation getLocationToExplore() {
        MapLocation randomLocation = getRandomLocation(initialMap);

        //give up after a certain number of tries
        int tries = 0;
        while (Globals.gc.canSenseLocation(randomLocation)&& !(initialMap.isPassableTerrainAt(randomLocation)>0) && tries < 100) {
            randomLocation = getRandomLocation(initialMap);
        }
        return randomLocation;
    }

    /**
     * randomly choses a location
     * @param map the map that the location should be on
     * @return a random location on the map
     */
    private static MapLocation getRandomLocation(PlanetMap map) {
        return new MapLocation(map.getPlanet(), (int)(Math.random()*map.getWidth()),(int)(Math.random()*map.getHeight()));
    }

    /**
     * This method will get a depth map of all the shortest paths from an initial MapLocation to a final MapLocation
     * @param initialLocation The initial MapLocation
     * @param finalLocation The final MapLocation
     */
    public void getDepthMap(MapLocation initialLocation, MapLocation finalLocation)
    {
        HashMap<Integer, HashSet<MapLocation>> depthMap = new HashMap<>();

        HashSet<String> allLocations = new HashSet<>();
        allLocations.add(initialLocation.toString());

        depthMap = searchForPath(initialLocation, finalLocation, 1, depthMap, allLocations);
    }

    /**
     * A recursive function that will compile all the shortest paths between a given initial and final MapLocation
     * into a depth map. The map contains all points 'x' moves away from the initial position until the final position
     * To find the shortest paths, trace the index of the final position and find adjacent locations with an index of n-1
     * @param initialLocation The MapLocation that you are find a path to the finalLocation from
     * @param finalLocation The final MapLocation of the move
     * @param depthIndex An index that represents the number of moves you are away from the initial MapLocation
     * @param depthMap A map that stores all the paths as moves away from the initial MapLocation
     * @param allLocations A lookup map that keeps track of all the locations you have visited
     * @return A depth map of all the shortest paths
     */
    public HashMap<Integer, HashSet<MapLocation>> searchForPath(MapLocation initialLocation, MapLocation finalLocation, int depthIndex,
                                         HashMap<Integer, HashSet<MapLocation>> depthMap, HashSet<String> allLocations) {

        ArrayList<String> nextLocations = getAvailablePositions(initialLocation);

        if (initialLocation.isAdjacentTo(finalLocation)) {
            if (depthIndex < depthMap.size()) {
                for (int i = depthIndex; i < depthMap.size(); i++) {
                    depthMap.remove(i + 1);
                }
            }
            return depthMap;
        }
        if (nextLocations.size() == 0) {
            return depthMap;
        }
        if (depthIndex > depthMap.size()) {
            return depthMap;
        }

        for (String location: nextLocations) {
            if (!allLocations.contains(location)) {
                allLocations.add(location);

                HashSet<MapLocation> depthSet;
                if (depthMap.get(depthIndex) == null) {
                    depthSet = new HashSet<>();
                } else {
                    depthSet = depthMap.get(depthIndex);
                }

                depthSet.add(stringToMapLocation(location));
                depthMap.put(depthIndex, depthSet);
            }
        }

        for (String location: nextLocations) {
            MapLocation newLocation = stringToMapLocation(location);
            depthMap = searchForPath(newLocation, finalLocation, depthIndex + 1, depthMap, allLocations);
        }

        HashSet<MapLocation> finalDepthSet = new HashSet<>();
        finalDepthSet.add(finalLocation);
        depthMap.put(depthMap.size(), finalDepthSet);

        return depthMap;
    }

    /**
     * Method that will give all open adjacent positions to a given MapLocation
     * @param currentLocation The location you want to find adjacent location to
     * @return A list of all available adjacent positions
     */
    private ArrayList<String> getAvailablePositions(MapLocation currentLocation) {
        ArrayList<String> availablePositions = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            MapLocation locationToCheck = currentLocation.add(Direction.swigToEnum(i));
            PlanetMap earthMap = Globals.gc.startingMap(Planet.Earth);

            if (earthMap.onMap(locationToCheck) && earthMap.isPassableTerrainAt(locationToCheck) > 0) {
                if (!Globals.gc.canSenseLocation(locationToCheck) || !Globals.gc.hasUnitAtLocation(locationToCheck)) {
                    availablePositions.add(mapLocationToString(locationToCheck));
                }
            }
        }

        return availablePositions;
    }

    /**
     * Method that will convert a MapLocation into an easily recognizable string.
     * @param mapLocation The MapLocation that you want to convert
     * @return A string that represents the MapLocation
     */
    private static String mapLocationToString(MapLocation mapLocation) {
        StringBuilder convertedLocation = new StringBuilder();

        if (mapLocation.getPlanet() == Planet.Mars) {
            convertedLocation.append(" ");
        }
        convertedLocation.append(mapLocation.getX());
        convertedLocation.append(" ");
        convertedLocation.append(mapLocation.getY());

        return convertedLocation.toString();
    }

    /**
     * A method that will convert the recognizable string back into a MapLocation
     * @param location The MapLocation represented by the string
     * @return A MapLocation that represents the string
     */
    private static MapLocation stringToMapLocation(String location) {
        Planet mapPlanet;
        if (location.charAt(0) == ' ') {
            mapPlanet = Planet.Mars;
            location = location.substring(1);
        } else {
            mapPlanet = Planet.Earth;
        }

        int spaceIndex = location.indexOf(' ');
        int xLocation = Integer.parseInt(location.substring(0, spaceIndex));
        location = location.substring(spaceIndex + 1);
        int yLocation = Integer.parseInt(location);

        return new MapLocation(mapPlanet, xLocation, yLocation);
    }

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
                (!Globals.gc.canSenseLocation(location) || !Globals.gc.hasUnitAtLocation(location));
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
        if (Globals.gc.unit(robotId).movementHeat() < 10) {
            System.out.println("moving robot: " + robotId);

            //get optimal location to move to
            MapLocation locationToMoveTo = getNextForBreadthFirstSearch(Globals.gc.unit(robotId).location().mapLocation(), destinationLocation, initialMap);

            //if no location to move to, return true
            if (locationToMoveTo == null) {
                System.out.println("cannot get within 1 square of destination or is already at destination/within 1 square");
                return true;
            }

            //try to move to location
            Direction directionToMove = Globals.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
            if (Globals.gc.canMove(robotId, directionToMove)) {
                Globals.gc.moveRobot(robotId, directionToMove);
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


