package uk.ac.ed.inf.powergrab;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;


public class App 
{

    public static void main( String[] args ) throws Exception
    {
    		
    	//getting user input
    		//15 09 2019 55.944425 -3.188396 5678 stateless
    		String day = args[0];
    		String month = args[1];
    		String year = args[2];
    		Double lat_in = Double.parseDouble(args[3]);
    		Double lon_in = Double.parseDouble(args[4]);
    		Integer seed = Integer.parseInt(args[5]);
    		String drone_type = args[6];
    		
    		
    	
    	//getting the map
    		String mapString = "http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day + "/" + "powergrabmap.geojson";
    		URL mapURL = new URL(mapString);
    		
    		HttpURLConnection conn = (HttpURLConnection) mapURL.openConnection();
    		conn.setReadTimeout(10000);
    		conn.setConnectTimeout(15000);
    		conn.setRequestMethod("GET");
    		conn.setDoInput(true);
    		conn.connect();
    		InputStream inputStream = conn.getInputStream();
    		
    		InputStreamReader isReader = new InputStreamReader(inputStream);
    		BufferedReader reader = new BufferedReader(isReader);
    		StringBuffer sourceBuffer = new StringBuffer();
    		String str;
    		while((str = reader.readLine()) != null) {
    			sourceBuffer.append(str);
    		}
    		
    		String mapSource = sourceBuffer.toString();
    		
			//declare a list of stations to store from the map
			List<Station> station_list = new ArrayList<Station>();	
			//returns a FeatureCollection 
			FeatureCollection fc =  FeatureCollection.fromJson(mapSource);
			List<Feature> fc_list = fc.features();
			
				//for every feature, retrieve the following properties
				for (int i=0; i<= fc_list.size()-1; i++) {
					//every Feature as a Point. converted from Geometry type
					Point p = (Point) fc_list.get(i).geometry();
					//get list of double numbers, coords of one Point
					List<Double> p_coord = p.coordinates();
					//get coin_value
					Float coin = fc_list.get(i).getProperty("coins").getAsFloat();
					//get power_value
					Float power = fc_list.get(i).getProperty("power").getAsFloat();
					
					//create new Station object
					Station station = new Station(coin, power, p_coord);
					//append Station object to list of stations
					station_list.add(station);
				}
			
		/////-----STATELESS-----/////
			
			if (drone_type.equals("stateless")) {
				//initialize a stateless drone
				double latitude = lat_in; 
				double longitude = lon_in;
				Position init_pos = new Position(latitude, longitude);
				Stateless drone1  = new Stateless(init_pos);
				drone1.add_to_path();
				
				//initialize seed
				Random rnd = new Random(seed);
				
				//retrieve drone's path as LineString object
				LineString path = drone1.StartGameStateless(station_list, rnd);
				
				//add drone's path to map's features
				fc.features().add(Feature.fromGeometry(path));
				System.out.println(fc.toJson().toString());
				
			}
		/////-----STATEFUL-----/////
			else if (drone_type.equals("stateful")) {
				//initialize a stateful drone (w/ same parameters)
				double latitude = lat_in; 
				double longitude = lon_in;
				Position init_pos = new Position(latitude, longitude);
				Stateful drone1  = new Stateful(init_pos);
				//initial position is added to path
				drone1.add_to_path();

				//initialize seed
				Random rnd = new Random(seed);
				
				//retrieve drone's path as LineString object
				LineString path = drone1.StartGameStateful(station_list, rnd);
				//add drone's path to map's features
				fc.features().add(Feature.fromGeometry(path));
				System.out.println(fc.toJson().toString());
			}
			
			else {
				System.out.println("Error parsing input arguments. Please review your input");
			}
			
    }
  
			
    	
    }

