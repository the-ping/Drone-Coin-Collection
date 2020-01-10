package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.LineString;


public class Stateless extends Drone {
	
	public Stateless(Position position) {
		super(position);
	}

	// calculate list of 16 future positions
		public List<Position> calculate_future_pos(List<Direction> dir_list) {
			List<Position> list_positions = new ArrayList<Position>();
			for (int i=0; i<dir_list.size(); i++) {
				list_positions.add(this.position.nextPosition(dir_list.get(i)));
			}
			return list_positions;
		}
		
	//the best station near drone
		public int get_best_station(List<Station> nearby_stations) {
			//which station has most coins, store the index of that station
			int index = -1;
			float coin = 0.0f;
			for (int i=0; i<nearby_stations.size(); i++) {
				if (nearby_stations.get(i).coin > coin && nearby_stations.get(i).coin >= 0.0f) {
					coin = nearby_stations.get(i).coin;
					index = i;
				}
			}
			
			return(index);
		}
		
	//drone heads to random direction
		public Direction to_random_dir(Random rnd, List<Station> stations) {
			List<Direction> directions = Direction.directions();
			boolean moved_to_random = false;
			Direction rand_dir = directions.get(rnd.nextInt(15));
			
			//as long next position ends up our of play area or enters a bad station vicinity
			//generate another random direction
			if (this.position.nextPosition(rand_dir).inPlayArea() == false || this.position.nextPosition(rand_dir).bad_stations_near(stations) == true) {
				while (this.position.nextPosition(rand_dir).inPlayArea() == false || this.position.nextPosition(rand_dir).bad_stations_near(stations) == true) {
					rand_dir = this.to_random_dir(rnd, stations);
				}
			}
			moved_to_random = this.one_move(rand_dir);
			
			if(moved_to_random == false) {
				rand_dir = null;
			}
			return rand_dir;
		}
		
	//if encountered a station, update drone's power and coins
		public Direction to_station(Station station, Direction direction, Random rnd, List<Station> stations) {	
			List<Direction> directions = Direction.directions();
			boolean moved_to_station = false;
			//what if this charged station overlaps a bad station?
			if (this.position.nextPosition(direction).bad_stations_near(stations) == true) {
				//get a random direction
				while (this.position.nextPosition(direction).bad_stations_near(stations) == true) {
					direction = this.to_random_dir(rnd, stations);
				}
				moved_to_station = true;
			}
			else {
				moved_to_station = this.one_move(direction);
				if (moved_to_station == true) {
					//update drone's power and coins
					this.power = this.power + station.power;
					this.coin = this.coin + station.coin;
					//update station's power and coins
					station.drone_visited();
				}
			}
			
			if (moved_to_station == false) {
				direction = null;
			}
			
			return direction;
		}
		
	//GamePlay for Stateless
		public LineString StartGameStateless(List<Station> station_list, Random rnd) {
			//store drone's status for every move
			double lat_txt = this.position.latitude;
			double lon_txt = this.position.longitude;
			Direction dir_txt = null;
			List<Direction> possible_directions = Direction.directions();
			//for every move
				for (int m = 0; m<250; m++) {
					//drone hasn't moved
					boolean drone_moved = false;
					//drone has moved to this direction
					Direction dir = null;
					//list of drone's nearby stations
					List<Station> nearby_stations = new ArrayList<Station>();
					//list of selected directions that leads to the nearby stations
					List<Direction> selected_directions = new ArrayList<Direction>();
					//list of drone's 16 future positions
					List<Position> future_positions = this.calculate_future_pos(possible_directions);
					
					//for each future position, get stations nearby (if any)
					for (int f = 0; f<future_positions.size(); f++) {
						//for each of the 50 stations on the map, is station near the drone?
						for (int s = 0; s<station_list.size(); s++) {
							//if this future position and this station is nearby
							if (future_positions.get(f).position_near_station(station_list.get(s))) {
								//append list of nearby stations
								nearby_stations.add(station_list.get(s));
								//append list of directions that leads to a station
								selected_directions.add(possible_directions.get(f));
							}
						}
					}
					
					if (nearby_stations.size() != 0) {
						//get the index of best direction and station it's heading to
						int best_index = this.get_best_station(nearby_stations);
						
						//if best_index == -1, means nearby stations are all negative valued
						//hence opt for random direction
						if (best_index == -1)
						{
							dir = (this.to_random_dir(rnd, station_list));
						}
						else {
							//move drone to the best station's direction
							dir = this.to_station(nearby_stations.get(best_index), selected_directions.get(best_index), rnd, station_list);
						}
					}
					else {
						//randomly head to one direction 
						dir = (this.to_random_dir(rnd, station_list));
					}
					if (dir != null) {
						drone_moved = true;
					}
					dir_txt = dir;
					
					System.out.println(lat_txt + "," + lon_txt + "," + dir_txt + "," + this.position.latitude + "," + this.position.longitude + "," + this.coin + "," + this.power);
					
					//udpate latitudes and longitudes of drone
					lat_txt = this.position.latitude;
					lon_txt = this.position.longitude;
				}
				
				return LineString.fromLngLats(this.drone_path);
		}
		
		
		
}
