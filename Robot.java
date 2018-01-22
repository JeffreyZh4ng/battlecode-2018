import bc.*;

import java.util.*;
import java.util.LinkedList;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends UnitInstance {

    public Stack<MapLocation> movePathStack = null;

    public Robot(int id) {
        super(id);
    }

    public Stack<MapLocation> getMovePathStack() {
        return movePathStack;
    }


    /**
     * Checks if location both passable and appears not to have robots in it
     * @param map The map to check
     * @param location The location to check
     * @return If the location appears empty
     */
    public static boolean doesLocationAppearEmpty(PlanetMap map, MapLocation location) {

        // Returns if location is onMap, passableTerrain, and if it appears unoccupied by a Unit
        return map.onMap(location) && map.isPassableTerrainAt(location) > 0 &&
                (!Player.gc.canSenseLocation(location) || !Player.gc.hasUnitAtLocation(location));
    }


    public void setMovePathStack(Stack<MapLocation> movePathStack) {
        this.movePathStack = movePathStack;
    }

    /**
     * A move method manager that will analyze the path of other robots to see when this one should move
     * @param destinationLocation The destination location of this robot
     * @return If the robot has completed the task or not
     */
    public boolean pathManager(MapLocation destinationLocation) {
        if (move(this.getId(), destinationLocation)) {
            if (movePathStack == null) {
                System.out.println("Attacker: " + this.getId() + " Could not find a path to destination!");
                return true;
            } else {
                movePathStack.pop();
            }
        }

        return false;
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

        // if path should be recalculated
        if (movePathStack == null) {
            movePathStack = getPathFromBFS(this.getLocation(), destinationLocation, Player.gc.startingMap(Player.gc.planet()));
            if (movePathStack == null) {
                System.out.println("Cannot get to location: " + destinationLocation.toString());
                return true;
            }
        }

        // If this robot is covering destination, try to move it to an adjacent square. If it moves it is done moving
        if (Player.gc.unit(robotId).location().mapLocation().equals(destinationLocation)) {

            for (Direction direction : Player.getMoveDirections()) {
                if (Player.gc.canMove(robotId, direction)) {
                    Player.gc.moveRobot(robotId, direction);
                    movePathStack = null;
                    return true;
                }
            }

            return false;
        }

        // If adjacent to destination, the robot has finished moving
        // TODO: Check?
        if (Player.gc.unit(robotId).location().mapLocation().isAdjacentTo(destinationLocation) || movePathStack.empty()) {
            movePathStack = null;
            return true;
        }

        if (Player.gc.canMove(robotId, this.getLocation().directionTo(movePathStack.peek()))) {
            Player.gc.moveRobot(robotId, this.getLocation().directionTo(movePathStack.peek()));
            System.out.println("Attacker: " + this.getId() + " Moved!");
            return true;
        } else {
            System.out.println("Waiting");
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
    public Stack<MapLocation> getPathFromBFS(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startingLocation);

        HashMap<String, MapLocation> checkedLocations = new HashMap<>();
        checkedLocations.put(startingLocation.toString(), startingLocation);

        while (!frontier.isEmpty()) {

            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection : Player.getMoveDirections()) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (Player.doesLocationAppearEmpty(map, nextLocation) && !checkedLocations.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    checkedLocations.put(nextLocation.toString(), currentLocation);

                    if (currentLocation.isAdjacentTo(destinationLocation)) {
                        frontier.clear();
                    }
                }
            }
        }

        //find shortest of paths to adjacent locations and save shortest one
        MapLocation shortestNeighborLocation = null;
        Stack<MapLocation> shortestPath = new Stack<>();

        for (Direction directionFromDestination : Player.getMoveDirections()) {

            MapLocation neighborLocation = destinationLocation.add(directionFromDestination);
            if (checkedLocations.containsKey(neighborLocation.toString()) && Player.doesLocationAppearEmpty(map, neighborLocation)) {

                Stack<MapLocation> currentPath = new Stack<>();
                MapLocation currentTraceLocation = neighborLocation;

                //trace back path
                while (!currentTraceLocation.equals(startingLocation)) {
                    currentPath.push(currentTraceLocation);
                    currentTraceLocation = checkedLocations.get(currentTraceLocation.toString());

                    if (currentTraceLocation == null) {
                        break;
                    }
                    if (currentTraceLocation.isAdjacentTo(destinationLocation)) {
                        currentPath.clear();
                        currentPath.push(currentTraceLocation);
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

//    /**
//     * Sets emergency task to move robot to explore invisible territory
//     */
//    public void explore() {
//        ArrayList<MapLocation> wanderPath = getExplorePath(Player.gc.unit(this.getId()).location().mapLocation(),Player.gc.startingMap(Player.gc.planet()));
//        if (wanderPath != null) {
//            movePathStack = wanderPath;
//            this.setEmergencyTask(new RobotTask(-1, Command.MOVE, wanderPath.get(wanderPath.size() - 1)));
//        }
//    }
//
//    /**
//     * For when a robot has nothing to do, should move around so that it finds tasks or gain information this method
//     * finds a location to explore
//     * @return A random location that seems good to be explored
//     */
//    public MapLocation getLocationToExplore() {
//        PlanetMap initialMap = Player.gc.startingMap(Player.gc.planet());
//        MapLocation randomLocation = getRandomLocation(initialMap);
//
//        //give up after a certain number of tries
//        int tries = 0;
//        while (Player.gc.canSenseLocation(randomLocation) && !(initialMap.isPassableTerrainAt(randomLocation) > 0) && tries < 100) {
//            randomLocation = getRandomLocation(initialMap);
//            tries++;
//        }
//        return randomLocation;
//    }
//
//    /**
//     * Randomly chooses a location
//     * @param map The map that the location should be on
//     * @return A random location on the map
//     */
//    private static MapLocation getRandomLocation(PlanetMap map) {
//        return new MapLocation(map.getPlanet(), (int)(Math.random()*map.getWidth()),(int)(Math.random()*map.getHeight()));
//    }
//
//    /**
//     * Fill this out john
//     * @param startingLocation
//     * @param map
//     * @return
//     */
//    public ArrayList<MapLocation> getExplorePath(MapLocation startingLocation, PlanetMap map) {
//
//        ArrayList<Direction> moveDirections = Player.getMoveDirections();
//
//        //shuffle directions so that wandering doesn't gravitate towards a specific direction
//
//
//        MapLocation destinationLocation = null;
//        Queue<MapLocation> frontier = new LinkedList<>();
//        frontier.add(startingLocation);
//        HashMap<String, MapLocation> cameFrom = new HashMap<>();
//        cameFrom.put(startingLocation.toString(), startingLocation);
//
//        while (!frontier.isEmpty()) {
//
//            // Get next direction to check around
//            MapLocation currentLocation = frontier.poll();
//            Collections.shuffle(moveDirections, new Random());
//            // Check if locations around frontier location have already been added to came from and if they are empty
//            for (Direction nextDirection : moveDirections) {
//                MapLocation nextLocation = currentLocation.add(nextDirection);
//
//                if (doesLocationAppearEmpty(map, nextLocation) && !cameFrom.containsKey(nextLocation.toString())) {
//                    frontier.add(nextLocation);
//                    cameFrom.put(nextLocation.toString(), currentLocation);
//                    if (!Player.gc.canSenseLocation(currentLocation)) {
//                        frontier.clear();
//                        destinationLocation = currentLocation;
//                    }
//                }
//            }
//        }
//
//
//        if (destinationLocation == null) {
//            return null;
//        }
//        ArrayList<MapLocation> newPath = new ArrayList<>();
//
//        ArrayList<MapLocation> currentPath = new ArrayList<>();
//        MapLocation currentTraceLocation = destinationLocation;
//
//        //trace back path
//        while (!currentTraceLocation.equals(startingLocation)) {
//            newPath.add(0, currentTraceLocation);
//            currentTraceLocation = cameFrom.get(currentTraceLocation.toString());
//            if (currentTraceLocation == null) {
//                break;
//            }
//        }
//
//        return newPath;
//    }
//
//    /**
//     * Make worker wander to a random location within its vision radius
//     */
//    public void wander() {
//        ArrayList<MapLocation> wanderPath = getWanderPath(Player.gc.unit(this.getId()).location().mapLocation(),Player.gc.startingMap(Player.gc.planet()));
//        if (wanderPath != null) {
//            path = wanderPath;
//            this.setCurrentTask(new RobotTask(-1, Command.MOVE, wanderPath.get(wanderPath.size() - 1)));
//        }
//        System.out.println("no wander Location");
//    }
}


