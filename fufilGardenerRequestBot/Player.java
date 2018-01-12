import bc.*;
import commandsAndRequests.Globals;
import planets.Earth;
import planets.Mars;
import robots.Robot;
import robots.Worker;

public class Player {

    private static final int FLOOD_ROUND = 750;
    

    public static void main(String[] args) {

        Earth earth = new Earth();
        Mars mars = new Mars();

        addStartingWorkersToEarthMap();

        while (true) {
            if (Globals.gameController.planet() == Planet.Earth && Globals.gameController.round() < FLOOD_ROUND) {
                earth.execute();
            } else if (Globals.gameController.planet() == Planet.Mars) {
                mars.execute();
            }

            Globals.gameController.nextTurn();
        }
    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers at the beginning of the game
     */
    private static void addStartingWorkersToEarthMap() {
        VecUnit units = Globals.gameController.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            Robot worker = new Worker(unitId);
            Earth.earthWorkerMap.put(unitId, worker);
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
            if (Globals.gameController.canMove(robotId, Direction.swigToEnum(i))) {
                return Direction.swigToEnum(i);
            }
        }
        return null;
    }

    /**
     * finds next optimal locations for each robot to move to and moves them to that location
     */
    public static void moveWorkers() {
        //find optimal next locations, consider if robot in path is moving, find optimal order of execution
        //execute move actions
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
}