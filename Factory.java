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
            if (Player.gc.canProduceRobot(this.getId(), UnitType.Mage) && Earth.mageCount < 15) {
                Earth.mageCount++;
                Player.gc.produceRobot(this.getId(), UnitType.Mage);
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

                UnitInstance unitInstance = new Mage(unitId);
                System.out.println("Mage id: " + unitInstance.getId());
                if (planet == Planet.Earth) {
                    Earth.earthStagingAttackerMap.put(unitId, unitInstance);
                } else {
                    Mars.marsStagingAttackerMap.put(unitId, unitInstance);
                }
            }
        }
    }
}

