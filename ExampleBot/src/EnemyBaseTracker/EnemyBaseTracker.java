package EnemyBaseTracker;


import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;

/**
 * Tracker for an individual enemy. Used by the EnemyBaseTrackerManager.
 */
public class EnemyBaseTracker {

    Player associatedPlayer;
    ArrayList<MemoryUnitBuilding> enemiesUnits;

    /**
     * Setup the Tracker. Pass in the player its attached to.
     * @param associatedPlayer The player that the tracker will keep track of.
     */
    public EnemyBaseTracker(Player associatedPlayer)
    {
        enemiesUnits = new ArrayList<>();
        this.associatedPlayer = associatedPlayer;
    }

    /**
     * Returns all the Memory Units that the tracker has
     * @return All the memory entries the tracker has
     */
    public ArrayList<MemoryUnitBuilding> getAllUnits(){return new ArrayList<>(enemiesUnits);}

    /**
     * Get the associated player
     * @return Returns the player that is associated with the tracker
     */
    public Player getAssociatedPlayer() {return associatedPlayer;}

    /**
     * Checks to see if the unit is one that there isn't a memory for yet by checking the unique ID. Then if it is new, adding it to the memory banks
     * @param unitToAdd The unit that has been found
     */
    public void addUnitIfNew(Unit unitToAdd)
    {
        for (MemoryUnitBuilding currentMemory : enemiesUnits)
        {
            if (unitToAdd.getID() == currentMemory.getUnit().getID())
                return;
        }
        enemiesUnits.add(new MemoryUnitBuilding(unitToAdd));
    }

    /**
     * Checks to see if the destroyed unit was one that was on record. And if so removes it from the record
     * @param toDestroy The unit that was destroyed
     */
    public void unitDestroyed(Unit toDestroy)
    {
        MemoryUnitBuilding toRemove = null;
        for (MemoryUnitBuilding currentMemory : enemiesUnits)
        {
            if (toDestroy.getID() == currentMemory.getUnit().getID())
                toRemove = currentMemory;
        }
        if (toRemove != null)
            enemiesUnits.remove(toRemove);
    }

    /**
     * Runs all the updates on each memory entry
     * @param visuals True if the onscreen drawn location representers are wanted
     */
    public void onFrame(boolean visuals)
    {
        for (MemoryUnitBuilding currentMemory : enemiesUnits) {
            currentMemory.update(visuals);
        }
    }


    /**
     * Checks to see if this enemy has a unit of the type passed in
     * @param typeToCheck The UnitType to check for
     * @return True if this enemy has a unit of that type. False if they do not.
     */
    public boolean doesEnemyHave(UnitType typeToCheck)
    {
        for (MemoryUnitBuilding currentMemory : enemiesUnits) {
            if (currentMemory.getUnit().getType() == typeToCheck)
                return true;
        }
        return false;
    }


}
