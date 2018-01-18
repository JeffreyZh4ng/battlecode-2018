import bc.*;

import java.util.*;
import java.util.LinkedList;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends UnitInstance {

    private boolean inCombat;

    public ArrayList<MapLocation> path = null;

    public Robot(int id) {
        super(id);
    }

    public boolean isInCombat() {
        return this.inCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
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

    /**
     * Helper method that will return all the directions around the robot except fo the center
     * @return All the directions except for the center
     */
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
     * @param destinationLocation
     * @return if the robot has reached within on square of its destination or cannot get to destination at all
     */
    public boolean move(int robotId, MapLocation destinationLocation) {
        System.out.println("moving robot: " + robotId+"towards dest: " + destinationLocation + "from: ");

        // If this robot is covering destination
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
        // If adjacent to destination, done
        if (Player.gc.unit(robotId).location().mapLocation().isAdjacentTo(destinationLocation)) {
            return true;
        }

        // TODO: logic from this point on is messy but seems to work, should be cleaned up

        MapLocation locationToMoveTo;
        Direction directionToMove = null;
        if(path != null && path.size() > 0) {
            locationToMoveTo = path.get(0);
            directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
        } else {
            path = getNextForBreadthFirstSearch(Player.gc.unit(robotId).location().mapLocation(), destinationLocation,
                    Player.gc.startingMap(Player.gc.planet()));
            if (path == null) {
                System.out.println("cannot get within 1 square of destination or is already at destination/within 1 square");
                return true;
            }
            locationToMoveTo = path.get(0);
            directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);

        }

        // If cannot move along path, recalculate
        if (path != null && !Player.gc.canMove(robotId, directionToMove)) {
            System.out.println("recalculating path");
            path = getNextForBreadthFirstSearch(Player.gc.unit(robotId).location().mapLocation(), destinationLocation,
                    Player.gc.startingMap(Player.gc.planet()));
        }


        // If no location to move to, return true
        if (path == null) {
            System.out.println("cannot get within 1 square of destination or is already at destination/within 1 square");
            return true;
        }
        locationToMoveTo = path.get(0);
        directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);

        // Try to move to location
        if (Player.gc.canMove(robotId, directionToMove)) {
            Player.gc.moveRobot(robotId, directionToMove);
            path.remove(0);
        }

        return false;
    }

    /**
     * Uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation Current location of object to move
     * @param destinationLocation The location that you want to move to
     * @param map The map of earth or mars
     * @return The next place to step
     */
    public static ArrayList<MapLocation> getNextForBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {

        ArrayList<Direction> moveDirections = getMoveDirections();

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startingLocation);
        HashMap<String, MapLocation> cameFrom = new HashMap<>();
        cameFrom.put(startingLocation.toString(), startingLocation);

        while (!frontier.isEmpty()) {

            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have alredy been added to came from and if they are empty
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (doesLocationAppearEmpty(map, nextLocation) && !cameFrom.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    cameFrom.put(nextLocation.toString(), currentLocation);
                }
            }
        }


        ArrayList<MapLocation> path = new ArrayList<>();
        MapLocation currentLocation = destinationLocation;
        // If could not path to check if already one away from destination else find adjacent destination
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

        // Trace back from destination to start
        if (currentLocation == null) {
            return null;
        }
        while (!currentLocation.equals(startingLocation)) {
            path.add(0, currentLocation);
            currentLocation = cameFrom.get(currentLocation.toString());
            if (currentLocation == null) {
                return null;
            }
        }
        return path;
    }
}


