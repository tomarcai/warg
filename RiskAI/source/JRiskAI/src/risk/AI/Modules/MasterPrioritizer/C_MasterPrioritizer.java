package risk.AI.Modules.MasterPrioritizer;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_Ownership;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Input.I_MP_GoalDistribution;
import java.util.*;
import risk.AI.Data_Structures.T_Game;
import neuralnetwork.FeedForwardNetwork;
import trainingdataconverter.trainingdata.GameData;

public class C_MasterPrioritizer {
	
	private Random rand = new Random();
	
	private String type;
	private O_MP_GoalDistribution output;
	private int currentPlayerIndex;
	private Mission ownMission;
	private List<Mission> missions;
	private List<Continent> continents;
	private List<Player> playersInGame;
	private risk.AI.Data_Structures.T_Game game;
	private T_TrainingExampleWriter trainingExample;
	//private O_IG_NextMoveEstimate nextMove;
	private FeedForwardNetwork neuralNetwork = null;
	private C_Timing timer = new C_Timing();		
	
	public C_MasterPrioritizer(String type, int currentPlayerIndex, Mission ownMission, List continents, T_Game game, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.currentPlayerIndex = currentPlayerIndex;
		this.continents = continents;
		this.missions = game.getMissions();
		this.ownMission = ownMission;
		this.game = game;
		this.playersInGame = game.getPlayers();
		this.trainingExample = trainingExample;
		loadModel();
	}
	
	/**
	 * Loads the model for the chosen AI technique and measures the time it takes to do it.
	 * To rule out disk access time the model will be loaded twice, and only the load time
	 * for the second time will be measured. This will give the time it takes to build the
	 * model structure in memory.
	 */
	private void loadModel() {
		long loadTime = 0;
		if (type.equals("bn") || type.equals("dt") || type.equals("nb") || type.equals("nn") || type.equals("nn_wo")) {
			doLoadModel();
			timer.startTimer();
			doLoadModel();
			timer.endTimer();
			loadTime = timer.getTimer();
		}
		trainingExample.setModuleLoadTime(currentPlayerIndex, "master_prioritizer", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt")) {
			// To do...
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "master_prioritizer/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "master_prioritizer/nn_wo/data.nnmodel");
		}
	}
	
