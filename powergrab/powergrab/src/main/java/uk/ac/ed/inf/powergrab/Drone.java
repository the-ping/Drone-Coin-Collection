package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Point;

public abstract class Drone {
	
	//drone properties
	protected Position position;
	protected int moves;
	protected float power;
	protected float coin;
	protected Point point;
	protected List<Point> drone_path = new ArrayList<Point>();
	
	
	//constructor-san
	public Drone(Position position) {
		this.position = position;
		this.moves = 250;
		this.power = 250.0f;
		this.coin = 0.0f;
		this.point = Point.fromLngLat(this.position.longitude, this.position.latitude);
	}
	
	//add to path
		public void add_to_path() {
			drone_path.add(this.point);
		}
	
	//every move updates drone basic stats
	public boolean one_move(Direction direction) {
		boolean moved = false;
		//if power hasn't run out
		if((this.power - 1.25) > 0.0 ) {
			//used up 1 move
			this.moves -= 1;
			//used up 1.25 units of power
			this.power -= 1.25;
			//update position
			this.position = this.position.nextPosition(direction);
			//get corresponding point
			this.point = Point.fromLngLat(this.position.longitude, this.position.latitude);
			//add point to drone's path
			this.add_to_path();
			//drone successfully moved one step
			moved = true;
		}
		
		return moved;
	}

	

}
