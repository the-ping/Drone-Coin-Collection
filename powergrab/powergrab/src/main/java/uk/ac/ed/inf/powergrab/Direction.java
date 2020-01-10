package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

public enum Direction {
	N, S, E, W, NNE, ENE, NNW, WNW, SSE, ESE, SSW, WSW, NE, NW, SE, SW	;
	
	//remove a direction from a list of directions
		//return the remaining list
		public List<Direction> remove_from_directions(List<Direction> directions) {
			List<Direction> new_directionlist = new ArrayList<Direction>();
			for (int i = 0; i<directions.size(); i++) {
				if (directions.get(i) != this) {
					new_directionlist.add(directions.get(i));
				}
			}
			return new_directionlist;
		}
		
	//retrieve a list of 16 directions
		public static List<Direction> directions() {
		List<Direction> possible_directions = new ArrayList<Direction>();
		for (Direction dir : Direction.values()) {
			possible_directions.add(dir);
		} 
		return possible_directions;
		}
		
}



	
