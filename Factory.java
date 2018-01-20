import bc.MapLocation;
import bc.UnitType;

public class Factory extends Structure {

    public Factory(int id, boolean isBuilt, MapLocation factoryLocation) {
        super(id, isBuilt, factoryLocation);
    }

    @Override
    public void run() {
        if (this.isBuilt()) {
            if (Player.gc.canProduceRobot(this.getId(), UnitType.Ranger) && Earth.rangerCount < 20) {
                Player.gc.produceRobot(this.getId(), UnitType.Ranger);
                Earth.rangerCount++;
            }
        }

        unload();
    }
}

