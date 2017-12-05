package BuilderManager;


import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import main.BaseAPI;

import java.util.ArrayList;

/**
 * BUILDER MANAGER
 * Manages the constructing of buildings and looks after keeping idle workers mining minerals.
 * You can pass in jobs of what to build and where to build it and the manager will get a builder for it, send them mining, and will look after replacing builders if they are destroyed.
 * If the building is destroyed that was being constructed, the builder will be returned to mining.
 *
 * REQUIRED USER BOT CALLS
 * onGameStart: call Constructor
 * onFrame: call onFrame
 * onUnitCreate: call unitBuildingStarted
 * onUnitMorph: call unitBuildingStarted
 * onUnitComplete: call unitBuildingComplete
 * onUnitDestroy: call unitBuildingKilled
 *
 */
public class BuilderManager {


    ArrayList<ConstructionJob> allJobs;
    ArrayList<Unit> allBuilders;

    private boolean debugMessagesOn;

    /**
     * Constructor. Initializes the two Arrays of Jobs and Builders.
     * @param debugMessagesOn True if you want the console output debug messages detailing the workings of the production queue. False if you don't.
     */
    public BuilderManager(boolean debugMessagesOn)
    {
        this.debugMessagesOn = debugMessagesOn;
        allJobs = new ArrayList<>();
        allBuilders = new ArrayList<>();
    }

    /**
     * Gets the list of all builders
     * @return ArrayList of all builder units
     */
    public ArrayList<Unit> getBuilders() {return allBuilders;}


    /**
     * Tells the manager to get a builder and start constructing the required building at the location. Assumption is made that the player has the resources to do so when this method is called.
     * @param toBuild The building to construct
     * @param position The location to build it at
     */
    public void addJob(UnitType toBuild, TilePosition position)
    {
        debugMessage("Adding Job");
        Unit newBuilder = getSpareBuilderCloseTo(position.toPosition());
        debugMessage("Got Builder");
        allJobs.add(new ConstructionJob(newBuilder, toBuild, position));
        newBuilder.build(toBuild,position);
    }

    /**
     * Cancels a previously requested job.
     * @param toCancel The type of building to cancel
     * @param position The location it as to be built
     */
    public void cancelJob(UnitType toCancel, TilePosition position)
    {
        for (ConstructionJob currentJob : allJobs)
        {
            if ((currentJob.getToBuild() == toCancel) && (currentJob.getLocation() == position))
            {
                if (currentJob.getStartedBuilding() != null)
                {
                    currentJob.getStartedBuilding().haltConstruction();
                }
                else
                {
                    currentJob.getAssignedBuilder().cancelConstruction();
                }
            }
        }
    }

    /**
     * Call Every frame.
     * Catches any idle workers and sends them mining to their closest mineral source.
     */
    public void onFrame()
    {
        for (Unit currentBuilder : allBuilders)
        {
            if (currentBuilder.isIdle()) {
                currentBuilder.gather(getClosestMineralToUnit(currentBuilder));
                debugMessage("Idle Worker sent mining");
            }

        }
    }

    /**
     * Call to let the manager know that a unit has been started. The manager will then ascertain if it is relevant to the builder manager or not.
     * If it is relevant then the construction job will be marked as started.
     * @param started the unit that has been started
     */
    public void unitBuildingStarted(Unit started)
    {
        if(started.getPlayer().getID() == BaseAPI.baseAPI.self.getID()) {
            if (started.getType().isBuilding()) {
                for (ConstructionJob currentJob : allJobs){
                    if ((currentJob.getToBuild() == started.getType()) && (currentJob.getStartedBuilding() == null)) {
                        currentJob.setStartedBuilding(started);
                        debugMessage("Building Started");
                        return;
                    }
                }
            }
        }
    }

