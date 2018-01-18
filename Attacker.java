import bc.MapLocation;
import bc.Planet;

public abstract class Attacker extends Robot {
    public Attacker(int id) {
        super(id);
    }
    //public abstract boolean attack();

    private boolean inCombat = false;

    public void runAttack() {

        //get correct attack target
        MapLocation attackTarget = null;
        if (Player.gc.planet() == Planet.Earth) {
            attackTarget = Earth.attackTarget;
        } else if (Player.gc.planet() == Planet.Mars) {
            attackTarget = Mars.attackTarget;
        } else {
            System.out.println("ERROR not on planet");
        }


        if (attackTarget != null && !this.inCombat) {
            move(this.getId(),attackTarget);

        } else if(this.inCombat) {
            if(attack()) {
                this.inCombat = false;
            }
            if (attackTarget == null) {

            }
        } else {

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

        attack();
    }


    public boolean attack() {
        // Attack the nearest enemy in attack range

        // If any enemies are still in sight range
        // Return false
        // else
        // Return true;







        //if enemy in range attack
        // else if global location is set, go to global location
        //if nothing at globallocation set empty
        //if globallocation empty, wander and set enemy location
        return true;
    }
}
