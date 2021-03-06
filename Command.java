/**
 * Commands that will indicate the actions robots need to take for each task
 */
public enum Command {

    // Commands for the global task list
    CONSTRUCT_FACTORY,
    CONSTRUCT_ROCKET,

    // Commands for the robot task list
    BLUEPRINT_FACTORY,
    BLUEPRINT_ROCKET,
    LOAD_ROCKET,
    IN_COMBAT,
    ALERTED,
    WANDER,
    BUILD,
    CLONE,
    MOVE,
    STALL,
}
