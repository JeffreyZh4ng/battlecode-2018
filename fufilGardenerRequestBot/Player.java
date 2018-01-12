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
}