import bc.*;

import java.util.HashMap;
import java.util.HashSet;

public class Player {

    public static final GameController gc = new GameController();

    public static void main(String[] args) {

        Earth earth = new Earth();
        Mars mars = new Mars();

        if(gc.planet() == Planet.Earth) {
            addStartingWorkersToEarthMap();
        }

        gc.queueResearch(UnitType.Rocket); // Can build rockets at round 100
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Knight);
        gc.queueResearch(UnitType.Knight);

        while (true) {
            System.out.println("time left: " + gc.getTimeLeftMs());
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

            if (gc.team() == Team.Blue && gc.planet() == Planet.Earth) {
                System.out.println("Round number: " + gc.round());

                MapLocation initialLocation = new MapLocation(Planet.Earth, 0,0);
                MapLocation finalLocation = new MapLocation(Planet.Earth, 0,4);

                HashMap<Integer, HashSet<MapLocation>> map = Robot.getDepthMap(initialLocation, finalLocation);
                for (int key: map.keySet()) {
                    System.out.println("Depth: " + key);
                    for (MapLocation location: map.get(key)) {
                        System.out.println(Robot.mapLocationToString(location));
                    }
                    System.out.println("");
                }
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
        earth.createGlobalTask(Command.CONSTRUCT_ROCKET);
        earth.createGlobalTask(Command.CONSTRUCT_ROCKET);
        earth.createGlobalTask(Command.CONSTRUCT_ROCKET);
        earth.createGlobalTask(Command.CONSTRUCT_ROCKET);
        earth.createGlobalTask(Command.CONSTRUCT_ROCKET);
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

    /**
     * Simplified method of isOccupiable that will handle any exceptions that the gc.isOccupiable method will
     * throw. Check if the location is on the map then checks if it can see the location. If the location can
     * be seen then return the default isOccupiable method. If we cant see the location then return true because
     * the worst that can happen is there is an enemy unit there which we can destroy.
     * @param mapLocation The location you want to check
     * @return If it is occupiable or not
     */
    public static boolean inOccupiable(MapLocation mapLocation) {
        PlanetMap initialMap = Player.gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation)) {
            if (Player.gc.canSenseLocation(mapLocation)) {
                return gc.isOccupiable(mapLocation) > 0;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}


