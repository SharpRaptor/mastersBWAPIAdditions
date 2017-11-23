package EnemyBaseTracker;

import bwapi.Color;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import main.BaseAPI;

/**
 * Storage Class for details about an enemy unit. When a unit is no longer visible, it's details can't be accessed so a few pertinent details are stored along with the unit class.
 */
public class MemoryUnitBuilding {

    private Unit unit;
    private UnitType lastKnownType;
    private TilePosition lastKnownPosition;
    private boolean isVisible;

    public MemoryUnitBuilding(Unit unit)
    {
        this.unit = unit;
        this.lastKnownType = unit.getType();
        this.lastKnownPosition = unit.getTilePosition();
        this.isVisible = unit.isVisible();
    }

    /**
     * Gets the unit that this Memory contains
     * @return the contained memory unit
     */
    public Unit getUnit() {return unit;}

    /**
     * Gets the last known unit type of this unit. Changes will happen most often with Zerg morphing units
     * @return The last known Unit type of the unit.
     */
    public UnitType getLastKnownUnitType() {return lastKnownType;}

    /**
     * Gets the last seen location of the unit when it disappeared into the fog of war
     * @return Tile position of last known location.
     */
    public TilePosition getLastKnownPosition() {return lastKnownPosition;}

    /**
     * Returns whether the unit is currently visible
     * @return Boolean True if the unit is visible and the details are now accessable. False if not.
     */
    public boolean isVisible() {return isVisible;}

    /**
     * Updates the information held in the Memory
     */
    public void update(boolean visuals)
    {
        isVisible = unit.isVisible();
        if (unit.isVisible())
        {
            if (visuals)
                BaseAPI.baseAPI.game.drawCircleMap(unit.getPosition(),10, Color.Green);
            lastKnownType = unit.getType();
            lastKnownPosition = unit.getTilePosition();
        }
        else{
            if (visuals)
                BaseAPI.baseAPI.game.drawCircleMap(lastKnownPosition.toPosition(),10, Color.Red);
        }


    }
}
