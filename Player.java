import bc.*;

public class Player {

    private static final int FLOOD_ROUND = 750;
    public static final GameController gc = new GameController();

    public static void main(String[] args) {

        Earth earth = new Earth();
        Mars mars = new Mars();

        addStartingWorkersToEarthMap();

        while (true) {

            System.out.println(gc.round());
            System.out.println("time left: " + gc.getTimeLeftMs());
            // Will only run blue code for the time being to help with testing
            System.out.println("Round number: " + gc.round());
            if (gc.planet() == Planet.Earth && gc.round() < FLOOD_ROUND) {
                earth.execute();
            } else if (gc.planet() == Planet.Mars) {
                mars.execute();
            }

            gc.nextTurn();
        }
    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers at the beginning of the game
     */
    private static void addStartingWorkersToEarthMap() {
        VecUnit units = gc.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            Unit worker = new Worker(unitId);


            for (int j = 0; j < 20; j++) {
                MapLocation randomLocation = Robot.getLocationToExplore();
                RobotTask task = new RobotTask(j,Command.MOVE, randomLocation);
                worker.robotTaskQueue.add(task);
            }
            Earth.earthWorkerMap.put(unitId, worker);
        }
    }
}


