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

            // Will only run blue code for the time being to help with testing
            System.out.println("Round number: " + Globals.gc.round());
            if (Globals.gc.planet() == Planet.Earth && Globals.gc.round() < FLOOD_ROUND
                    && Globals.gc.team().equals(Team.Blue)) {
                earth.execute();
            } else if (Globals.gc.planet() == Planet.Mars && Globals.gc.team().equals(Team.Blue)) {
                mars.execute();
            }

            Globals.gc.nextTurn();
        }
    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers at the beginning of the game
     */
    private static void addStartingWorkersToEarthMap() {
        VecUnit units = Globals.gc.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            Unit worker = new Worker(unitId);

            RobotTask task = new RobotTask(Command.MOVE, new MapLocation(Planet.Earth, 10, 10));
            worker.robotTaskQueue.add(task);
            Earth.earthWorkerMap.put(unitId, worker);
        }
    }
}


