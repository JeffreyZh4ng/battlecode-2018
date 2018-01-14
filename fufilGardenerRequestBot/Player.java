import bc.*;
import commandsAndRequests.Command;
import commandsAndRequests.Globals;
import commandsAndRequests.RobotTask;
import planets.Earth;
import planets.Mars;
import units.Unit;
import units.robots.Worker;

public class Player {

    private static final int FLOOD_ROUND = 750;

    public static void main(String[] args) {

        Earth earth = new Earth();
        Mars mars = new Mars();

        addStartingWorkersToEarthMap();

        while (true) {

            System.out.println("Round number: " + Globals.gameController.round());
            if (Globals.gameController.planet() == Planet.Earth && Globals.gameController.round() < FLOOD_ROUND
                    && Globals.gameController.team().equals(Team.Blue)) {
                earth.execute();
            } else if (Globals.gameController.planet() == Planet.Mars && Globals.gameController.team().equals(Team.Blue)) {
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
            Unit worker = new Worker(unitId);

            RobotTask task = new RobotTask(Command.MOVE, new MapLocation(Planet.Earth, 10, 10));
            worker.robotTaskQueue.add(task);
            Earth.earthWorkerMap.put(unitId, worker);
        }
    }
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


