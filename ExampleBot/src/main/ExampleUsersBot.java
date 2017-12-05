package main;

import BuilderManager.BuilderManager;
import ProductionQueue.*;
import EnemyBaseTracker.EnemyBaseTrackerManager;
import SquadManager.*;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

import java.util.HashMap;
import java.util.List;


public class ExampleUsersBot extends BaseAPI {

    ProductionQueue productionQueue;
    EnemyBaseTrackerManager enemyBaseTrackerManager;
    BuilderManager builderManager;
    SquadManager squadManager;
    boolean testingBool ;

    int targetBuilderNum;
    int gameProgressionTrack;

    int bunkerSquadRef;
    int scoutSquadRef;
    int defenseSquadRef;

    public static void main(String[] args) {
        new ExampleUsersBot().run();
    }

    @Override
    public void onStart()
    {
        /**
         * The super class on start method initialises the Brood War Terrain Analyser
         * Along with enabling the flag that allows user input during a bots game. This is enabled to allow for easier bot testing.
         * This needs to be called for the Base API access functions to work later on down the line for access to the player and game data.
         */
        super.onStart();

        System.out.println("UsersBot Setup");

        /**
         * Simple Testing Variables.
         */
        targetBuilderNum = 10;
        gameProgressionTrack = 0;
        testingBool = false;

        //SQUAD MANAGER
        squadManager = new SquadManager(true);

        //BUILDER MANAGER
        builderManager = new BuilderManager(false);


        //CONSTRUCTION QUEUE
        productionQueue = new ProductionQueue(false,true);
        productionQueue.onGameStart();


        //ENEMY BASE TRACKER
        enemyBaseTrackerManager = new EnemyBaseTrackerManager(true);
        enemyBaseTrackerManager.createTrackersForAllEnemies();


    }

    @Override
    public void onFrame()
    {
        enemyBaseTrackerManager.onFrame();
        builderManager.onFrame();
        squadManager.onFrame();


        ProductionOrder toExecute;
        toExecute = productionQueue.Update();

        if (toExecute != null){
            if (toExecute instanceof UnitBuildingOrder) {
                if (((UnitBuildingOrder) toExecute).getToProduce().isBuilding())
                    builderManager.addJob(((UnitBuildingOrder) toExecute).getToProduce(), getBuildTile(((UnitBuildingOrder) toExecute).getToProduce(), BaseAPI.baseAPI.self.getStartLocation()));
                else
                {
                    UnitType needed = ((UnitBuildingOrder) toExecute).getToProduce().whatBuilds().first;
                    getFree(needed).train(((UnitBuildingOrder) toExecute).getToProduce());
                }
            }
            else if(toExecute instanceof ResearchOrder) {
                TechType toResearch = ((ResearchOrder) toExecute).getToProduce();
                UnitType needed = ((ResearchOrder) toExecute).getToProduce().whatResearches();
                getFree(needed).research(toResearch);
            }
            else if(toExecute instanceof UpgradeOrder) {
                UpgradeType toUpgrade = ((UpgradeOrder) toExecute).getToProduce();
                UnitType needed = ((UpgradeOrder) toExecute).getToProduce().whatUpgrades();
                getFree(needed).upgrade(toUpgrade);
            }
        }


        if ((self.supplyTotal() - self.supplyUsed()) <= 6 && (self.supplyTotal() != 400)) { //Supply in BWAPI is doubled so that zerglings tat are normally 0.5 supply in StarCraft can have a full supply. Every value is doubled
            if (!productionQueue.checkIfHaveInProduction(UnitType.Terran_Supply_Depot)){
                productionQueue.AddToQueue(UnitType.Terran_Supply_Depot, 2);
            }
        }

        if (builderManager.getBuilders().size() < targetBuilderNum) {
            if (!productionQueue.checkIfHaveInProduction(UnitType.Terran_SCV)) {
                productionQueue.AddToQueue(UnitType.Terran_SCV, 2);
            }
        }

        gameProgressionUpdate();
    }


