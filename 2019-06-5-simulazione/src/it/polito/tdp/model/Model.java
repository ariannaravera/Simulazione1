package it.polito.tdp.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.db.EventsDao;

public class Model {
	
	EventsDao dao;
	SimpleWeightedGraph<Integer, DefaultEdge> grafo;
	Map<Long, Event> eventi;
	Map<Double,String> archi;
	
	public Model(){
		dao=new EventsDao();
		eventi=new HashMap<>();
		archi=new TreeMap<>();
		for(Event e:dao.listAllEvents()) {
			eventi.put(e.getIncident_id(), e);
		}
	}

	public List<Integer> getAnni() {
		return dao.listAnni();
	}

	public void creaGrafo(Integer anno) {
		grafo=new SimpleWeightedGraph<>(DefaultEdge.class);
		Map<Long, Event> eventiPerAnno=new HashMap<>();
		Map<Integer, List<Double>> centriCrimini=new HashMap<>();
	 	
		Graphs.addAllVertices(grafo, dao.getDistretti());
		
		for(Event e:eventi.values()) {
			if(e.getReported_date().getYear()==anno) {
				eventiPerAnno.put(e.getIncident_id(), e);
			}
		}
		for(int i:dao.getDistretti()) {
			List<Double> medie=new LinkedList<>();
			double sommaLat=0.0;
			double sommaLon=0.0;
			int n=0;
			for(Event e:eventiPerAnno.values()) {
				if(e.getDistrict_id()==i) {
					sommaLat+=e.getGeo_lat();
					sommaLon+=e.getGeo_lon();
					n++;
				}
			}
			medie.add((sommaLat/n));
			medie.add((sommaLon/n));
			centriCrimini.put(i, medie);
		}
		
		for(int i:dao.getDistretti()) {
			for(int j:dao.getDistretti()) {
				if(i!=j) {
					LatLng pt1=new LatLng(centriCrimini.get(i).get(0), centriCrimini.get(i).get(1));
					LatLng pt2=new LatLng(centriCrimini.get(j).get(0), centriCrimini.get(j).get(1));
					double peso=LatLngTool.distance(pt1,pt2,LengthUnit.KILOMETER);
					Graphs.addEdge(grafo, i, j, peso);
					archi.put(peso, i+","+j);
				}
			}
		}
		
		System.out.println("Creato il grafo!");
		System.out.println("Vertici: "+grafo.vertexSet().size()+" e archi: "+grafo.edgeSet().size());
	}

	public List<Integer> getDistretti() {
		return dao.getDistretti();
	}

	public String getVicini(int i) {
		String vicini="";
		List<Integer> pesi= new LinkedList<Integer>();
		for(Double d:archi.keySet()) {
			String[] vettore=archi.get(d).split(",");
			if(i==Integer.parseInt(vettore[0])) {
				vicini+=vettore[1]+" "+d+"\n";
			}
			if(i==Integer.parseInt(vettore[1])) {
				vicini+=vettore[0]+"\n";
			}
		}
		return vicini;
	}
	
}