	public void run(I_MP_GoalDistribution input, boolean isOpponentFramework) {
		if (type.equals("random")) {
			random_run();
		} else {
			timer.startTimer();
			if(type.equals("script")) {
				script_run(input.getMissionEstimate(), input.getNextMoveEstimate(), input.getContinentEstimate(), input.getWinningEstimate(), input.getOwnership());
			} else if (type.equals("dt")) {
				dt_run(input.getMissionEstimate(), input.getNextMoveEstimate(), input.getContinentEstimate(), input.getWinningEstimate(), input.getOwnership());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getMissionEstimate(), input.getNextMoveEstimate(), input.getContinentEstimate(), input.getWinningEstimate(), input.getOwnership());
			}
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(currentPlayerIndex, "master_prioritizer", timer.getTimer());
				trainingExample.saveModule(input,output,(Player)game.getPlayers().get(this.currentPlayerIndex));
			}
		}
	}
	
	public O_MP_GoalDistribution getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run() {
		output = new O_MP_GoalDistribution();
		float[] dist = new float[38];
		for (int x = 0; x < 38; x++) {
			dist[x] = rand.nextFloat();
		}
		output.setFullDistribution(dist);
	}
	
	//Scripted AI
	private void script_run(O_IG_MissionEstimate opp_mission,
		O_IG_NextMoveEstimate opp_nextmove,
		O_IG_ContinentEstimate opp_continentestimate,
		O_IG_WinningEstimate opp_winningestimate,
		O_IG_Ownership continentOwner) {
		output = new O_MP_GoalDistribution();
		
		
		float[] cd_dist = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		float[] c_dist = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		float[] d_dist = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		float[] o_dist = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		float[] od_dist = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		float[] a_dist = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		float eighteen_dist = 0.0f;
		float twentyfour_dist = 0.0f;
		boolean concealMission = false;
		float maxValue = 1.0f;
		//Mission ownMission = playersInGame.get(currentPlayerIndex).getMission();
		
		//Determine if the AI should conceal its mission.
		if(opp_winningestimate.getEstimate(currentPlayerIndex, 0) < 0.98f){
			if(opp_mission.getEstimate(currentPlayerIndex, missions.indexOf(ownMission)) > 0.75f){//set back to 0.75
				for(int cn = 0; cn < opp_continentestimate.getNumOfContinents(); cn ++){
					if(ownMission.getContinent1() == continents.get(cn)){
						d_dist[cn] += 0.1f;
						if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
					}
					if(ownMission.getContinent2() == continents.get(cn)){
						d_dist[cn] += 0.1f;
						if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
					}
				}
				for(int pn = 0; pn < opp_continentestimate.getNumOfPlayers(); pn++){
					if(playersInGame.get(pn) != ownMission.getPlayer()){
						a_dist[pn] += 0.1f;
						if(a_dist[pn] > maxValue){a_dist[pn]=maxValue;}
					} else{
						a_dist[pn] += 0.07f;
						if(a_dist[pn] > maxValue){a_dist[pn]=maxValue;}
					}
				}
			} else{
				//Distribute points to "conquer and defend" goals.
				//The following gives points to continents the player is close to occupying.
				float i = 0.75f;
				float thePoint = 0.4f;
				float pointGiven;
				float pointReduction = 0.05f;
				if(opp_winningestimate.getEstimate(currentPlayerIndex, 0) < 0.9f){
					//The following gives points to continents in the players mission.
					
					if(ownMission.getContinent1() != null){
						boolean ownContinent1 = continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent1()));
						if(!continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent1()))){
							cd_dist[continents.indexOf(ownMission.getContinent1())] += 0.5f;
							if(cd_dist[continents.indexOf(ownMission.getContinent1())] > maxValue){cd_dist[continents.indexOf(ownMission.getContinent1())]=maxValue;}
						}
					}
					
					if(ownMission.getContinent2() != null){
						boolean ownContinent2 = continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent2()));
						if(!continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent2()))){
							cd_dist[continents.indexOf(ownMission.getContinent2())] += 0.5f;
							if(cd_dist[continents.indexOf(ownMission.getContinent2())] > maxValue){cd_dist[continents.indexOf(ownMission.getContinent2())]=maxValue;}
						}
					}
					
					for(int cn=0; cn < opp_continentestimate.getNumOfContinents(); cn++){
						boolean done = false;
						if(!continentOwner.getIsOwned(currentPlayerIndex,cn) && opp_continentestimate.getEstimate(currentPlayerIndex, cn, 0)>0.9f){
							cd_dist[cn] += 0.45f;
						}else{
							i = 0.85f;
							pointGiven = thePoint;
							if(!continentOwner.getIsOwned(currentPlayerIndex,cn)){
								for(int round=0; round<5; round++){
									if(!done && opp_continentestimate.getEstimate(currentPlayerIndex, cn, round)>i){
										cd_dist[cn] += pointGiven;
										done = true;
									} else{
										i += 0.03f;
										pointGiven -= pointReduction;
									}
								}
							}
						}
					}
					
				} else {
					//Conquer Continent.
					//The following gives points to continents in the players mission.
					
					
					if(ownMission.getContinent1() != null){
						if(!continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent1()))){
							c_dist[continents.indexOf(ownMission.getContinent1())] += 1.0f;
							if(c_dist[continents.indexOf(ownMission.getContinent1())] > maxValue){c_dist[continents.indexOf(ownMission.getContinent1())]=maxValue;}
						}
					}
					if(ownMission.getContinent2() != null){
						if(!continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent2()))){
							c_dist[continents.indexOf(ownMission.getContinent2())] += 1.0f;
							if(c_dist[continents.indexOf(ownMission.getContinent2())] > maxValue){c_dist[continents.indexOf(ownMission.getContinent2())]=maxValue;}
						}
					}
					
					
					for(int cn=0; cn < opp_continentestimate.getNumOfContinents(); cn++){
						boolean done = false;
						if(!continentOwner.getIsOwned(currentPlayerIndex,cn) && opp_continentestimate.getEstimate(currentPlayerIndex, cn, 0)>0.9f){
							c_dist[cn] += 0.45f;
						}else{
							i = 0.85f;
							pointGiven = thePoint;
							if(!continentOwner.getIsOwned(currentPlayerIndex,cn)){
								for(int round=0; round<5; round++){
									if(!done && opp_continentestimate.getEstimate(currentPlayerIndex, cn, round)>i){
										c_dist[cn] += pointGiven;
										done = true;
									} else{
										i += 0.03f;
										pointGiven -= pointReduction;
									}
								}
							}
							
						}
						if(c_dist[cn] > maxValue){c_dist[cn]=maxValue;}
					}
				}
				
				
				//Defend continent.
				Vector territoriesInDanger = new Vector();
				territoriesInDanger = opp_nextmove.getTerritoryInDangerList();
				for(int cn = 0; cn < opp_continentestimate.getNumOfContinents(); cn++){
					
					boolean ownsContinent = continentOwner.getIsOwned(currentPlayerIndex, cn);
					if(continentOwner.getIsOwned(currentPlayerIndex, cn)){
						d_dist[cn] += 0.6;
						if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
						
						if(((ownMission.getContinent1() == (Continent)continents.get(cn)) ||
							(ownMission.getContinent2() == (Continent)continents.get(cn)))){
							if(opp_winningestimate.getEstimate(currentPlayerIndex,0)<0.9){
								d_dist[cn] += 0.4f;
								if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
							}
							
						}
						if(territoriesInDanger != null){
							//give extra points for each occupied territory in an opponent's attackplan
							for(int k = 0; k < territoriesInDanger.size();k++){
								if(((Country)territoriesInDanger.get(k)).getOwner() == playersInGame.get(currentPlayerIndex)){
									if((continents.get(cn)).getTerritoriesContained().contains(territoriesInDanger.get(k))){
									}
									d_dist[cn] += 0.1f;
									if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
								}
							}
						}
					}
				}
				
				
			/*	//Defend: Continent is not mission specific.
				if((ownMission.getContinent1() != (Continent)continents.get(cn)) &&
					(ownMission.getContinent2() != (Continent)continents.get(cn)) &&
					continentOwner.getIsOwned(currentPlayerIndex, cn)){
					d_dist[cn] += 0.2f;
					if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
					if(territoriesInDanger != null){
						//assign points to the continent if the player is occupying it, and
						//an occupied territory is in an opponent's attackplan.'
						for(int k = 0; k < territoriesInDanger.size();k++){
							if((continents.get(cn)).getTerritoriesContained().contains(territoriesInDanger.get(k))){
								d_dist[cn] += 0.1f;
								if(d_dist[cn] > maxValue){d_dist[cn]=maxValue;}
							}
						}
					}
				}
			}*/
				//Distribute O&D and O goals.
				for(int cn = 0; cn < opp_continentestimate.getNumOfContinents(); cn++){
					for(int pn = 0; pn < opp_continentestimate.getNumOfPlayers(); pn++){
						if(currentPlayerIndex != pn){
							if(continentOwner.getIsOwned(pn, cn)){
								//Check if opponent is estimated to win within the next two rounds and if the estimate of the mission is above 50%.
								if(opp_winningestimate.getEstimate(pn,2) >= 0.999f){
									if(opp_mission.getEstimate(pn, missions.indexOf(opp_mission.getMostLikelyMission(pn, missions))) > 0.5f){
										//Assign O&D points to continents in the opponents mission.
										if(opp_mission.getMostLikelyMission(pn, missions).getContinent1() == continents.get(cn) ||
											opp_mission.getMostLikelyMission(pn, missions).getContinent2() == continents.get(cn)){
											od_dist[cn] += 0.3f;
											//Give more points if the player is the one in the AI's mission.
											if(ownMission.getPlayer()!=null){
												if(continentOwner.getIsOwned(game.getPlayerIndex(ownMission.getPlayer()),cn)){
													o_dist[cn] += 0.2f;
													if(o_dist[cn] > maxValue){o_dist[cn]=maxValue;}
												}
											}
											//Assign more points if the continent is mission specific to the AI.
											if((ownMission.getContinent1() == (Continent)continents.get(cn)) ||
												(ownMission.getContinent2() == (Continent)continents.get(cn))){
												od_dist[cn] += 0.2;
											}
											if(od_dist[cn] > maxValue){od_dist[cn]=maxValue;}
										} else{
											//If the continent is not mission specific to the AI, assign points to O&D goal only if continent is
											//mission specific to the AI. Else assign points to O on the continent.
											if((ownMission.getContinent1() == (Continent)continents.get(cn)) ||
												(ownMission.getContinent2() == (Continent)continents.get(cn))){
												od_dist[cn] += 0.3;
											}else{
												o_dist[cn] += 0.2f;
												//Give more points if the player is the one in the AI's mission.
												if(ownMission.getPlayer()!=null){
													if(continentOwner.getIsOwned(game.getPlayerIndex(ownMission.getPlayer()),cn)){
														o_dist[cn] += 0.2f;
														if(o_dist[cn] > maxValue){o_dist[cn]=maxValue;}
													}
												}
											}
										}
										if(o_dist[cn] > maxValue){o_dist[cn]=maxValue;}
									}
								} else{
									//If the opponent occupying the continent is not winning, assign points to O&D only if they are
									//mission specific to the AI. Else assign points to O only.
									if((ownMission.getContinent1() == (Continent)continents.get(cn)) ||
										(ownMission.getContinent2() == (Continent)continents.get(cn))){
										od_dist[cn] += 0.4;
										if(od_dist[cn] > maxValue){od_dist[cn]=maxValue;}
									}else{
										o_dist[cn]+=0.3;
										//Give more points if the player is the one in the AI's mission.
										if(ownMission.getPlayer()!=null){
											if(continentOwner.getIsOwned(game.getPlayerIndex(ownMission.getPlayer()),cn)){
												o_dist[cn] += 0.2f;
												if(o_dist[cn] > maxValue){o_dist[cn]=maxValue;}
											}
										}
										if(o_dist[cn] > maxValue){o_dist[cn]=maxValue;}
									}
								}
							}
							
						}
					}
				}
				
				
				
				
				
				//Attack Player that has 18 or 24 and are close to winning.
				
				for(int pl=0; pl < opp_continentestimate.getNumOfPlayers();pl++){
					float threshold = 0.6f;
					if(((opp_mission.getMostLikelyMission(pl, missions).getPlayer() == null && opp_mission.getMostLikelyMission(pl, missions).getNoofcountries()== 24) && opp_mission.getEstimate(pl, missions.indexOf(opp_mission.getMostLikelyMission(pl, missions))) > 0.75f
						&& currentPlayerIndex != pl && !game.isPlayerDead((Player)((Vector)game.getPlayers()).get(pl))) ||
						(opp_mission.getMostLikelyMission(pl, missions).getNoofcountries()== 18  && opp_mission.getEstimate(pl, missions.indexOf(opp_mission.getMostLikelyMission(pl, missions))) > 0.75f
						&& currentPlayerIndex != pl  && !game.isPlayerDead((Player)((Vector)game.getPlayers()).get(pl)))){
						for(int rn = 0; rn < 5; rn++){
							if(opp_winningestimate.getEstimate(pl, rn) >= threshold){
								a_dist[pl] += 0.3f;
								if(a_dist[pl] > maxValue){a_dist[pl]=maxValue;}
								break;
							} else{
								threshold += 0.1f;
							}
							
						}
					}
				}
				//Attack player if AI has him as a mission.
				for(int pl = 0; pl < opp_continentestimate.getNumOfPlayers(); pl++){
					float threshold = 0.8f;
					if((ownMission.getPlayer()== playersInGame.get(pl)) && !game.isPlayerDead((Player)((Vector)game.getPlayers()).get(pl)) && ownMission.getPlayer()!= playersInGame.get(currentPlayerIndex)){
						a_dist[pl] += 0.4f;
						if(opp_winningestimate.getEstimate(currentPlayerIndex, 0) > 0.9f){
							a_dist[pl] = 1.0f;
							if(a_dist[pl] > maxValue){a_dist[pl]=maxValue;}
						}
					}
					for(int rn = 0; rn <3; rn++){
						if(opp_winningestimate.getEstimate(pl, rn) >= threshold && pl != currentPlayerIndex && !game.isPlayerDead((Player)((Vector)game.getPlayers()).get(pl)) && ownMission.getPlayer()!= null){
							a_dist[pl] += 0.2f;
							if(a_dist[pl] > maxValue){a_dist[pl]=maxValue;}
							break;
						} else{
							threshold += 0.1f;
						}
					}
				}
				
				//18 with 2 armies on each.
				float threshold = 0.6f;
				if(ownMission.getNoofcountries()==18){
					eighteen_dist += 0.2f;
					for(int rn = 0; rn < 5; rn++){
						if(opp_winningestimate.getEstimate(currentPlayerIndex, rn) >= threshold){
							eighteen_dist += 0.3f;
							if(eighteen_dist > maxValue){eighteen_dist=maxValue;}
							break;
						} else{
							threshold += 0.1f;
						}
					}
				}
				
				//24 territories.
				threshold = 0.6f;
				if((ownMission.getNoofcountries()==24 && ownMission.getPlayer() == null) || ((ownMission.getPlayer() != null) && (game.isPlayerDead(ownMission.getPlayer()))) || (ownMission.getPlayer() == playersInGame.get(currentPlayerIndex))){
					twentyfour_dist += 0.2f;
					for(int rn = 0; rn < 5; rn++){
						if(opp_winningestimate.getEstimate(currentPlayerIndex, rn) >= threshold){
							twentyfour_dist += 0.2f;
							if(twentyfour_dist > maxValue){twentyfour_dist=maxValue;}
							break;
						} else{
							threshold += 0.1f;
						}
					}
				}
			}
		}else{
			//If close to winning and mission is 24.
			if((ownMission.getNoofcountries()==24 && ownMission.getPlayer() == null) || ((ownMission.getPlayer() != null) && (game.isPlayerDead(ownMission.getPlayer()))) || (ownMission.getPlayer() == playersInGame.get(currentPlayerIndex))){
				twentyfour_dist = 1.0f;
			}
			//Mission is 18.
			if(ownMission.getNoofcountries()==18){
				eighteen_dist += 1.0f;
			}
			//Kill player.
			for(int pl = 0; pl < opp_continentestimate.getNumOfPlayers(); pl++){
				if((ownMission.getPlayer()== playersInGame.get(pl)) && !game.isPlayerDead((Player)((Vector)game.getPlayers()).get(pl)) && ownMission.getPlayer()!= playersInGame.get(currentPlayerIndex)){
					a_dist[pl] += 1.0f;
				}
			}
			
			if(ownMission.getContinent1() != null){
				if(!continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent1()))){
					c_dist[continents.indexOf(ownMission.getContinent1())] += 1.0f;
					if(c_dist[continents.indexOf(ownMission.getContinent1())] > maxValue){c_dist[continents.indexOf(ownMission.getContinent1())]=maxValue;}
				}
			}
			if(ownMission.getContinent2() != null){
				if(!continentOwner.getIsOwned(currentPlayerIndex,continents.indexOf(ownMission.getContinent2()))){
					c_dist[continents.indexOf(ownMission.getContinent2())] += 1.0f;
					if(c_dist[continents.indexOf(ownMission.getContinent2())] > maxValue){c_dist[continents.indexOf(ownMission.getContinent2())]=maxValue;}
				}
			}
			if(ownMission.getContinent3()!=null){
				for(int cn=0; cn < opp_continentestimate.getNumOfContinents(); cn++){
					if(opp_continentestimate.getEstimate(currentPlayerIndex, cn,0) > 0.9 && !continentOwner.getIsOwned(currentPlayerIndex,cn)){
						c_dist[cn]=1.0f;
					}
				}
			}
		}
		output.setDistribution_CD(cd_dist);
		output.setDistribution_C(c_dist);
		output.setDistribution_OD(od_dist);
		output.setDistribution_D(d_dist);
		output.setDistribution_O(o_dist);
		output.setDistribution_A(a_dist);
		output.setDistribution_18(eighteen_dist);
		output.setDistribution_24(twentyfour_dist);
	}

	private void dt_run(O_IG_MissionEstimate missionEstimate, O_IG_NextMoveEstimate nextMoveEstimate, O_IG_ContinentEstimate continentEstimate, O_IG_WinningEstimate winningEstimate, O_IG_Ownership ownership) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void nn_run(O_IG_MissionEstimate missionEstimate, O_IG_NextMoveEstimate nextMoveEstimate, O_IG_ContinentEstimate continentEstimate, O_IG_WinningEstimate winningEstimate, O_IG_Ownership ownership) {
		GameData data = new GameData(game.getPlayers(), this.currentPlayerIndex);
		data.setMissionEstimates(missionEstimate);
		data.setNextMove(nextMoveEstimate);
		data.setContinentEstimates(continentEstimate);
		data.setWinningEstimates(winningEstimate);
		data.setOwnership(ownership);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_MP_GoalDistribution();
		int i = 0;
		// Build the input
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getNextMoveAttribute()[territoryIndex].getNNValue());
			i++;
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				nnInput[i] = Float.parseFloat(data.getWinningEstimate(roundIndex).getNNValue());
				i++;
			}
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int missionIndex = 0; missionIndex < T_Game.NUMBER_OF_MISSIONS; missionIndex++) {
				nnInput[i] = Float.parseFloat(data.getMissionEstimate(missionIndex).getNNValue());
				i++;
			}		
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				for (int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
					nnInput[i] = Float.parseFloat(data.getContinentEstimate(continentIndex,roundIndex).getNNValue());
					i++;
				}
			}
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
				nnInput[i] = Float.parseFloat(data.getOwnership(continentIndex).getNNValue());
				i++;
			}
		}
		// Validate number of inputs
		if (i != (nnInput.length)) {
			throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
		}
		// Query network
		float[] out = this.neuralNetwork.calcOutput(nnInput);
		// Build the output
		output.setFullDistribution(out);
	}

	public void setNewCurrentPlayer(int currentPlayerIndex, Mission mission) {
		this.currentPlayerIndex = currentPlayerIndex;
		this.ownMission = mission;
	}
	
}

