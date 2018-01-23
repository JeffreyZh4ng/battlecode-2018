import bc.*;

public class Mage extends Attacker{

    public Mage(int id) {
        super(id);
    }

    @Override
    public void run() {
        runAttacker();
    }

    /**
     * Attacks the enemy with most surounding enemies that it can, if no enemies reachable, will move towards them
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    @Override
    public boolean runBattleAction() {
        Team otherTeam = Player.gc.team() == Team.Blue ? Team.Red : Team.Blue;
        System.out.println("Mage id2 " + this.getId());
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getAttackRange(), otherTeam);

        if (enemyUnits.size() == 0) {
            enemyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), otherTeam);
        }
        if (enemyUnits.size() == 0) {
            return false;
        }

        if (Player.gc.isAttackReady(this.getId())) {
            Unit mostSurroundedUnit = getMostSurroundedEnemy(enemyUnits);
            int distanceToMostSurroundedUnit = (int)(this.getLocation().distanceSquaredTo(mostSurroundedUnit.location().mapLocation()));

            if (distanceToMostSurroundedUnit > this.getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    move(this.getId(), mostSurroundedUnit.location().mapLocation());
                }
            }

            if (Player.gc.canAttack(this.getId(), mostSurroundedUnit.id())) {
                Player.gc.attack(this.getId(), mostSurroundedUnit.id());
            }
        }

        return false;
    }

    /**
     * Given a VecUnit of units, will return the unit with the lowest health
     * @param units the units to compare
     * @return the weakest of the given units
     */
    private Unit getMostSurroundedEnemy(VecUnit units) {
        if (units.size() == 0) {
            System.out.println("unit list was empty");
            return null;
        }
        Unit mostSurroundedUnit = null;
        int maxCount = -1;
        for (int i = 0; i < units.size(); i++) {
            int thisCount = 0;
            Unit thisUnit = units.get(i);
            for (Direction direction : Player.getMoveDirections()) {
                MapLocation locationToTest = thisUnit.location().mapLocation().add(direction);
                if(Player.gc.canSenseLocation(locationToTest) && Player.gc.hasUnitAtLocation(locationToTest)) {
                    thisCount++;
                }
            }
            if (thisCount > maxCount) {
                mostSurroundedUnit = thisUnit;
            }
        }
        return mostSurroundedUnit;
    }
}