    /**
     * Call to let the Manager know that a unit has been completed. The manager will ascertain if it is relevant.
     * If it is relevant then the manager will either add the builder to the builder list.
     * Or will mark the job as complete and remove it from the list.
     * @param completed The completed unit
     */
    public void unitBuildingComplete(Unit completed)
    {
        if(completed.getPlayer().getID() == BaseAPI.baseAPI.self.getID()) {
            if (completed.getType().isWorker()){
                for (Unit currentBuilder : allBuilders) {
                    if (currentBuilder.getID() == completed.getID())
                        return;
                }
                allBuilders.add(completed);
                debugMessage("Builder Added");
            }

            if (completed.getType().isBuilding())
            {
                ConstructionJob toRemove = null;
                for (ConstructionJob currentJob : allJobs){
                    if (currentJob.getStartedBuilding().getID() == completed.getID()){
                        debugMessage("Found completed job");
                        toRemove = currentJob;
                        break;
                    }
                }
                if (toRemove!=null) {
                    allJobs.remove(toRemove);
                    debugMessage("Job Removed");
                }
            }

        }
    }

    /**
     * Call to inform the manager that a unit or building has been destroyed. The manager will then ascertain if it is relevant to it.
     * If it was a unit relevant to the manager then the manager will either retrieve a new builder or will cancel the job if it was the building destroyed.
     * @param killedUnitBuilding The unit destroyed.
     */
    public void unitBuildingKilled(Unit killedUnitBuilding)
    {
        if(killedUnitBuilding.getPlayer().getID() == BaseAPI.baseAPI.self.getID()) {
            if (killedUnitBuilding.getType().isWorker()){
                for (ConstructionJob currentJob : allJobs){
                    if (currentJob.getAssignedBuilder().getID() == killedUnitBuilding.getID()) {

                        if (currentJob.getStartedBuilding() != null) {
                            Unit newBuilder = getSpareBuilderCloseTo(currentJob.getLocation().toPosition());
                            newBuilder.rightClick(currentJob.getStartedBuilding());
                            currentJob.setAssignedBuilder(newBuilder);
                        }
                        else{
                            Unit newBuilder = getSpareBuilderCloseTo(currentJob.getLocation().toPosition());
                            currentJob.setAssignedBuilder(newBuilder);
                            newBuilder.build(currentJob.getToBuild(),currentJob.getLocation());
                        }
                    }
                }

            }

            if (killedUnitBuilding.getType().whatBuilds().first.isWorker())
            {
                ConstructionJob toRemove = null;
                for (ConstructionJob currentJob : allJobs){
                    if (currentJob.getStartedBuilding().getID() == killedUnitBuilding.getID())
                    {
                        toRemove = currentJob;
                    }
                }
                if (toRemove != null) {
                    debugMessage("Job Removed");
                    allJobs.remove(toRemove);
                }
            }
        }
    }

    /**
     * Retrieves the closest builder that is only mining minerals
     * @param closeTo What you are getting the closest builder to
     * @return the builder
     */
    public Unit getSpareBuilderCloseTo(Position closeTo)
    {
        Unit closestBuilder = null;
        for (Unit builder : allBuilders)
        {
            if ((closestBuilder == null) || (builder.getDistance(closeTo) < closestBuilder.getDistance(closeTo)))
            {
                if (builder.isGatheringMinerals())
                    closestBuilder = builder;
            }
        }
        return closestBuilder;
    }

    /**
     * Finds the closest mineral patch
     * @param closestTo What you are finding the closest mineral patch to
     * @return the mineral patch
     */
    private Unit getClosestMineralToUnit(Unit closestTo)
    {
        Unit closestMineral = null;

        for (Unit neutralUnit : BaseAPI.baseAPI.game.neutral().getUnits())
        {
            if (neutralUnit.getType().isMineralField()) {
                if ((closestMineral == null) || (closestTo.getDistance(neutralUnit) < closestTo.getDistance(closestMineral))) {
                    closestMineral = neutralUnit;
                }
            }
        }
        if (closestMineral != null)
            return closestMineral;
        return null;

    }



    private void debugMessage(String message)
    {
        if (debugMessagesOn)
            System.out.println("BUILDER MANAGER: " + message);
    }

}
