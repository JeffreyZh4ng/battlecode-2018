import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.UnitType;

public class Factory extends Structure {

    public Factory(int id, boolean isBuilt, MapLocation factoryLocation) {
        super(id, isBuilt, factoryLocation);
    }

    @Override
    public void run() {
        if (this.isBuilt()) {
            if (Player.gc.canProduceRobot(this.getId(), UnitType.Ranger) && Earth.rangerCount < 40) {
                Player.gc.produceRobot(this.getId(), UnitType.Ranger);
                Earth.rangerCount++;
            }
        }

        unload();
    }

    @Override
    public void unload() {
        for (int i = 0; i < 8; i++) {
            Direction direction = Direction.swigToEnum(i);
            if (Player.gc.canUnload(this.getId(), direction)) {
                Player.gc.unload(this.getId(), direction);

                MapLocation unloadLocation = this.getLocation().add(direction);
                Planet planet = this.getStructureLocation().getPlanet();
                int unitId = Player.gc.senseUnitAtLocation(unloadLocation).id();

                UnitInstance unitInstance = new Ranger(unitId);
                if (planet == Planet.Earth) {
                    Earth.earthStagingAttackerMap.put(unitId, unitInstance);
                } else {
                    Mars.marsStagingAttackerMap.put(unitId, unitInstance);
                }
            }
        }
    }
}

