import bc.*;
import planets.EarthOld;
import planets.Mars;
import java.util.Queue;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Player {

    private static final int FLOOD_ROUND = 750;
    private static final Direction[] moveDirections = {Direction.North, Direction.Northeast, Direction.East, Direction.Southeast, Direction.South, Direction.Southwest, Direction.West, Direction.Northwest};
    
    public static GameController gameController = new GameController();
    // public static HashMap<Integer, Robot> earthUnitsHashMap = new HashMap<>();
    // public static HashMap<Integer, Building> earthStructureHashMap = new HashMap<>();
    // public static HashMap<Integer, Squadron> squadronHashMap = new HashMap<>();
    // public static Queue<Command> hitList = new PriorityQueue<>();
    // public static Stack<Command> emergencyTasks = new Stack<>();

    public static void main(String[] args) {

//        Earth.addWorkersToHashMap(gameController);
//        PlanetMap earthMap = gameController.startingMap(Planet.Earth);
//        //findKarbonite(earthMap); // Need to write a method that will iterate over all spaces on the map
//        // And will find the greatest concentrations of karbonite
//
//        gameController.queueResearch(UnitType.Worker);
//        gameController.queueResearch(UnitType.Ranger);
//        gameController.queueResearch(UnitType.Rocket);
//        Earth.workerEarthNextRoundCommandQueue.add(Command.BLUEPRINT_FACTORY);
//        Earth.workerEarthNextRoundCommandQueue.add(Command.CLONE);
//        Earth.workerEarthNextRoundCommandQueue.add(Command.BLUEPRINT_ROCKET);
//        //printKarboniteValues();

        EarthOld earth = new EarthOld();
        Mars mars = new Mars();

        PlanetMap initialEarthMap = gameController.startingMap(Planet.Earth);

        while (true) {

            if (gameController.planet() == Planet.Earth && gameController.round() < FLOOD_ROUND) {
                // earth.execute();
            } else if (gameController.planet() == Planet.Mars) {
                mars.execute();
            }

            gameController.nextTurn();
        }
    }

    /**
     * Method that will return a random direction when called. Intended to be used for preliminary testing
     * @return One of 8 random directions
     */
    public static Direction pickRandomDirection() {
        int randomInt = (int)(Math.random()*8 + 1);
        return Direction.swigToEnum(randomInt);
    }

    /**
     * Subject to change. The isOccupiable method is bugged in the API
     * @param robotId The id of the robot
     * @return An available direction. Null if none are available
     */
    public static Direction returnAvailableDirection(int robotId) {
        for (int i = 0; i < 8; i++) {
            if (gameController.canMove(robotId, Direction.swigToEnum(i))) {
                return Direction.swigToEnum(i);
            }
        }
        return null;
    }

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
    public static void moveWorkers() {
        //TODO: find optimal next locations, consider if robot in path is moving, find optimal order of execution, execute moves
        //for now will find path bassed only on impassable object and move immediately
    }

    /**
     * uses BreadthFirstSearch algorithm to get the next location based on current map
     * @param startingLocation current location of object to move
     * @param destinationLocation
     * @param map
     * @return the next place to step
     */
    public static MapLocation getNextForBreadthFirstSearch(MapLocation startingLocation, MapLocation destinationLocation, PlanetMap map) {
        try {
            Queue<MapLocation> frontier = new PriorityQueue<>();
            frontier.add(startingLocation);
            HashMap<MapLocation,MapLocation> came_from = new HashMap<>();
            came_from.put(startingLocation, null);

            while(!frontier.isEmpty()) {
                MapLocation currentLocation = frontier.poll();
                for(Direction nextDirection : moveDirections) {
                    MapLocation nextLocation = currentLocation.add(nextDirection);
                    if(map.onMap(nextLocation) && map.isPassableTerrainAt(nextLocation)&& came_from.get(nextLocation)!=null) {
                        frontier.add(nextLocation);
                        came_from.put(nextLocation,currentLocation);
                    }
                }
            }
            MapLocation resultLocation = null;
            MapLocation currentLocation = destinationLocation;
            while(currentLocation != startingLocation) {
                resultLocation = currentLocation;
                currentLocation = came_from.get(currentLocation);
            }
            return resultLocation;
        } catch (Exception error) {
            System.out.println(error);
        }
        return null;
    }
}

