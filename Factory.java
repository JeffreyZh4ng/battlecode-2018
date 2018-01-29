import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.UnitType;

public class Factory extends UnitInstance {

    private boolean isBuilt;

    public Factory(int id, boolean isBuilt) {
        super(id);
        this.isBuilt = isBuilt;
    }

    /**
     * Method that will run code for the factory
     */
    @Override
    public void run() {
        if (isBuilt) {
            UnitType unitToProduce = findUnitToProduce();
            if (Player.gc.canProduceRobot(this.getId(), unitToProduce)) {
                Player.gc.produceRobot(this.getId(), unitToProduce);

                switch (unitToProduce) {
                    case Knight:
                        Earth.knightCount++;
                        System.out.println("Knight count " + Earth.knightCount);
                        break;
                    case Ranger:
                        Earth.rangerCount++;
                        System.out.println("Ranger count " + Earth.rangerCount);
                        break;
                    case Healer:
                        Earth.healerCount++;
                        System.out.println("Healer count " + Earth.healerCount);
                        break;
                    case Mage:
                        Earth.mageCount++;
                        System.out.println("Mage count " + Earth.mageCount);
                        break;
                }
            }
        }

        unload();
    }

    /**
     * Method that will unload units inside its garrison when it can
     */
    public void unload() {
        for (int i = 0; i < 8; i++) {
            Direction direction = Direction.swigToEnum(i);
            if (Player.gc.canUnload(this.getId(), direction)) {
                Player.gc.unload(this.getId(), direction);

                MapLocation unloadLocation = this.getLocation().add(direction);
                int unitId = Player.gc.senseUnitAtLocation(unloadLocation).id();
                UnitType unitType = Player.gc.unit(unitId).unitType();

                UnitInstance unitInstance = null;
                switch (unitType) {
                    case Knight:
                        unitInstance = new Knight(unitId);
                        break;
                    case Ranger:
                        unitInstance = new Ranger(unitId);
                        break;
                    case Healer:
                        unitInstance = new Healer(unitId);
                        break;
                    case Mage:
                        unitInstance = new Mage(unitId);
                        break;
                    default:
                        unitInstance = new Knight(unitId);
                        System.out.println("ERROR in UNLOAD, unit type: " + unitType);
                }

                Earth.earthStagingAttackerMap.put(unitId, unitInstance);
            }
        }
    }

    /**
     * Method that will tell the factory produce a specific unit given the current unit counts.
     * @return The unit to produce
     */
    private UnitType findUnitToProduce() {
        if (Earth.healerCount < (Earth.knightCount)*(.5) && Earth.knightCount > 5) {
            return UnitType.Healer;
        } else {
            return UnitType.Knight;
        }
    }
}