    private void gameProgressionUpdate()
    {
        HashMap<UnitType, Integer> makeup;
        BaseAPI.baseAPI.game.drawTextScreen(400, 25, "Game Progression Track: " + gameProgressionTrack);
        switch (gameProgressionTrack)
        {
            // make builders and supply depots
            case 0:
                if (builderManager.getBuilders().size() < 10) {
                    break;
                } //on workers finished

                gameProgressionTrack = 1;

                makeup = new HashMap<>();
                makeup.put(UnitType.Terran_Marine, 4);
                bunkerSquadRef = squadManager.createSquad(makeup);
                for (HashMap.Entry<UnitType, Integer> entry : squadManager.getSquad(bunkerSquadRef).getSquadNeeds().entrySet())
                {
                    for (int i = 0; i < entry.getValue(); i++)
                        productionQueue.AddToQueue(entry.getKey(),3);
                }
                productionQueue.AddToQueue(UnitType.Terran_Refinery, 4);
                productionQueue.AddToQueue(UnitType.Terran_Bunker,2);
                targetBuilderNum = 15;

                //squad 4 marines - bunker squad
                //refinery
                //bunker
                break;

            case 1:
                if (getType(UnitType.Terran_Bunker) == null)
                    break;
                if (!squadManager.getSquad(bunkerSquadRef).getWasComplete() || !getType(UnitType.Terran_Bunker).isCompleted()){
                    break;
                }

                gameProgressionTrack = 2;

                squadManager.getSquad(bunkerSquadRef).setSquadRightClick(getType(UnitType.Terran_Bunker));
                productionQueue.AddToQueue(UnitType.Terran_Barracks, 2);

                makeup = new HashMap<>();
                makeup.put(UnitType.Terran_Marine, 6);
                makeup.put(UnitType.Terran_Medic, 2);
                scoutSquadRef = squadManager.createSquad(makeup);
                for (HashMap.Entry<UnitType, Integer> entry : squadManager.getSquad(scoutSquadRef).getSquadNeeds().entrySet())
                {
                    for (int i = 0; i < entry.getValue(); i++)
                        productionQueue.AddToQueue(entry.getKey(),3);
                }

                //on squad + bunker complete
                //put squad in bunker
                //barracks
                //squad 6 marines + 2 medics
                break;
            case 2:
                if (getType(UnitType.Terran_Academy) == null)
                    break;
                if (!squadManager.getSquad(scoutSquadRef).getWasComplete() || !getType(UnitType.Terran_Academy).isCompleted()){
                    break;
                }

                gameProgressionTrack = 3;

                for (BaseLocation b : BWTA.getBaseLocations()) {
                    // If this is a possible start location,
                    if (b.isStartLocation()) {
                        // do something. For example send some unit to attack that position:
                        if (!BaseAPI.baseAPI.game.isVisible(b.getTilePosition()))
                            squadManager.getSquad(scoutSquadRef).setSquadMoveTarget(b.getPosition(), true);
                    }
                }
                productionQueue.AddToQueue(TechType.Stim_Packs, 2);

                makeup = new HashMap<>();
                makeup.put(UnitType.Terran_Marine, 6);
                makeup.put(UnitType.Terran_Medic, 2);
                defenseSquadRef = squadManager.createSquad(makeup);
                for (HashMap.Entry<UnitType, Integer> entry : squadManager.getSquad(defenseSquadRef).getSquadNeeds().entrySet())
                {
                    for (int i = 0; i < entry.getValue(); i++)
                        productionQueue.AddToQueue(entry.getKey(),3);
                }

                //squad + academy complete
                //send squad exploring
                //stim
                //squad 5 marines + 2 medics
                break;
            case 3:
                if (!squadManager.getSquad(defenseSquadRef).getWasComplete() || !self.hasResearched(TechType.Stim_Packs)){
                    break;
                }
                gameProgressionTrack = 4;
                productionQueue.AddToQueue(UpgradeType.Terran_Infantry_Weapons, 3, 1);
                //squad and stim complete
                //weap +1
                break;
        }
    }


    @Override
    public void onUnitCreate(Unit unit)
    {
        productionQueue.unitStructureStarted(unit);
        builderManager.unitBuildingStarted(unit);
    }

