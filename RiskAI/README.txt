
term 5 - dat5.pdf
 - theory
 - Design and planning of the modules and classes

term 6 - dat6.pdf/report.pdf (same files)
 - practice
 - implementation, testing and review of results


testing out the lib

build and place RiskAI.jar into the Domination install dir
copy these files into the Domination install folder:
 - source/JRiskAI/closeToContinent_one.oobn
 - source/JRiskAI/closeToWinBN.oobn
 - source/JRiskAI/custom_framework.txt
 - source/RiskBattleProbTable/BattleOutcomeTable.txt
 - source/TrainingDataConverter/AIPlayerFrameworkSettings.txt
copy "ai-data" folder into the Domination install dir

there seem to be 3 new AIs, from docs:
 - Scripted Framework: Creates a purely scripted implementation of the framework.
 - Best framework: Creates the best implementation of the framework, which were found through the research documented in this report.
 - Custom framework: Creates an implementation of the framework, which is leaded from the file “custom framework.txt”. This makes it possible to create your own custom framework. Please be aware that not all tech- niques can be used in all modules. Please consult Table 5.2 on page 69. Please also notice that Bayesian networks (BN) only works if Hugin Re- searcher [AOJJ89] is installed on the computer. Therefore, the best AI does NOT use BN in the module “IG Continent”, but instead a neural network trained with winners only, which is the second best technique for that module.


scripting seems to mean logic simply written in code
e.g. risk.AI.Modules.RoundPlanner.C_RP_InitPlacement#script_run returns a Country for placement

"human" - 0
"ai easy" - 1
"ai hard" - 2
"ai extrahard" - 6 - Extra hard: this is the standard mission AI, ONLY works in mission mode
"ai random" - 4
"AI (Scripted framework)" - "ai framework" - 5 - "script" for each of 17 components
"AI (Custom framework)" - "ai framework_custom" - 7 - loaded from "custom_framework.txt" file
"AI (Best framework)" - "ai framework_best" - 8 - hard coded in game RiskGame.java line 165

there are 17 components to each framework AI, and each can be set to any of "random", "script", "nn", "nn_wo", ...

bn = Bayesian Net.
dt = Decision Tree
nb = naive Bayes classifiers
nn = Neural Net.
nn_wo = neural networks trained with winners only


limitations

some classes seem to only exist in compiled form
such as all the classes of the trainingdataconverter
e.g. trainingdataconverter.trainingdata.GameData


seems AI is ONLY designed to work mission mode.
from docs: "startgame mission: Begins the game. The AI only works with mission types of Risk games."


continents 6 and countries 42 seems to be hard coded in a lot of places
this means that errors are thrown when other maps are used.

trainingdataconverter.trainingdata.TrainingData seems to have map hard coded
risk.AI.Data_Structures.T_Game seems to have NUMBER_OF_TERRITORIES and CONTINENT_NAMES hard coded
risk.AI.Modules.InformationGivers.C_IG_Winning seems to have map specific logic

why is all this stuff hard coded? it could have just been loaded from the map
