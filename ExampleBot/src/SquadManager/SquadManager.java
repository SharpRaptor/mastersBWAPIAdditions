package SquadManager;


import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * SQUAD MANAGER
 * Stores all players squads. Allows for the creation of squads with unit type lists for what they should consist of.
 * The squad will then keep a list of what units are assigned to it and can compare what it has to what its supposed to have.
 * The squad allows for groups of units to referred to easily on mass to give targets or movement direction.
 *
 * REQUIRED USER BOT CALLS
 * onGameStart : constructor
 * onFrame : onFrame
 * onUnitDestroy : onUnitDestroy
 */
public class SquadManager {

    ArrayList<Squad> allSquads;
    int currentSquadMaxID;
    boolean removeDeadSquads;

    boolean debugMessagesOn;

    public SquadManager(boolean debugMessagesOn){
        this.debugMessagesOn = debugMessagesOn;
        currentSquadMaxID = 0;
        allSquads = new ArrayList<>();
        removeDeadSquads = true;

    }

    /**
     * Create a new squad with the desired unit make up
     * @param squadMakeup The Map of units that are to be created. HashMap of Unit Types to the integer count of them
     * @return the unique squad ID
     */
    public int createSquad(HashMap<UnitType, Integer> squadMakeup)
    {
        currentSquadMaxID++;
        allSquads.add(new Squad(squadMakeup,currentSquadMaxID,debugMessagesOn));
        return currentSquadMaxID;
    }

    /**
     * Disbands the squad by removing it from the list of squads. Units that were in the squad will no longer return true to is unit in a squad
     * @param squadID The unique squad ID to be disbanded
     */
    public void disbandSquad(int squadID)
    {
        Squad toRemove = null;

        for (Squad currentSquad : allSquads) {
            if (currentSquad.getID() == squadID) {
                toRemove = currentSquad;
                break;
            }
        }

        if(toRemove!=null)
        {
            allSquads.remove(toRemove);
        }
    }

    /**
     * Gets all the squads
     * @return ArrayList of Squads. All the squads in memory
     */
    public ArrayList<Squad> getAllSquads()
    {
        return allSquads;
    }

    /**
     * Gets the squad with the requested ID
     * @param squadID the ID of the squad requested
     * @return The requested Squad if it exists
     */
    public Squad getSquad(int squadID)
    {
        for (Squad currentSquad : allSquads)
        {
            if (currentSquad.getID() == squadID)
                return currentSquad;
        }
        return null;
    }

    /**
     * Lets the manager know a unit has been destroyed. This will let each squad check to see if it was one of there units destroyed.
     * @param destroyedUnit The destroyed Unit
     */
    public void onUnitDestroy(Unit destroyedUnit)
    {
        for (Squad currentSquad : allSquads) {
            currentSquad.onUnitDestroy(destroyedUnit);
        }
    }

    /**
     * Each frame checks to see if any squads are now dead and will remove them from the list if the flag RemoveDeadSquads is true.
     */
    public void onFrame()
    {
        if(removeDeadSquads) {


            ArrayList<Squad> toRemove = new ArrayList<Squad>();
            for (Squad current : allSquads) {
                if (current.getIsDead()) {
                    toRemove.add(current);
                }
            }
            for (Squad current : toRemove) {
                allSquads.remove(current);
            }
        }
    }

    /**
     * Set to true by default, this will remove squads from the squad list if all units in it are dead and at some point it was complete.
     * If false then the squad will remain in the list, just with no units assigned.
     * @param setToo Set the parameter to. True if want to remove the dead squads, false if want to leave them.
     */
    public void setRemoveDeadSquads(boolean setToo) {this.removeDeadSquads = setToo;}

    /**
     * Runs a check in each squad to see if the requested unit is attached to that squad
      * @param unitToCheck The unit to check if it belongs to a squad
     * @return True if the unit is in a squad. False if it isn't
     */
    public boolean isUnitAssignedToSquad(Unit unitToCheck)
    {
        for (Squad currentSquad : allSquads)
        {
            for (Unit currentUnit : currentSquad.getAssignedUnits())
            {
                if (unitToCheck.getID() == currentUnit.getID())
                    return true;
            }
        }
        return false;
    }

    /**
     * Gets the needs of all squads compiled into one hashmap
     * @return Hashmap of UnitType : Integer of all the squad needs
     */
    public HashMap<UnitType, Integer> getAllSquadNeeds()
    {
        HashMap<UnitType, Integer> currentSquadNeeds = new HashMap<>();
        HashMap<UnitType, Integer> toReturn = new HashMap<>();

        for (Squad currentSquad : allSquads)
        {
            currentSquadNeeds = currentSquad.getSquadNeeds();
            for (HashMap.Entry<UnitType, Integer> entry : currentSquadNeeds.entrySet()) {
                if (toReturn.get(entry.getKey()) != null){
                    toReturn.put(entry.getKey(), entry.getValue() + toReturn.get(entry.getKey()));
                }
                else{
                    toReturn.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return toReturn;
    }

    private void debugMessage(String message)
    {
        if (debugMessagesOn)
            System.out.println("SQUAD MANAGER: " + message);
    }


}