    @Override
    public void onUnitMorph(Unit unit)
    {
        builderManager.unitBuildingStarted(unit);
        productionQueue.unitStructureStarted(unit);
    }

    @Override
    public void onUnitComplete(Unit unit)
    {
        builderManager.unitBuildingComplete(unit);

        for (Squad currentSquad : squadManager.getAllSquads()) {
            if (currentSquad.getSquadNeeds().get(unit.getType()) != null) {
                if (currentSquad.getSquadNeeds().get(unit.getType()) > 0) {
                    currentSquad.addUnitToSquad(unit);
                    return;
                }
            }
        }
    }

    @Override
    public void onUnitDestroy(Unit unit)
    {
        productionQueue.unitStructureDestroyed(unit);
        builderManager.unitBuildingKilled(unit);
        enemyBaseTrackerManager.unitDestroyed(unit);
        squadManager.onUnitDestroy(unit);

        HashMap<UnitType, Integer> allSquadNeeds = squadManager.getAllSquadNeeds();
        int currentCount = 0;
        for (HashMap.Entry<UnitType, Integer> entry : allSquadNeeds.entrySet()) {
            currentCount = productionQueue.checkHowManyInProduction(entry.getKey());
            if (currentCount < entry.getValue()){
                for (int i = currentCount; i < entry.getValue(); i++)
                    productionQueue.AddToQueue(entry.getKey(),3);
            }
        }
    }

    @Override
    public void onUnitDiscover(Unit unit)
    {

        enemyBaseTrackerManager.unitFound(unit);
    }


    private Unit getFree(UnitType ofType)
    {
        List<Unit> allUnits = BaseAPI.baseAPI.self.getUnits();
        for (Unit currentUnit : allUnits) {
            if (currentUnit.getType() == ofType && currentUnit.isIdle() && currentUnit.isCompleted()) {
                return currentUnit;
            }
        }
        return null;
    }

    private Unit getType(UnitType ofType)
    {
        for (Unit currentUnit : self.getUnits())
        {
            if (currentUnit.getType() == ofType)
                return currentUnit;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Example BuildTile Function provided by http://sscaitournament.com/index.php?action=tutorial
    //This example is a simple and inefficient search based implementation, as creating a good building
    //placement algorithm is a complicated procedure

    //example has been edited to make static for easier use

    // Returns a suitable TilePosition to build a given building type near
    // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
    public static TilePosition getBuildTile(UnitType buildingType, TilePosition aroundTile) {
        TilePosition ret = null;
        int maxDist = 3;
        int stopDist = 40;

        // Refinery, Assimilator, Extractor
        if (buildingType.isRefinery()) {
            for (Unit n : BaseAPI.baseAPI.game.neutral().getUnits()) {
                if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
                        ( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
                        ( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
                        ) return n.getTilePosition();
            }
        }

        while ((maxDist < stopDist) && (ret == null)) {
            for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
                for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
                    if (BaseAPI.baseAPI.game.canBuildHere(new TilePosition(i,j), buildingType)) {
                        // units that are blocking the tile
                        boolean unitsInWay = false;
                        for (Unit u : BaseAPI.baseAPI.game.getAllUnits()) {
                            //if (u.getID() == builder.getID()) continue;
                            if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
                        }
                        if (!unitsInWay) {
                            return new TilePosition(i, j);
                        }
                        // creep for Zerg
                        if (buildingType.requiresCreep()) {
                            boolean creepMissing = false;
                            for (int k=i; k<=i+buildingType.tileWidth(); k++) {
                                for (int l=j; l<=j+buildingType.tileHeight(); l++) {
                                    if (!BaseAPI.baseAPI.game.hasCreep(k, l)) creepMissing = true;
                                    break;
                                }
                            }
                            if (creepMissing) continue;
                        }
                    }
                }
            }
            maxDist += 2;
        }

        if (ret == null) BaseAPI.baseAPI.game.printf("Unable to find suitable build position for "+buildingType.toString());
        return ret;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



}
