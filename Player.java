import bc.*;

import java.util.HashMap;
import java.util.HashSet;

public class Player {

    public static final GameController gc = new GameController();

    public static void main(String[] args) {

        addStartingWorkersToEarthMap();
        while (true) {

            if (gc.team() == Team.Blue && gc.planet() == Planet.Earth) {
                System.out.println("Round number: " + gc.round());

                if (gc.round() == 1) {
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                }
                Earth.execute();
            }


            gc.nextTurn();
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
    public static boolean isOccupiable(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation) && initialMap.isPassableTerrainAt(mapLocation) > 0) {
            if (gc.canSenseLocation(mapLocation)) {
                return gc.isOccupiable(mapLocation) > 0;
            } else {
                return true;
            }

        }

        return false;
    }

    /**
     * Is occupiable for structures
     * @param mapLocation The location you want to check
     * @return If it is occupiable or not
     */
    public static boolean isOccupiableForStructure(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation) && initialMap.isPassableTerrainAt(mapLocation) > 0) {
            if (gc.canSenseLocation(mapLocation)) {
                if (gc.hasUnitAtLocation(mapLocation)) {
                    if (gc.senseUnitAtLocation(mapLocation).unitType() != UnitType.Factory ||
                            gc.senseUnitAtLocation(mapLocation).unitType() != UnitType.Rocket) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }

        }

        return false;
    }

    /**
     * Should move robot in given direction,
     * checks that: unit exists, unit is correct team, unit is on this map, destination is on map, destination is empty
     * @param id The id of the robot to move
     * @return If move was successful
     */
    public static boolean moveRobot(int id, MapLocation locationToMoveTo) {
        if(gc.isMoveReady(id)) {
            PlanetMap startingMap = gc.startingMap(gc.planet());
            Unit unit;
            try {
                unit = gc.unit(id);
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("UNIT WITH ID: " + id + " DOES NOT EXIST");
                return false;
            }

            if (unit.team() == gc.team()) {
                if (unit.location().mapLocation().getPlanet() == gc.planet()) {

                    MapLocation unitMapLocation = unit.location().mapLocation();

                    if (startingMap.onMap(unitMapLocation) && isOccupiable(locationToMoveTo) && gc.canMove(id, unitMapLocation.directionTo(locationToMoveTo))) {
                        try {
                            gc.moveRobot(id, unitMapLocation.directionTo(locationToMoveTo));
                            return true;
                        } catch (Exception e) {
                            System.out.println(e);
                            System.out.println("ERROR MOVE ROBOT STILL FAILED TO STOP ERROR AFTER MANY TESTS unit: " + unit);
                            return false;
                        }
                    } else {
                        System.out.println("ERROR LOCATION TO MOVE TO INVALID locationtomoveto: " + locationToMoveTo + "from unit at: " + unitMapLocation);
                        return false;
                    }
                } else {
                    System.out.println("ERROR ROBOT NOT ON THIS PLANET location: " + unit.location().mapLocation());
                    return false;
                }
            } else {
                System.out.println("ERROR UNIT TO MOVE TEAM INVALID, unit: " + unit);
                return false;

            }
        } else {
            System.out.println("ERROR movement heat not ready");
            return false;
        }
    }

    /**
     * Simplified method of senseUnitAtLocation that will handle the exception of if the location is not visible.
     * Catches all errors.
     * @param mapLocation The location of the unit that you want to sense
     * @return The unit at the location, null if
     */
    public static Unit senseUnitAtLocation(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation)) {
            if (gc.canSenseLocation(mapLocation)) {
                return gc.senseUnitAtLocation(mapLocation);
            } else {
                System.out.println("LOCATION IS NOT IN VIEW RANGE");
                return null;
            }
        }

        System.out.println("LOCATION IS NOT ON THE MAP");
        return null;
    }

    /**
     * Will return the karbonite at a specific location. If the location is not on the map or the location is
     * not in the range of a robot, return -1
     * @param mapLocation The location you want to check the karbonite value of
     * @return The value of karbonite at the location
     */
    public static int karboniteAt(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation)) {
            if (gc.canSenseLocation(mapLocation)) {
                return (int)(gc.karboniteAt(mapLocation));
            } else {
                System.out.println("LOCATION IS NOT IN THE SENSE ZONE");
                return -1;
            }
        }

        System.out.println("LOCATION IS NOT ON THE MAP");
        return -1;
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
}


