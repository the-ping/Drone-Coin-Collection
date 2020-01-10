package uk.ac.ed.inf.powergrab;

import java.util.List;
import java.util.Random;

public class Position {
	protected double latitude;
	protected double longitude;
	protected double lat;
	protected double lon;
	protected double r = 0.0003;
	//stored calculations
	private double w1 = r*Math.cos(Math.PI*0.375);
	private double h1 = r*Math.sin(Math.PI*0.375);
	private double w2 = r*Math.cos(Math.PI*0.25);
	private double h2 = r*Math.sin(Math.PI*0.25);
	private double w3 = r*Math.cos(Math.PI*0.125);
	private double h3 = r*Math.sin(Math.PI*0.125);
	
	public Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		
	}
	
	//drone's new position if taken an arb. direction
	public Position nextPosition(Direction direction) {
		
		switch(direction) {
			case N:
				lat = this.latitude + r;
				lon = this.longitude;
				break;
			case S:
				lat = this.latitude - r;
				lon = this.longitude;
				break;
			case E:
				lat = this.latitude;
				lon = this.longitude + r;
				break;
			case W:
				lat = this.latitude;
				lon = this.longitude - r;
				break;
			case NE:
				lat = this.latitude + h2;
				lon = this.longitude + w2;
				break;
			case NNE:
				lat = this.latitude + h1;
				lon = this.longitude + w1;
				break;
			case ENE:
				lat = this.latitude + h3;
				lon = this.longitude + w3;
				break;
			case NW:
				lat = this.latitude + h2;
				lon = this.longitude - w2;
				break;
			case NNW:
				lat = this.latitude + h1;
				lon = this.longitude - w1;
				break;
			case WNW:
				lat = this.latitude + h3;
				lon = this.longitude - w3;
				break;
			case SE:
				lat = this.latitude - h2;
				lon = this.longitude + w2;
				break;
			case SSE:
				lat = this.latitude - h1;
				lon = this.longitude + w1;
				break;
			case ESE:
				lat = this.latitude - h3;
				lon = this.longitude + w3;
				break;
			case SW:
				lat = this.latitude - h2;
				lon = this.longitude - w2;
				break;
			case SSW:
				lat = this.latitude - h1;
				lon = this.longitude - w1;
				break;
			case WSW:
				lat = this.latitude - h3;
				lon = this.longitude - w3;
				break;
		}
		
		Position newpos = new Position(lat, lon);
		return newpos;
	}

	
	  public boolean inPlayArea() { // is drone within the specified area
	  if ((55.942617 < this.latitude && this.latitude < 55.946233) && (-3.192473 < this.longitude && this.longitude < -3.184319)) { 
		  return true; 
		  } 
	  else { 
		  return false; 
		  }
	  
	  }
	  
	  //does this position have nearby station
	  public boolean position_near_station(Station station) {
		  double distance = Math.sqrt(Math.pow(this.latitude - station.position.latitude, 2) + Math.pow(this.longitude - station.position.longitude, 2));
		  if (distance < 0.00025) {
			  return true;
		  }
		  else {
			  return false;
		  }
		  
	  }
	  
	  //does this position have nearby BAD stations
	  public boolean bad_stations_near(List<Station> stations) {
		  boolean bad = false;
		  for (int i = 0; i<stations.size(); i++) {
			  //if there exists at least bad station nearby
			  if(this.position_near_station(stations.get(i)) && stations.get(i).coin < 0.0f) {
				  bad = true;
				  break;
			  }
		  }
		  return bad;
	  }
	  
	  //calculate distance between 2 points
	  public double calculate_distance(Position p2) {
		  return Math.sqrt(Math.pow(this.latitude - p2.latitude, 2) + Math.pow(this.longitude - p2.longitude, 2));
	  }
	  
	  //head to random position
	  public Direction head_random(Random rnd, List<Station> neg_stations) {
		  //randomly generate a direction
		  Direction dir = Direction.directions().get(rnd.nextInt(15));
		  //if this direction leads to out of play area or encounter bad stations
		  //get another random direction
		  while (this.nextPosition(dir).inPlayArea() == false || this.nextPosition(dir).bad_stations_near(neg_stations)) {
			  dir = Direction.directions().get(rnd.nextInt(15));
		  }
		  return dir;
	  }
	  
	 
	
}
