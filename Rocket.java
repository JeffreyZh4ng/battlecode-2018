import bc.*;

import java.util.*;

public class Rocket extends Structure {

    private boolean inFlight = false;
    private int garrisonCount = 0;
    private HashSet<Integer> garrison = new HashSet<>();

    public Rocket(int id, boolean isBuilt, MapLocation rocketLocation) {
        super(id, isBuilt, rocketLocation);
    }

    public boolean isInFlight() {
        return inFlight;
    }


    @Override
    public void run() {
        if (this.isBuilt()) {
            if (this.getLocation().getPlanet() == Planet.Earth && !inFlight) {
                if (garrisonCount == 0) {
                    MapLocation locationToLand = Player.getLandingLocation();
                    if (Player.gc.canLaunchRocket(this.getId(), locationToLand)) {
                        Player.gc.launchRocket(this.getId(), locationToLand);
                        inFlight = true;
                    }
                }
            }
            System.out.println("Rocket size: " + garrisonCount);
        }
    }

    @Override
    public void unload() {
        for (int i = 0; i < 8; i++) {
            Direction direction = Direction.swigToEnum(i);
            if (Player.gc.canUnload(this.getId(), direction)) {
                Player.gc.unload(this.getId(), direction);

                MapLocation unloadLocation = this.getLocation().add(direction);
                int unitId = Player.gc.senseUnitAtLocation(unloadLocation).id();
                garrison.add(unitId);

                UnitType unitType = Player.gc.unit(unitId).unitType();
                UnitInstance unitInstance;
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
                        unitInstance = new Worker(unitId);
                        break;
                }

                if (unitType == UnitType.Worker) {
                    Mars.marsWorkerMap.put(unitId, unitInstance);
                } else {
                    Mars.marsAttackerMap.put(unitId, unitInstance);
                }
            }
        }
    }

    /**
     * Every round this method is called to try to load units that were previously unable to be loaded
     */
    public boolean loadUnit(int unitId) {
        if (Player.gc.canLoad(this.getId(), unitId)) {
            Player.gc.load(this.getId(), unitId);
            garrisonCount++;
            Earth.earthGarrisonedUnits.add(unitId);
            System.out.println("Loaded unit " + unitId);

            return true;
        }

        return false;
    }

    /**
     * When a rocket is empty, disintegrate it
     * @return If the rocket was disintegrated or not
     */
    public boolean disintegrateRocket() {
        if (garrison.size() >= 8) {
            Player.gc.disintegrateUnit(this.getId());

            return true;
        }

        return false;
    }
}
