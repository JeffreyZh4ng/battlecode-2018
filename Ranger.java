import bc.*;

public class Ranger extends Attacker {

    private static final int MIN_ATTACK_RANGE = 10;

    public Ranger(int id) {
        super(id);
    }

    @Override
    public boolean runBattleAction() {
        return attackClosestEnemyInRange();
    }

    public void run() {
        runAttacker();
    }

    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    public boolean attackClosestEnemyInRange() {

        Team otherTeam = Player.gc.team() == Team.Blue ? Team.Red : Team.Blue;
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), otherTeam);

        if (enemyUnits.size() == 0) {
            return true;
        }

        if (Player.gc.isAttackReady(this.getId())) {
            Unit closestUnit = getClosestUnit(-1, enemyUnits);
            int closestDistanceToUnit = (int)this.getLocation().distanceSquaredTo(closestUnit.location().mapLocation());

            if (closestDistanceToUnit > getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    move(this.getId(), closestUnit.location().mapLocation());
                }
            }

            if (closestDistanceToUnit < MIN_ATTACK_RANGE) {
                // TODO: try move back could be better then this
                // Player.moveRobot(this.getId(), this.getLocation().add(closestUnit.location().mapLocation().directionTo(this.getLocation())));
                closestUnit = getClosestUnit(-1, Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getAttackRange(), otherTeam));
            }

            if (Player.gc.canAttack(this.getId(), closestUnit.id())) {
                Player.gc.attack(this.getId(), closestUnit.id());
            }
        }

        return false;
    }
}
