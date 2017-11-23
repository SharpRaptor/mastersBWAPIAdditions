package EnemyBaseTracker;


import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;
import main.BaseAPI;

import java.util.ArrayList;

/**
 * Manager that holds all of the different Enemy Base Trackers. Then allows for accessing each trackers records while keeping them up to date.
 *
 * REQUIRED USER BOT CALLS:
 * onGameStart - Call Constructor. Then call createTrackersForAllEnemies
 * onFrame - Call onFrame
 * onUnitFound - Call foundUnit
 * onUnitDestroy - Call destroyedUnit
 */

public class EnemyBaseTrackerManager {

    private ArrayList<EnemyBaseTracker> allBaseTrackers;

    private boolean visualsOn;
    /**
     * Setup of the EnemyBaseTracker Manager. Needs to be run before any other method.
     * @param visualsOn True if the on screen visual representations of unit locations are wanted
     */
    public EnemyBaseTrackerManager(boolean visualsOn)
    {
        this.visualsOn = visualsOn;
        allBaseTrackers = new ArrayList<>();
    }

    /**
     * Runs the Update method for each EnemyTracker. Allowing each record to stay up to date with the most recent movement information and unit status.
     */
    public void onFrame()
    {
        for (EnemyBaseTracker currentTracker : allBaseTrackers)
            currentTracker.onFrame(visualsOn);
    }

    /**
     * Creates new Trackers for each enemy in the game. Must be run in the game start method.
     */
    public void createTrackersForAllEnemies()
    {
        for (Player currentPlayer : BaseAPI.baseAPI.game.enemies())
        {
            allBaseTrackers.add(new EnemyBaseTracker(currentPlayer));
        }
    }

    /**
     * Gets all MemoryUnitBuilding records from the tracker for the requested memory.
     * @param forEnemy The enemy you want the records for.
     * @return ArrayList of MemoryUnitBuildings: All current records for that enemy.
     */
    public ArrayList<MemoryUnitBuilding> getEnemyUnitBuildingList(Player forEnemy)
    {
        for (EnemyBaseTracker currentTracker : allBaseTrackers)
        {
            if (currentTracker.getAssociatedPlayer().getID() == forEnemy.getID())
                return currentTracker.getAllUnits();
        }
        return null;
    }

    /**
     * Checks to see if a given enemy has a unit or building
     * @param type The unit or building to check for
     * @param forEnemy The enemy to check for
     * @return True if that enemy has been discovered to have that unit or building. False if they don't, or it hasn't been discovered that they do.
     */
    public boolean checkIfEnemyHas(UnitType type, Player forEnemy){
        for (EnemyBaseTracker currentTracker : allBaseTrackers) {
            if (currentTracker.getAssociatedPlayer().getID() == forEnemy.getID()) {
                return currentTracker.doesEnemyHave(type);
            }
        }
        return false;
    }

    /**
     * Tell the manager than a unit has been found. Manager will discern which player its from and add it to the records if pertinent.
     * @param foundUnit The unit found.
     */
    public void unitFound(Unit foundUnit)
    {
        for (EnemyBaseTracker currentTracker : allBaseTrackers) {
            if (currentTracker.getAssociatedPlayer().getID() == foundUnit.getPlayer().getID())
            {
                currentTracker.addUnitIfNew(foundUnit);
            }
        }
    }

    /**
     * Tell the manager than a unit has been destroyed. Manager will discern which player its from and amend it in the records if pertinent.
     * @param destroyedUnit The unit destroyed.
     */
    public void unitDestroyed(Unit destroyedUnit)
    {

        for (EnemyBaseTracker currentTracker : allBaseTrackers) {
            if (currentTracker.getAssociatedPlayer().getID() == destroyedUnit.getPlayer().getID())
            {
                currentTracker.unitDestroyed(destroyedUnit);
            }
        }
    }
}
