import bc.*;

public abstract class Attacker extends Robot {
    public Attacker(int id) {
        super(id);
    }
    //public abstract boolean attack();

    private boolean inCombat = false;

    private MapLocation wanderLocation = null;

    public void runAttack() {

        //get correct attack target

        if (PlanetInstance.attackTarget != null && !this.inCombat) {
            move(this.getId(),PlanetInstance.attackTarget);

        } else if(this.inCombat) {
            if(attackClosestEnemyInRange()) {
                this.inCombat = false;
                // TODO: should consider more the range it should be in
                if (this.getLocation().distanceSquaredTo(PlanetInstance.attackTarget) < this.getVisionRange()* 0.8) {
                    return;
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

//        if (global target && not in combat) {
//            // move towards global target
//            // Sense if there are enemies again
//            // If there are, set in combat to true
//        }
//
//        else if (this.inCombat) {
//            if (attack()) {
//                this.setInCombat(false);
//                // if global location is also in sight set to clear
//                return;
//            }
//
//            if (global target is also empty){
//                // set nearest enemy location to global target
//            }
//        }
//
//        else {
//            // wander?
//            // If enemy is seen, set in combat to true and set global location to enemy location
//        }
    }


//    //default attack, others may have to implement diffrently
//    public boolean attack() {
//
//
//        // Attack the nearest enemy in attack range
//
//        // If any enemies are still in sight range
//        // Return false
//        // else
//        // Return true;
//
//
//
//
//
//
//
//        //if enemy in range attack
//        // else if global location is set, go to global location
//        //if nothing at globallocation set empty
//        //if globallocation empty, wander and set enemy location
//        return true;
//    }


    /**
     * Attacks the weakest enemy that it can
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
                if (Player.gc.isMoveReady(this.getId())) {
                    move(this.getId(), closestUnit.location().mapLocation());
                }
            }

            if (Player.gc.canAttack(this.getId(), closestUnit.id())) {
                Player.gc.attack(this.getId(), closestUnit.id());
            }
        }

        return false;
    }

    public int getAttackRange() { return (int)(Player.gc.unit(this.getId()).attackRange()); }
}
