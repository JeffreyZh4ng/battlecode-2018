import bc.*;

import java.util.*;
import java.util.LinkedList;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends UnitInstance {

    public ArrayList<MapLocation> path = null;
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
     * Make worker wander to a random location within its vision radius
     */
    public void wander() {
        ArrayList<MapLocation> wanderPath = getWanderPath(Player.gc.unit(this.getId()).location().mapLocation(),Player.gc.startingMap(Player.gc.planet()));
        if (wanderPath != null) {
            path = wanderPath;
            emergencyTask = new RobotTask(-1, Command.MOVE, wanderPath.get(wanderPath.size() - 1));
        }
        System.out.println("no wander Location");
    }

    public ArrayList<MapLocation> getWanderPath(MapLocation startingLocation, PlanetMap map) {

        ArrayList<Direction> moveDirections = getMoveDirections();

        //shuffle directions so that wandering doesn't gravitate towards a specific direction
        Collections.shuffle(moveDirections, new Random());

        MapLocation destinationLocation = null;
        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startingLocation);
        HashMap<String, MapLocation> cameFrom = new HashMap<>();
        cameFrom.put(startingLocation.toString(), startingLocation);

        while (!frontier.isEmpty()) {

            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (doesLocationAppearEmpty(map, nextLocation) && !cameFrom.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    cameFrom.put(nextLocation.toString(), currentLocation);
                    if (!Player.gc.canSenseLocation(currentLocation)) {
                        frontier.clear();
                        destinationLocation = currentLocation;
                    }
                }
            }
        }


        if (destinationLocation == null) {
            return null;
        }
        ArrayList<MapLocation> newPath = new ArrayList<>();

        ArrayList<MapLocation> currentPath = new ArrayList<>();
        MapLocation currentTraceLocation = destinationLocation;

        //trace back path
        while (!currentTraceLocation.equals(startingLocation)) {
            newPath.add(0, currentTraceLocation);
            currentTraceLocation = cameFrom.get(currentTraceLocation.toString());
            if (currentTraceLocation == null) {
                break;
            }
        }


        return newPath;
    }

    /**
     * Checks if location both passable and appears not to have robots in it
     * @param map The map to check
     * @param location The location to check
     * @return If the location appears empty
     */
    public boolean doesLocationAppearEmpty(PlanetMap map, MapLocation location) {

        // Returns if location is onMap, passableTerrain, and if it appears unoccupied by a Unit
        return map.onMap(location) && map.isPassableTerrainAt(location) > 0 &&
                (!Player.gc.canSenseLocation(location) || !Player.gc.hasUnitAtLocation(location));
    }

    /**
     * Helper method that will return all the directions around the robot except fo the center
     * @return All the directions except for the center
     */
    // TODO: If you want to make this static, put it in the Player class
    public static ArrayList<Direction> getMoveDirections() {
        ArrayList<Direction> directions = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            directions.add( Direction.swigToEnum(i));
        }
        return directions;
    }

    /**
     * Move a robot
     * @param robotId robot to move
     * @param destinationLocation The destination location a unit
     * @return if the robot has reached within on square of its destination or cannot get to destination at all
     */
    public boolean move(int robotId, MapLocation destinationLocation) {
        if (!Player.gc.isMoveReady(this.getId())) {
            return false;
        }
        // If this robot is covering destination
        if (Player.gc.unit(robotId).location().mapLocation().equals(destinationLocation)) {
            for (Direction direction : getMoveDirections()) {
                if (Player.gc.canMove(robotId, direction)) {
                    Player.gc.moveRobot(robotId, direction);
                    return true;
                }
            }
            return false;

        }
        // If adjacent to destination, done
        if (Player.gc.unit(robotId).location().mapLocation().isAdjacentTo(destinationLocation)) {
            return true;
        }

        // if path should be recalculated
        if (path == null || path.size() == 0 || !path.get(path.size()-1).equals(destinationLocation)
                || !Player.gc.canMove(robotId, this.getLocation().directionTo(path.get(0)))) {
            path = getPathFromBreadthFirstSearch(this.getLocation(), destinationLocation, Player.gc.startingMap(Player.gc.planet()));
        }

        if (path != null && path.size() > 0 && Player.gc.canMove(robotId, this.getLocation().directionTo(path.get(0)))) {
            Player.gc.moveRobot(robotId, this.getLocation().directionTo(path.get(0)));
            path.remove(0);
            return false;
        } else {
            System.out.println("cannot get to destination");
            return false;
        }
    }

    /**
     * Uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation Current location of object to move
     * @param destinationLocation The location that you want to move to
     * @param map The map of earth or mars
     * @return The next place to step
     */
    public ArrayList<MapLocation> getPathFromBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {

        ArrayList<Direction> moveDirections = getMoveDirections();

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startingLocation);
        HashMap<String, MapLocation> cameFrom = new HashMap<>();
        cameFrom.put(startingLocation.toString(), startingLocation);

        while (!frontier.isEmpty()) {

            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (doesLocationAppearEmpty(map, nextLocation) && !cameFrom.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    cameFrom.put(nextLocation.toString(), currentLocation);
                    if (currentLocation.isAdjacentTo(destinationLocation)) {
                        frontier.clear();
                    }
                }
            }
        }

        //find shortest of paths to adjacent locations and save shortest one
        MapLocation shortestNeighborLocation = null;

        ArrayList<MapLocation> shortestPath = new ArrayList<>();

        for (Direction directionFromDestination : moveDirections) {

            MapLocation neighborLocation = destinationLocation.add(directionFromDestination);
            if (cameFrom.containsKey(neighborLocation.toString()) && doesLocationAppearEmpty(map, neighborLocation)) {

                ArrayList<MapLocation> currentPath = new ArrayList<>();
                MapLocation currentTraceLocation = neighborLocation;

                //trace back path
                while (!currentTraceLocation.equals(startingLocation)) {
                    currentPath.add(0,currentTraceLocation);
                    currentTraceLocation = cameFrom.get(currentTraceLocation.toString());
                    if (currentTraceLocation == null) {
                        break;
                    }
                    if (currentTraceLocation.isAdjacentTo(destinationLocation)) {
                        currentPath.clear();
                        currentPath.add(currentTraceLocation);
                    }
                }

                if (shortestNeighborLocation == null || currentPath.size() < shortestPath.size()) {
                    shortestPath = currentPath;
                    shortestNeighborLocation = neighborLocation;
                }
            }
        }
        return shortestPath;
    }

    /**
     * For when a robot has nothing to do, should move around so that it finds tasks or gain information this method
     * finds a location to explore
     * @return A random location that seems good to be explored
     */
    public MapLocation getLocationToExplore() {
        PlanetMap initialMap = Player.gc.startingMap(Player.gc.planet());
        MapLocation randomLocation = getRandomLocation(initialMap);

        //give up after a certain number of tries
        int tries = 0;
        while (Player.gc.canSenseLocation(randomLocation) && !(initialMap.isPassableTerrainAt(randomLocation) > 0) && tries < 100) {
            randomLocation = getRandomLocation(initialMap);
            tries++;
        }
        return randomLocation;
    }

    /**
     * Randomly chooses a location
     * @param map The map that the location should be on
     * @return A random location on the map
     */
    private static MapLocation getRandomLocation(PlanetMap map) {
        return new MapLocation(map.getPlanet(), (int)(Math.random()*map.getWidth()),(int)(Math.random()*map.getHeight()));
    }
}


