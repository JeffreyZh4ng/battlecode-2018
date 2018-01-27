import bc.*;

import java.util.*;

public class Rocket extends UnitInstance {

    private boolean isBuilt;
    private boolean inFlight;
    private int garrisonCount;

    public Rocket(int id, boolean isBuilt) {
        super(id);
        this.isBuilt = isBuilt;
        this.inFlight = false;
        this.garrisonCount = 0;

        if (isBuilt) {
            Earth.createGlobalTask(Command.LOAD_ROCKET);
        }
    }

    public boolean isInFlight() {
        return inFlight;
    }

    @Override
    public void run() {
        if (isBuilt) {

            if ((this.getLocation().getPlanet() == Planet.Earth && !inFlight && garrisonCount == 8) ||
                    Player.gc.unit(this.getId()).health() < 100) {

                // TODO: Dont forget about changing the location
                MapLocation locationToLand = new MapLocation(Planet.Mars, 10, 10);
                if (Player.gc.canLaunchRocket(this.getId(), locationToLand)) {
                    Player.gc.launchRocket(this.getId(), locationToLand);
                    inFlight = true;
                }

            } else if (this.getLocation().getPlanet() == Planet.Mars) {
                unload();
            }
        }
    }

    /**
     * Method will unload all the units it can when the rocket reaches mars.
     */
    public void unload() {
        for (int i = 0; i < 8; i++) {
            Direction direction = Direction.swigToEnum(i);
            if (Player.gc.canUnload(this.getId(), direction)) {
                Player.gc.unload(this.getId(), direction);

                MapLocation unloadLocation = this.getLocation().add(direction);
                int unitId = Player.gc.senseUnitAtLocation(unloadLocation).id();

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
            Earth.earthGarrisonedUnits.add(unitId);
            System.out.println("Loaded unit " + unitId);
            garrisonCount++;

            return true;
        }

        return false;
    }
}
