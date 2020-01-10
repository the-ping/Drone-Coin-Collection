package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class Stateful extends Drone {

	public Stateful(Position position) {
		super(position);
	} 
	
//find the closest station to the drone's current position
	public Station closest_station(List<Station> stations) {
		
		Station goal_station = null;
		double shortest_dist = 0.009; //an arbitrary big distance
		//retrieve closest station
		for (int i=0; i<stations.size(); i++) {
			//distance between drone and said station
			double distance = this.position.calculate_distance(stations.get(i).position);
			if(distance<shortest_dist) {
				goal_station = stations.get(i);
				shortest_dist = distance;
			}
		}
		return goal_station;
	}

	
//choosing the next best direction, heading towards goal
	public Direction choose_direction(Station goal_station, List<Direction> directions) {
		//initialize chosen direction
		Direction chosen_dir = null;
		//distance of drone's next position to goal_station (initially drone's current position)
		double shortest_dist = 1;
		//find drone's next position that is closest to the goal_station
			for (int i=0; i<directions.size(); i++) {
				//if next position is closer to goal than shortest known distance
				if (this.position.nextPosition(directions.get(i)).calculate_distance(goal_station.position) < shortest_dist) {
					//if direction doesn't lead to out of play area
					if (this.position.nextPosition(directions.get(i)).inPlayArea()) {
						//update shortest distance
						shortest_dist = this.position.nextPosition(directions.get(i)).calculate_distance(goal_station.position);
						//head to this direction
						chosen_dir = directions.get(i);
					}
					
				}
			}
		return chosen_dir;
	}


//stepped into any station's vicinity
	public boolean met_station(Station station, Position pos) {
		boolean met = false;
		//is drone's position within a station's range
		if (station.position.calculate_distance(pos) < 0.00025) {
			met = true;
		}
		return met;
	}
	
//stepped into bad station vicinity
	public boolean met_bad_station(List<Station>neg_stations, Position pos) {
		boolean met = false;
		for (int i=0; i<neg_stations.size(); i++) {
			if (this.met_station(neg_stations.get(i), pos)) {
				met = true;
			}
		}
		return met;
	}

//heading to the goal (closest) station
	public Direction to_goal_station (List<Station> pos_stations, List<Station> neg_stations) {
		List<Direction> directions = Direction.directions();
		//haven't met goal station
		boolean met_goalstation = false;
		Station goal_station = this.closest_station(pos_stations);
		
		//pick the direction, that results next position to be closest to goal station
		Direction dir = this.choose_direction(goal_station, directions);
		//future position
		Position future_pos = this.position.nextPosition(dir);

		//if this move has reached goal station
		if(this.met_station(goal_station, future_pos)) {
			//if also steps into bad_station vicinity
			if(this.met_bad_station(neg_stations, future_pos)) {
				//iterate until find a spot that steps into goal but not bad station vicinity
					while(this.met_bad_station(neg_stations, future_pos)) {
						//have 1 less direction to choose from now
						directions = dir.remove_from_directions(directions);
						//pick another direction 
						dir = this.choose_direction(goal_station, directions);
						//new future position
						future_pos = this.position.nextPosition(dir);
					}
			}//if doesn't step into bad_station vicinity, proceed with initial direction planned
			//or head to chosen best next direction
			met_goalstation = true;
		}
		
		//if this move passes a bad station
		if(this.met_bad_station(neg_stations, future_pos)) {
			//if YES, 
			//iterate until find a spot not bad station vicinity
			while(this.met_bad_station(neg_stations, future_pos)) {
				//have 1 less direction to choose from now
				directions = dir.remove_from_directions(directions);
				//pick another direction 
				dir = this.choose_direction(goal_station, directions);
				//new future position
				future_pos = this.position.nextPosition(dir);
			}		
		}//if NO, then proceed with initial direction planned
		//or head to chosen best next direction
		boolean moved = this.one_move(dir);
		
		//if drone can't move, means power ran out, 
		//return direction as null to indicate power ran out
		if (moved == false) {
			dir = null;
		}
		
		if (met_goalstation == true) {
			//update drone's status
			this.coin = this.coin + goal_station.coin;
			this.power = this.power + goal_station.power;
			//update station's status
			goal_station.drone_visited();
		}
		return dir;
	}		
	
//get all positive stations on map
	public List<Station> get_all_pos_stations(List<Station> stations) {
		//create list to store positive stations
		List<Station> pos_stations = new ArrayList<Station>();
		for (int i = 0; i<stations.size(); i++) {
			if (stations.get(i).coin > 0.0f) {
				pos_stations.add(stations.get(i));
			}
		}
		return pos_stations;
	}
	
//get all negative stations on map
	public List<Station> get_all_neg_stations(List<Station> stations) {
		//create list to store negative stations
				List<Station> neg_stations = new ArrayList<Station>();
				for (int i = 0; i<stations.size(); i++) {
					if (stations.get(i).coin < 0.0f) {
						neg_stations.add(stations.get(i));
					}
				}
				return neg_stations;
	}

//Gameplay for Stateful
	public LineString StartGameStateful(List<Station> station_list, Random rnd) {

		//store drone's status for every move
		double lat_txt = this.position.latitude;
		double lon_txt = this.position.longitude;
		Direction dir = null;
		
		//get list of positive stations on map
		List<Station> all_pos_stations = this.get_all_pos_stations(station_list);
		//get list of negative stations on map
		List<Station> all_neg_stations = this.get_all_neg_stations(station_list);
		
		//keep tracks how many times drone visited the same spot
		int same_pos = 0;
		
		//for every move
		for (int m = 0; m<250; m++) {
			//if no more positive stations to visit, terminate game by zoning about one spot
			if (all_pos_stations.size() == 0) {
				//for the remaining moves, move randomly
				for (int k = m; k<250; k++) {
					dir = this.position.head_random(rnd, all_neg_stations);
					this.one_move(dir);
				}
				break;
			}
		
			//drone heads to goal station direction, while avoiding negative stations
			dir = this.to_goal_station(all_pos_stations, all_neg_stations);

			Station goal_station = this.closest_station(all_pos_stations);
			
			//if drone stuck in a loop path, head to random direction
			if(same_pos == 25) {
				dir = this.choose_direction(goal_station, Direction.directions());
				//head to this random direction
				this.one_move(dir);
				all_pos_stations = goal_station.remove_from_stations(all_pos_stations);
				//reset count
				same_pos = 0;
			}
			
			//already updated drone's position
			//so, does drone end up in goal_station's vicinity
			if(this.met_station(goal_station, this.position)) {
				//remember goal_station visited,
				//update list of positive stations left drone can visit
				all_pos_stations = goal_station.remove_from_stations(all_pos_stations);
			}
			
			Point prev_point = this.drone_path.get(this.drone_path.size()-1);
			Point prev_point_2 = this.drone_path.get(this.drone_path.size()-2);
			double pp_lat = prev_point.coordinates().get(1);
			double pp_lon = prev_point.coordinates().get(0);
			double pp_lat_2 = prev_point_2.coordinates().get(1);
			double pp_lon_2 = prev_point_2.coordinates().get(0);
			//counts how many times drone visits the same position on the map
			//does current position match 2 positions ago? or 1 position ago?
			//if yes, most likely a looped path occurrence
			if (this.position.latitude == pp_lat && this.position.longitude == pp_lon || this.position.latitude == pp_lat_2 && this.position.longitude == pp_lon_2) {
				same_pos ++;
			} 
		
//			System.out.println(lat_txt + "," + lon_txt + "," + dir + "," + this.position.latitude + "," + this.position.longitude + "," + this.coin + "," + this.power);
			
			//state current latitudes and longitudes of drone, 
			//to refer to this as 1 previous position when drone makes next move (in the next iteration)
			lat_txt = this.position.latitude;
			lon_txt = this.position.longitude;
			
		}
		return LineString.fromLngLats(this.drone_path);
	}
	
}
