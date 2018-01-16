import bc.*;

public class Player {

    private static final int FLOOD_ROUND = 750;
    public static final GameController gc = new GameController();

    public static void main(String[] args) {

        Earth earth = new Earth();
        Mars mars = new Mars();

        addStartingWorkersToEarthMap();
        startNewTask(earth);

        while (true) {

            if (gc.team() == Team.Blue && gc.planet() == Planet.Earth) {
                System.out.println("Round number: " + gc.round());
                //printOutUnitList();
            }

            if (gc.planet() == Planet.Earth && gc.round() < FLOOD_ROUND && gc.team() == Team.Blue) {
                earth.execute();
            } else if (gc.planet() == Planet.Mars && gc.team() == Team.Blue) {
                mars.execute();
            }


            if (gc.team() == Team.Blue && gc.planet() == Planet.Earth) {
                System.out.println("");
            }

            gc.nextTurn();
        }
    }

    private static void startNewTask(Earth earth) {
        earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
        earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
        earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
        earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
        earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers at the beginning of the game
     */
    private static void addStartingWorkersToEarthMap() {
        VecUnit units = gc.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            UnitInstance worker = new Worker(unitId);

            Earth.earthWorkerMap.put(unitId, worker);
        }
    }

    private static void printOutUnitList() {
        System.out.println("Printing from unit map!");
        for (int i = 0; i < gc.units().size(); i++) {
            if (gc.units().get(i).team() == Team.Blue && gc.units().get(i).unitType() == UnitType.Worker) {
                System.out.println("Unit no: " + gc.units().get(i).id());
            }
        }
    }
}


