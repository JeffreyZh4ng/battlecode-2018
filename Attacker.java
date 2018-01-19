import bc.*;

public abstract class Attacker extends Robot {
    public Attacker(int id) {
        super(id);
    }
    //public abstract boolean attack();

    private boolean inCombat = false;

    private MapLocation wanderLocation = null;

    /**
     * if not in combat moves to and attacks the global attackTarget otherwise attacks closest enemy in range
     */
    public void runAttack() {

        //get correct attack target
        if (PlanetInstance.attackTarget != null && !this.inCombat) {
            move(this.getId(),PlanetInstance.attackTarget);

        } else if(this.inCombat) {
            if(attackClosestEnemyInRange()) {

                this.inCombat = false;
                // TODO: should consider more the range it should be in need to test
                if (this.getLocation().distanceSquaredTo(PlanetInstance.attackTarget) < this.getVisionRange()* 0.8) {
                    PlanetInstance.attackTarget = null;
                }
            } else if (PlanetInstance.attackTarget == null) {
                PlanetInstance.attackTarget = getClosestUnit(-1, getEnemyUnitsInRange(this.getVisionRange())).location().mapLocation();
            }
        } else {

            //TODO: wander, probably could be better
            if (Player.gc.isMoveReady(this.getId())) {
                if(wanderLocation == null || move(this.getId(),this.wanderLocation)) {
                    wanderLocation = Robot.getLocationToExplore();
                }
            }

            //TODO: not sure about range here, this also maybe should be done globally? since all attacker will end up going here?
            VecUnit enemyUnits = this.getEnemyUnitsInRange(this.getVisionRange());
            if(enemyUnits != null) {
                this.inCombat = false;
                PlanetInstance.attackTarget = this.getClosestUnit(-1, enemyUnits).location().mapLocation();
            }
        }
    }



    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    private boolean attackClosestEnemyInRange() {


        VecUnit enemyUnits = this.getEnemyUnitsInRange(this.getVisionRange());

        if (enemyUnits.size() == 0) {
            return true;
        }

        if (Player.gc.isAttackReady(this.getId())) {
            Unit closestUnit = getClosestUnit(-1, enemyUnits);

            int closestDistanceToUnit = (int)this.getLocation().distanceSquaredTo(closestUnit.location().mapLocation());


            if (closestDistanceToUnit > getAttackRange()) {
                move(this.getId(), closestUnit.location().mapLocation());
            }

            if (Player.gc.canAttack(this.getId(), closestUnit.id())) {
                Player.gc.attack(this.getId(), closestUnit.id());
            }
        }

        return false;
    }

    public int getAttackRange() { return (int)(Player.gc.unit(this.getId()).attackRange()); }
}
