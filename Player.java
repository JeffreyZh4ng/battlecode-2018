import bc.*;

public class Player {

    public static final GameController gc = new GameController();

    public static void main(String[] args) {

        Earth earth = new Earth();
        Mars mars = new Mars();

        if(gc.planet() == Planet.Earth) {
            addStartingWorkersToEarthMap();
        }

        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Rocket); // Can build rockets at round 150
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Knight);
        gc.queueResearch(UnitType.Knight);

        while (true) {

            if(gc.planet() == Planet.Earth && gc.team() == Team.Blue) {
                if (gc.round() == 150 || gc.round() == 300 || gc.round() == 450 || gc.round() == 600 || gc.round() == 700) {
                    buildRockets(earth);
                }

                if (gc.round() == 1 || gc.round() == 200 || gc.round() == 350) {
                    startNewTask(earth);
                }

            }
            if (gc.team() == Team.Blue && gc.planet() == Planet.Earth) {
                System.out.println("Round number: " + gc.round());
                //printOutUnitList();
            }


            if (gc.planet() == Planet.Earth && gc.round() < 750 && gc.team() == Team.Blue) {
                earth.execute();
            } else if (gc.planet() == Planet.Mars && gc.team() == Team.Blue) {
                mars.execute();
            }


            // Debug statements
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
    }

    private static void buildRockets(Earth earth) {
        earth.createGlobalTask(Command.BLUEPRINT_ROCKET);
        earth.createGlobalTask(Command.BLUEPRINT_ROCKET);
        earth.createGlobalTask(Command.BLUEPRINT_ROCKET);
        earth.createGlobalTask(Command.BLUEPRINT_ROCKET);
        earth.createGlobalTask(Command.BLUEPRINT_ROCKET);
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


