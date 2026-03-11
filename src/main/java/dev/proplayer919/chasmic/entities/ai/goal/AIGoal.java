package dev.proplayer919.chasmic.entities.ai.goal;

/**
 * Custom AI goal interface for creature behaviors.
 * Goals are evaluated and executed by the AI system based on priority.
 */
public interface AIGoal {
    /**
     * Check if this goal can start executing.
     * @return true if conditions are met to start this goal
     */
    boolean canStart();

    /**
     * Called when the goal starts executing.
     */
    void start();

    /**
     * Called every tick while the goal is active.
     */
    void tick();

    /**
     * Check if this goal should stop executing.
     * @return true if the goal should end
     */
    boolean shouldEnd();

    /**
     * Called when the goal stops executing.
     */
    void end();

    /**
     * Get the priority of this goal. Higher priority goals are evaluated first.
     * Priority levels:
     * - 1: Highest (fleeing, critical survival)
     * - 2: High (attacking, defending)
     * - 3: Medium (investigating, social behaviors)
     * - 4: Low (wandering, idle behaviors)
     *
     * @return the priority level
     */
    int getPriority();

    /**
     * Check if this goal is currently active.
     * @return true if active
     */
    boolean isActive();
}

