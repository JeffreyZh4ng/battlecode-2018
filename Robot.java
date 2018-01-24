import bc.*;

import java.util.*;
import java.util.LinkedList;

/**
 * Superclass of all robots that specifies actions that all robots will be able to make
 */
public abstract class Robot extends UnitInstance {

    private Stack<MapLocation> movePathStack = null;

    public Robot(int id) {
        super(id);
    }
    
    @Override
    public void pollCurrentTask() {
        super.pollCurrentTask();
        movePathStack = null;
    }

    @Override
    public void setEmergencyTask(RobotTask emergencyTask) {
        super.setEmergencyTask(emergencyTask);
        movePathStack = null;
    }

    /**
     * A move method manager that will analyze the path of other robots to see when this one should move
     * @param destinationLocation The destination location of this robot
     * @return If the robot has completed the task or not
     */
    public boolean pathManager(MapLocation destinationLocation) {
        if (move(destinationLocation)) {
            if (movePathStack == null) {
                return true;
            } else {
                movePathStack.pop();
            }
        }

        return false;
    }

    /**
     * Method that will move the robot based on the top MapLocation in the robots move path stack
     * @param destinationLocation The destination location a unit
     * @return True if the robot was able to move
     */
    public boolean move(MapLocation destinationLocation) {
        if (!Player.gc.isMoveReady(this.getId())) {
            return false;
        }

        // If the current path is null
        if (movePathStack == null) {
             movePathStack = getPathFromBFS(destinationLocation);

             // After calculating the path, if it is still null, the robot is unable to reach the location.
            // If the robot is part of a global task, remove it from the task and the individual tasks it has.
             if (movePathStack == null) {
                 System.out.println("Unit: " + this.getId() + " cannot reach the desired location");

                 int currentTaskId = this.getCurrentTask().getTaskId();
                 if (currentTaskId != -1) {
                     Earth.earthTaskMap.get(currentTaskId).removeWorkerFromList(this.getId());
                 }
                 return true;
             }
        }

        // If adjacent to destination, the robot has finished moving
        if (movePathStack.peek().equals(destinationLocation)) {
            movePathStack = null;
            return true;
        }

        if (!Player.gc.canMove(this.getId(), this.getLocation().directionTo(movePathStack.peek()))) {
            reroute();
        }

        if (Player.gc.canMove(this.getId(), this.getLocation().directionTo(movePathStack.peek()))) {
            Player.gc.moveRobot(this.getId(), this.getLocation().directionTo(movePathStack.peek()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Uses BreadthFirstSearch algorithm to get the path of a unit to the given destination. The path returned does
     * not include the starting position or the final position
     * @param destinationLocation The location that you want to move to
     * @return A stack of MapLocations indicating the robots path to the destination
     */
    private Stack<MapLocation> getPathFromBFS(MapLocation destinationLocation) {

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(this.getLocation());

        HashMap<String, Integer> checkedLocations = new HashMap<>();
        checkedLocations.put(Player.mapLocationToString(this.getLocation()), 0);

        int moveCounter = 1;
        while (!frontier.isEmpty()) {

            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection: Player.getMoveDirections()) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (Player.isLocationEmpty(nextLocation) && !checkedLocations.containsKey(Player.mapLocationToString(nextLocation))) {
                    frontier.add(nextLocation);
                    checkedLocations.put(Player.mapLocationToString(nextLocation), moveCounter);

                    if (currentLocation.isAdjacentTo(destinationLocation)) {
                        frontier.clear();
                        break;
                    }
                }
            }

            moveCounter++;
        }

        checkedLocations.put(Player.mapLocationToString(destinationLocation), moveCounter);

        return backtrace(destinationLocation, checkedLocations);
    }

    /**
     * Helper method that will backtrace the visited locations and will find the shortest path from the destination
     * @param destinationLocation The destination that the unit wants to get to
     * @param checkedLocations The HashMap of checked locations
     * @return One of the shortest paths
     */
    private Stack<MapLocation> backtrace(MapLocation destinationLocation, HashMap<String, Integer> checkedLocations) {
        Stack<MapLocation> shortestPath = new Stack<>();
        shortestPath.add(destinationLocation);

        if (!checkedLocations.containsKey(Player.mapLocationToString(destinationLocation))) {
            return null;
        }

        MapLocation checkingLocation = destinationLocation;
        int movesToDestination = checkedLocations.get(Player.mapLocationToString(destinationLocation));
        for (int i = movesToDestination; i > 0 ; i--) {

            for (Direction nextDirection: Player.getMoveDirections()) {
                MapLocation nextLocation = checkingLocation.add(nextDirection);

                if (checkedLocations.containsKey(Player.mapLocationToString(nextLocation)) &&
                        checkedLocations.get(Player.mapLocationToString(nextLocation)) == i-1) {

                    shortestPath.add(nextLocation);
                    checkingLocation = nextLocation;

                    break;
                }
            }
        }

        return shortestPath;
    }

    /**
     * Method that will try to reroute the units path. If the unit is surrounded, it will add the original path back
     * and the robot will wait until it can move in the intended direction
     */
    private void reroute() {
        Stack<MapLocation> originalPath = new Stack<>();

        while (movePathStack.size() > 1) {
            if (!Player.isOccupiable(movePathStack.peek())) {
                originalPath.add(movePathStack.pop());
            } else {
                break;
            }
        }

        MapLocation nextOpenLocation = movePathStack.peek();
        Stack<MapLocation> recalculatedPath = getPathFromBFS(nextOpenLocation);

        if (recalculatedPath == null) {
            while (!originalPath.empty()) {
                movePathStack.add(originalPath.pop());
            }

        } else {
            while (!recalculatedPath.empty()) {
                movePathStack.add(recalculatedPath.pop());
            }
        }
    }

//    /**
//     * Method that will set a robots task to wander within a certain radius
//     * @param radius The radius to wander in
//     */
//    public void wanderWithinRadius(int radius) {
//
//            ArrayList<Direction> moveDirections = Player.getMoveDirections();
//            MapLocation myLocation = this.getLocation();
//            for (Direction direction : moveDirections) {
//                if (Player.isOccupiable(myLocation.add(direction)) && this.wanderFromLocation.distanceSquaredTo(myLocation.add(direction)) < radius) {
//                    this.addTaskToQueue(new RobotTask(-1, Command.MOVE, myLocation.add(direction)));
//                }
//            }
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
//        // System.out.println("no wander Location");
//    }
}


