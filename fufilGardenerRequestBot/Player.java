import bc.*;

public class Player {

    private static final int FLOOD_ROUND = 750;
    
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
        //find optimal next locations, consider if robot in path is moving, find optimal order of execution
        //execute move actions
    }
}