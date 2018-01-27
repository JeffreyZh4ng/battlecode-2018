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
            if (Player.gc.canProduceRobot(this.getId(), unitToProduce) && Player.gc.karbonite() > 80) {
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

                UnitInstance unitInstance = new Knight(unitId);
                Earth.earthStagingAttackerMap.put(unitId, unitInstance);
            }
        }
    }

    /**
     * Method that will tell the factory produce a specific unit given the current unit counts.
     * @return The unit to produce
     */
    private UnitType findUnitToProduce() {
        int armySize = Earth.earthAttackerMap.size();
        if (armySize > 40 && Earth.mageCount < 5) {
            return UnitType.Mage;
        } else {
//            int randomInt = (int)(Math.random() * 2);
//            if (randomInt == 0) {
//                return UnitType.Knight;
//            } else {
                return UnitType.Ranger;
//            }
        }
    }
}

