package minimax;



import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import javafx.util.Pair;
//import minimax.MapLocation;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import com.sun.javafx.scene.traversal.Direction;

public class MinimaxAlphaBeta extends Agent {
	    private final int numPlys;
	    private ArrayList<Stack<MapLocation>> optimalPaths = new ArrayList<Stack<MapLocation>>();
	    
	    public MinimaxAlphaBeta(int playernum, String[] args) {
	        super(playernum);
	        if(args.length < 1)
	        {
	            System.err.println("You must specify the number of plys");
	            System.exit(1);
	        }	  
	        numPlys = Integer.parseInt(args[0]);	
	    }

	    @Override
	    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
	    	
	        return middleStep(newstate, statehistory);
	    }

	    @Override
	    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
	        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
	                numPlys,
	                Double.NEGATIVE_INFINITY,
	                Double.POSITIVE_INFINITY); //returns the best child found from alphaBetaSearch
	        System.out.println("-------------------middle step executed--------------------------");
	        List<Integer> players = newstate.getUnitIds(playernum);
	        for(Integer id: players)
	        	System.out.println("UNIT " + id + "moves from " + newstate.getUnit(id).getXPosition() + "," + newstate.getUnit(id).getYPosition() + ((DirectedAction)bestChild.action.get(id)).getDirection());
	        System.out.println("Utility of move is " + bestChild.state.getUtility());
	        return bestChild.action;
	    }

	    @Override
	    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

	    }

	    @Override
	    public void savePlayerData(OutputStream os) {

	    }

	    @Override
	    public void loadPlayerData(InputStream is) {

	    }

	    /**
	     * You will implement this.
	     *
	     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
	     * and book for more information.
	     *
	     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
	     * code into other functions and methods)
	     *
	     * @param node The action and state to search from
	     * @param depth The remaining number of plys under this node
	     * @param alpha The current best value for the maximizing node from this node to the root
	     * @param beta The current best value for the minimizing node from this node to the root
	     * @return The best child of this node with updated values
	     */
	    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
	    {
	    	//calls all the children of the node and returns back node with max value
		    	 return maximumValue(node,depth,alpha,beta).getKey();
	    }
	    
	    public Pair<GameStateChild, Double> maximumValue(GameStateChild node, int depth, double alpha, double beta) {
	    	if (depth == 0)
	    		return new Pair<GameStateChild, Double>(node, node.state.getUtility());
	    	else {
	    		Pair<GameStateChild, Double> max = new Pair<GameStateChild, Double>(null, Double.NEGATIVE_INFINITY); //tracks the max child node and its utility
	    		Pair<GameStateChild, Double> temp = null;
	    		//double maxEval = Double.NEGATIVE_INFINITY;   		
	    		//determines node with max value by recursively calling all its children
	    		for (GameStateChild kid:orderChildrenWithHeuristics(node.state.getChildren())) { 
	    		//for (GameStateChild kid: node.state.getChildren()) {		
	        		//returns the utility of one leaf and the node that led to it
	        		temp = minimumValue(kid, depth-1, alpha, beta);
	        		if (temp.getValue() > max.getValue()) 
	        			max = new Pair<GameStateChild, Double>(kid, temp.getValue());
	        		
	        		//updates max eval to the highest encountered leaf so far
	        		
	        		//prunes if it's less than the best we've seen
	        		if (max.getValue() >= beta)	
	        			return max;
	        		alpha = Math.max(alpha, max.getValue()); //update if the highest utility we found > anything previously found
	      
	    		}
	        	return max;
	    	}
	    }
	    
	    public Pair<GameStateChild, Double> minimumValue(GameStateChild node, int depth, double alpha, double beta)
	    {
	    	if (depth == 0) {
	    		return new Pair<GameStateChild, Double>(node, node.state.getUtility());   	
	    	}
	    	else
	    	{
	    		Pair<GameStateChild, Double> min = new Pair<GameStateChild, Double>(null, Double.POSITIVE_INFINITY);
	    		Pair<GameStateChild, Double> temp;
	    		//determines node with min value by recursively calling all its children
	    		for (GameStateChild kid : orderChildrenWithHeuristics(node.state.getChildren())) { 
	        	//for (GameStateChild kid: node.state.getChildren()) {	
	    		//stores node that team awesome is most likely to choose 
	        		temp = maximumValue(kid,depth-1,alpha,beta);       		
	        		//decides which node has lower utility i.e. which node opponent will choose
	    			if (temp.getValue() < min.getValue())
	    				min = new Pair<GameStateChild, Double>(kid, temp.getValue());
	    			
	    			//prunes
	    			if (min.getValue() <= alpha) 
	    				return min;    			
	    			beta = Math.min(min.getValue(), beta);
	    		}
	        	return min; //returns the kid that leads to the best value
	    	}
	    }


	    /**
	     * You will implement this.
	     *
	     * Given a list of children you will order them according to heuristics you make up.
	     * See the assignment description for suggestions on heuristics to use when sorting.
	     *
	     * Use this function inside of your alphaBetaSearch method.
	     *
	     * Include a good comment about what your heuristics are and why you chose them.
	     *
	     * @param children
	     * @return The list of children sorted by your heuristic.
	     */ //if action moves me closer, makes an attack, distance comparison.
	    //note that we use higher heuristic values to represent better outcomes rather than to estimate utility directly
	    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
	    { //the point is to order by highest utility without examining the utilities (since that takes further plies)
	        //assign each action+location a heuristic, then quicksort based on the values
	    	ArrayList<HeuristicChildPair> heuristic = new ArrayList<HeuristicChildPair>(); 	    	
	    	int i = -1;
	    	int multiplier = 1; //multiply all heuristics w/ this value
	    	for (GameStateChild child: children) { //for each child gamestate
	    		heuristic.add(++i, new HeuristicChildPair(0, child));
	    		List<Integer> units;
	    		if (child.state.getPlayerNum() == 0) // If friendly unit
	    			units = child.state.getUnitIDs();
	    		else {
	    			units = child.state.getEnemyUnitIDs();
	    			multiplier = -1;
	    		} 		
	    		for (Integer uID: units) { //check info about allies
	    			UnitView unit = child.state.getState().getUnit(uID);
	    			Action action = child.action.get(uID);
	    			if (action != null && ActionType.PRIMITIVEATTACK.equals(action.getType())) //beneficial if we attack
	    				heuristic.get(i).incrVal(10 * multiplier);  		
	    			int[] enemyDistance = child.state.findEnemyDistances(unit);
	    			heuristic.get(i).incrVal(-enemyDistance[0] * multiplier);
	    			/*if (ActionType.PRIMITIVEMOVE.equals(action.getType())) { //not sure how to check direction
	    				UnitView enemy = child.state.getState().getUnit(child.state.getEnemyUnitIDs().get(i));
	    				int xDistToEnemy =  footman.getXPosition() - enemy.getXPosition();
	    				int yDistToEnemy = footman.getYPosition() - enemy.getYPosition(); 
	    				//Direction a = ((DirectedAction)action).direction
	    				//not sure how to find the direction of a move. but if the move direction is south 
	    				//and yDist is negative, add 3 to heuristic. similar logic for xDist
	    			}
	    		} */
	    	} 
	    	}
	    	//insertion sort that uses the heuristic values to sort the list
	    	for (int j = 1; j < children.size(); j++) {
	    		Integer val = heuristic.get(j).getVal();
	    		int k = j;
	    		while (k > 1 && val < heuristic.get(k-1).getVal()) {
	    			children.set(k, children.get(k - 1));
	    			heuristic.get(k).setVal(heuristic.get(--k).getVal());
	    		}
	    		children.set(k, children.get(j));
	    	}
	    	return children;
	    }
	    //added to pair heuristic to child, which will enable sorting 
	    public class HeuristicChildPair{
	    	int heuristic;
	    	GameStateChild child;
	    	public HeuristicChildPair(int heuristic, GameStateChild child) {
	    		
	    		this.heuristic = heuristic;
	    		this.child = child;
	    	}
	    	
	    	public int getVal() {
	    		return heuristic;
	    	}
	    	
	    	public void incrVal(int val) {
	    		heuristic += val;
	    	}
	    	public void setVal(int val) {
	    		heuristic = val;
	    	}
	    	public int compareTo(HeuristicChildPair h) {
	    		if (h.heuristic < this.heuristic)
	    			return -1;
	    		if (h.heuristic > this.heuristic)
	    			return 1;
	    		return 0;
	    	}
	    }
	    
	       
	  
	}
 /*   private final int numPlys;
    private Trees trees;
    
    public MinimaxAlphaBeta(int playernum, String[] args) {
        super(playernum);
        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }	  
        numPlys = Integer.parseInt(args[0]);	
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
    	trees = new Trees(newstate);
    	trees.createTreeTable();
    	return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate, trees),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY); //returns the best child found from alphaBetaSearch
        System.out.println("middle step executed");
        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
   /* public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
    	//calls all the children of the node and returns back node with max value
	    	 return maximumValue(node,depth,alpha,beta).getKey();
    }
    
    public Pair<GameStateChild, Double> maximumValue(GameStateChild node, int depth, double alpha, double beta) {
    	if (depth == 0)
    		return new Pair<GameStateChild, Double>(node, node.state.getUtility());
    	else {
    		Pair<GameStateChild, Double> max = new Pair<GameStateChild, Double>(null, Double.NEGATIVE_INFINITY); //tracks the max child node and its utility
    		Pair<GameStateChild, Double> temp = null;
    		//double maxEval = Double.NEGATIVE_INFINITY;   		
    		//determines node with max value by recursively calling all its children
    		for (GameStateChild kid:orderChildrenWithHeuristics(node.state.getChildren())) { 
    		//for (GameStateChild kid: node.state.getChildren()) {		
        		//returns the utility of one leaf and the node that led to it
        		temp = minimumValue(kid, depth-1, alpha, beta);
        		if (temp.getValue() > max.getValue()) 
        			max = new Pair<GameStateChild, Double>(kid, temp.getValue());
        		
        		//updates max eval to the highest encountered leaf so far
        		
        		//prunes if it's less than the best we've seen
        		if (max.getValue() >= beta)	
        			return max;
        		alpha = Math.max(alpha, max.getValue()); //update if the highest utility we found > anything previously found
      
    		}
        	return max;
    	}
    }
    
    public Pair<GameStateChild, Double> minimumValue(GameStateChild node, int depth, double alpha, double beta)
    {
    	if (depth == 0) {
    		System.out.println(node.state.getUtility() + "Utility for this move" + node.action);
    		return new Pair<GameStateChild, Double>(node, node.state.getUtility());   	
    	}
    	else
    	{
    		Pair<GameStateChild, Double> min = new Pair<GameStateChild, Double>(null, Double.POSITIVE_INFINITY);
    		Pair<GameStateChild, Double> temp;
    		//determines node with min value by recursively calling all its children
    		for (GameStateChild kid : orderChildrenWithHeuristics(node.state.getChildren())) { 
        	//for (GameStateChild kid: node.state.getChildren()) {	
    		//stores node that team awesome is most likely to choose 
        		temp = maximumValue(kid,depth-1,alpha,beta);       		
        		//decides which node has lower utility i.e. which node opponent will choose
    			if (temp.getValue() < min.getValue())
    				min = new Pair<GameStateChild, Double>(kid, temp.getValue());
    			
    			//prunes
    			if (min.getValue() <= alpha) 
    				return min;    			
    			beta = Math.min(min.getValue(), beta);
    		}
        	return min; //returns the kid that leads to the best value
    	}
    } */


    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */ //if action moves me closer, makes an attack, distance comparison.
    //note that we use higher heuristic values to represent better outcomes rather than to estimate utility directly
 /*   public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    { //the point is to order by highest utility without examining the utilities (since that takes further plies)
        //assign each action+location a heuristic, then quicksort based on the values
    	ArrayList<HeuristicChildPair> heuristic = new ArrayList<HeuristicChildPair>(); 	    	
    	int i = -1;
    	int multiplier = 1; //multiply all heuristics w/ this value
    	for (GameStateChild child: children) { //for each child gamestate
    		heuristic.add(++i, new HeuristicChildPair(0, child));
    		List<Integer> units;
    		HashMap<Integer, FutureUnit> futureUnits = child.state.getFutureUnits();
    		if (futureUnits.get(child.state.getUnitIDs().get(0)).getTurn() % 2 == 0) // If it's our turn
    			units = child.state.getUnitIDs();
    		else {
    			units = child.state.getEnemyUnitIDs();
    			multiplier = -1;
    		} 		
    		for (Integer uID: units) { //check info about movers
    			FutureUnit fu = futureUnits.get(uID);
    			Action action = child.action.get(uID);
    			if (action != null && ActionType.PRIMITIVEATTACK.equals(action.getType())) //beneficial if we attack
    				heuristic.get(i).incrVal(10 * multiplier);  		
    			int[] enemyDistance = child.state.findEnemyDistances(fu);
    			heuristic.get(i).incrVal(-enemyDistance[0] * multiplier);
    			/*if (ActionType.PRIMITIVEMOVE.equals(action.getType())) { //not sure how to check direction
    				UnitView enemy = child.state.getState().getUnit(child.state.getEnemyUnitIDs().get(i));
    				int xDistToEnemy =  footman.getXPosition() - enemy.getXPosition();
    				int yDistToEnemy = footman.getYPosition() - enemy.getYPosition(); 
    				//Direction a = ((DirectedAction)action).direction
    				//not sure how to find the direction of a move. but if the move direction is south 
    				//and yDist is negative, add 3 to heuristic. similar logic for xDist
    			}
    		} */
    	/*} 
    	} 
    	//insertion sort that uses the heuristic values to sort the list
    	for (int j = 1; j < children.size(); j++) {
    		Integer val = heuristic.get(j).getVal();
    		int k = j;
    		while (k > 1 && val < heuristic.get(k-1).getVal()) {
    			children.set(k, children.get(k - 1));
    			heuristic.get(k).setVal(heuristic.get(--k).getVal());
    		}
    		children.set(k, children.get(j));
    	}
    	return children;
    }
    //added to pair heuristic to child, which will enable sorting 
    public class HeuristicChildPair{
    	int heuristic;
    	GameStateChild child;
    	public HeuristicChildPair(int heuristic, GameStateChild child) {
    		
    		this.heuristic = heuristic;
    		this.child = child;
    	}
    	
    	public int getVal() {
    		return heuristic;
    	}
    	
    	public void incrVal(int val) {
    		heuristic += val;
    	}
    	public void setVal(int val) {
    		heuristic = val;
    	}
    	public int compareTo(HeuristicChildPair h) {
    		if (h.heuristic < this.heuristic)
    			return -1;
    		if (h.heuristic > this.heuristic)
    			return 1;
    		return 0;
    	}
    }
} */

