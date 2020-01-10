package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

//import com.mapbox.geojson.Point;

public class Station {
	
	//station properties
	protected float coin;
	protected float power;
	protected List<Double> p_coord;
//	protected Point p;
	protected Position position;
	
	public Station(float coin, float power, List<Double> p_coord) {
		this.coin = coin;
		this.power = power;
		this.p_coord = p_coord;
//		this.p = p;
		this.position = new Position(p_coord.get(1), p_coord.get(0)); //convert coordinate = (long, lat) into a position
	}
	
	//a drone has visited this station
	//update station's status
	public void drone_visited () {
		this.coin = 0.0f;
		this.power = 0.0f;
	}
	
	//Stateful drone use:
	//remove this station from a list of positive stations
	//return the remaining list
	public List<Station> remove_from_stations(List<Station> stations) {
		List<Station> new_stationlist = new ArrayList<Station>();
		for (int i = 0; i<stations.size(); i++) {
			if (stations.get(i) != this) {
				new_stationlist.add(stations.get(i));
			}
		}
		return new_stationlist;
	}
	
	
	
	 
	
}
