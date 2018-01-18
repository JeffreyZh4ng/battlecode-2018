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
        System.out.println("moving robot: " + robotId+"towards dest: " + destinationLocation + "from: ");

        //if this robot is covering destination
        if (Player.gc.unit(robotId).location().mapLocation().equals(destinationLocation)) {
            for (Direction direction : getMoveDirections()) {
                if (Player.gc.canMove(robotId, direction)) {
                    Player.gc.moveRobot(robotId, direction);
                    return true;
                }
            }
            System.out.println("IM Stuck!");
            //TODO: maybe should return true here not sure
            return false;

        }
        //if adjacent to destination, done
        if (Player.gc.unit(robotId).location().mapLocation().isAdjacentTo(destinationLocation)) {
            return true;
        }

        //TODO: logic from this point on is messy but seems to work, should be cleaned up


        //TODO: does it get the path if destination is covered????
        // if path should be recalculated
        if (path == null || path.size() == 0 || !path.get(path.size()-1).equals(destinationLocation)
                || !Player.gc.canMove(robotId, this.getLocation().directionTo(path.get(0)))) {
            path = getPathFromBreadthFirstSearch(this.getLocation(), destinationLocation, Player.gc.startingMap(Player.gc.planet()));
        }

        if (path != null && path.size() >0 && Player.gc.canMove(robotId, this.getLocation().directionTo(path.get(0)))) {
            Player.gc.moveRobot(robotId, this.getLocation().directionTo(path.get(0)));
            path.remove(0);
            return false;
        } else {
            System.out.println("cannot get to destination");
            return true;
        }






//        MapLocation locationToMoveTo;
//        Direction directionToMove = null;
//        if(path != null && path.size() > 0) {
//            locationToMoveTo = path.get(0);
//            directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
//        } else {
//            path = getPathFromBreadthFirstSearch(Player.gc.unit(robotId).location().mapLocation(), destinationLocation,
//                    Player.gc.startingMap(Player.gc.planet()));
//            if (path == null) {
//                System.out.println("cannot get within 1 square of destination or already is");
//                return true;
//            }
//            locationToMoveTo = path.get(0);
//            directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
//
//        }
//
//        //if cannot move along path, recalculate
//        if (path != null && !Player.gc.canMove(robotId, directionToMove)) {
//            System.out.println("recalculating path");
//            path = getPathFromBreadthFirstSearch(Player.gc.unit(robotId).location().mapLocation(), destinationLocation,
//                    Player.gc.startingMap(Player.gc.planet()));
//        }
//
//
//        //if no location to move to, return true
//        if (path == null) {
//            System.out.println("cannot get within 1 square of destination or is already at destination/within 1 square");
//            return true;
//        }
//        locationToMoveTo = path.get(0);
//        directionToMove = Player.gc.unit(robotId).location().mapLocation().directionTo(locationToMoveTo);
//
//        //try to move to location
//        if (Player.gc.canMove(robotId, directionToMove)) {
//            Player.gc.moveRobot(robotId, directionToMove);
//            path.remove(0);
//        }
//
//        return false;
    }

    /**
     * uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation current location of object to move
     * @param destinationLocation
     * @param map
     * @return the next place to step
     */
    public static ArrayList<MapLocation> getPathFromBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {

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


        ArrayList<MapLocation> path = new ArrayList<>();
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
            path.add(0,currentLocation);
            currentLocation = cameFrom.get(currentLocation.toString());
            if (currentLocation == null) {
                return null;
            }
        }
        return path;
    }



    /**
     * For when a robot has nothing to do, should move around so that it finds tasks or gain information this method
     * finds a location to explore
     * @return A random location that seems good to be explored
     */
    public static MapLocation getLocationToExplore() {
        PlanetMap initialMap = Player.gc.startingMap(Player.gc.planet());
        MapLocation randomLocation = getRandomLocation(initialMap);

        //give up after a certain number of tries
        int tries = 0;
        while (Player.gc.canSenseLocation(randomLocation) && !(initialMap.isPassableTerrainAt(randomLocation) > 0) && tries < 100) {
            randomLocation = getRandomLocation(initialMap);
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


