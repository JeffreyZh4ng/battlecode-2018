import bc.MapLocation;
import bc.Planet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Rocket extends Structure {

    private boolean inFlight = false;
    private ArrayList<Integer> loadList = new ArrayList<>();
    private HashMap<Integer, UnitInstance> garrison = new HashMap<>();

    public Rocket(int id, boolean isBuilt, MapLocation rocketLocation) {
        super(id, isBuilt, rocketLocation);
    }

    public boolean isInFlight() {
        return inFlight;
    }

    @Override
    public void unload() {
//        for (int i = 0; i < 8; i++) {
//            Direction direction = Direction.swigToEnum(i);
//            if (Player.gc.canUnload(this.getId(), direction)) {
//                Player.gc.unload(this.getId(), direction);
//
//                MapLocation unloadLocation = this.structureLocation.add(direction);
//                Planet planet = structureLocation.getPlanet();
//                int unitId = Player.gc.senseUnitAtLocation(unloadLocation).id();
//
//                UnitInstance unitInstance = new Ranger(unitId);
//                if (planet == Planet.Earth) {
//                    Earth.earthStagingAttackerMap.put(unitId, unitInstance);
//                } else {
//                    if (unitInstance.)
//                        Mars.marsStagingAttackerMap.put(unitId, unitInstance);
//                }
//            }
//        }
    }

    @Override
    public void run() {
        if (this.isBuilt()) {
            if (this.getLocation().getPlanet() == Planet.Earth) {
            }
        }
    }

    /**
     * Every round this method is called to try to load units that were previously unable to be loaded
     */
    public boolean loadUnit(int unitId, UnitInstance unitInstance) {
        if (Player.gc.canLoad(this.getId(), unitId)) {
            Player.gc.load(this.getId(), unitId);
            garrison.put(unitId, unitInstance);
            Earth.earthGarrisonedUnits.add(unitId);

            return true;

        }

        return false;
    }

//    /**
//     * Method that will be called by the GlobalTask. The rocket will try to load a unit if it can. If it cant,
//     * it will add it to the list to be loaded
//     * @param unitId The id of the unit to be loaded
//     */
//    public boolean addLoadToList(int unitId, UnitInstance unitInstance) {
//        if (Player.gc.canLoad(this.getId(), unitId)) {
//            Player.gc.load(this.getId(), unitId);
//            garrison.put(unitId, unitInstance);
//
//            return true;
//
//        } else {
//            loadList.add(unitId);
//
//            return false;
//        }
//    }


}
