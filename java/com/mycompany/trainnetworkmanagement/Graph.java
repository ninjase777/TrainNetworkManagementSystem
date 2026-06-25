package com.mycompany.trainnetworkmanagement;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Format;
import java.util.PriorityQueue;


public class Graph {
    HashMap<Station, List<Edge>> graph;
    public Graph() {
    graph = new HashMap<>();
    }
    public boolean addStation(String code, String name)
    {
        Station newStation = new Station(code , name);
        if(graph.containsKey(newStation)){
        return false;
        }
        graph.put(newStation, new ArrayList<>());
        
        return true;
    }
    public boolean addEdge(Station source, Station destination, int distance){
        
        if (!graph.containsKey(source) || !graph.containsKey(destination)){
            return false;
        }

        for (Edge edge : graph.get(source)){
            
            if (edge.getDestination().equals(destination)){
                return false;
            }
        }
        Edge newEdge = new Edge(destination, distance);
        graph.get(source).add(newEdge);
      
        return true;
}
    
    public boolean removeStation(Station station){
        
        if(!graph.containsKey(station)){
            return false;
        }
        graph.remove(station);
        for (List<Edge> edge : graph.values()) {
            Iterator<Edge> iterator = edge.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getDestination().equals(station)) {
                    iterator.remove();
                }
            }
        }
        return true;
    }
    public boolean removeEdge(Station source , Station destination)
    {
        if(!graph.containsKey(source)|| !graph.containsKey(destination))
        {
            return false;
        }
        Iterator<Edge> iterator = graph.get(source).iterator();
        while(iterator.hasNext())
        {
            if(iterator.next().getDestination().equals(destination))
            {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
    public void Export(String fileName) 
    {
        try(PrintWriter writer = new PrintWriter(fileName)){
            
            for(Station station : graph.keySet()){
                
                List<Edge> edges = graph.get(station);
               
                writer.print(station.getName() + " -> ");
                for(int i = 0 ; i< edges.size();i++)
                {
                    
                    writer.print(edges.get(i).getDestination().getName() +"(" + edges.get(i).getDistance()+")");
                    if(i < edges.size()-1)
                        writer.print(", ");
                }
                writer.println();
            }
        }catch(FileNotFoundException e){
            System.out.println("file not found ");
        }
    }
    public Station getStationByName(String name){
        for (Station station : graph.keySet())
        {
            if (station.getName().equals(name))
                return station;
        }
        return null;
    }

    public void Import(String fileName){
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String line;
            while ((line = reader.readLine()) != null){
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("->");
                if (parts.length != 2)
                    continue;
                String sourceName = parts[0].trim();
                Station source = getStationByName(sourceName);
                if (source == null){
                    addStation(sourceName, sourceName);
                    source = getStationByName(sourceName);
                }
                String[] destinations = parts[1].split(",");
                for (String dest : destinations){
                    dest = dest.trim();
                    int open = dest.indexOf('(');
                    int close = dest.indexOf(')');
                    if (open == -1 || close == -1)
                        continue;
                    String destinationName = dest.substring(0, open).trim();
                    int distance =Integer.parseInt(dest.substring(open + 1, close).trim());
                    Station destination = getStationByName(destinationName);

                    if (destination == null){
                        addStation(destinationName, destinationName);
                        destination = getStationByName(destinationName);
                    }
                    addEdge(source, destination, distance);
                }
            }

            System.out.println("test success");

        }
        catch (IOException e)
        {
            System.out.println("Error : " + e.getMessage());
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid format in file.");
        }
    }
    
    public static void drawFromFile(String filePath, String outputFile) throws IOException {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        for (String line : Files.readAllLines(Paths.get(filePath))) {
            line = line.trim();
            if (line.isEmpty())
                continue;
            String[] parts = line.split("->");
            if (parts.length != 2)
                continue;
            String source = parts[0].trim();
            dot.append("\"").append(source).append("\";\n");
            String[] targets = parts[1].split(",");
            for (String t : targets) {
                t = t.trim();
                if (!t.contains("(") || !t.contains(")"))
                    continue;
                String target = t.substring(0, t.indexOf("(")).trim();
                String weight =t.substring( t.indexOf("(") + 1,t.indexOf(")")).trim();
                dot.append("\"").append(source).append("\" -> \"").append(target).append("\" [label=\"").append(weight).append("\"];\n");
            }
        }
        dot.append("}");
        File out = new File(outputFile);
        if (out.getParentFile() != null){
            out.getParentFile().mkdirs();
        }
        Graphviz.fromString(dot.toString()).render(Format.PNG).toFile(out);
    }

    public List<Station> shortestPath(Station source,Station destination){
        HashMap<Station,Integer> dist=new HashMap<>();
        HashMap<Station,Station> parents=new HashMap<>();
        for(Station station:graph.keySet())
        {
            dist.put(station,Integer.MAX_VALUE);
            parents.put(station,null);
        }
        class State implements Comparable<State>{
            Station station;
            int distance;

            public Station getStation() {
                return station;
            }

            public int getDistance() {
                return distance;
            }


            public State(Station station, int distance) {
                this.station = station;
                this.distance = distance;
            }
            @Override
            public int compareTo(State state)
            {
                if(this.getDistance()<state.getDistance())
                    return -1;
                if(this.getDistance()>state.getDistance())
                    return 1;
                return 0;
            }
        }
        PriorityQueue<State> pq=new PriorityQueue<>();
        dist.put(source,0);
        pq.add(new State(source,0));

        while(!pq.isEmpty())
        {
            State u=pq.poll();
            if(u.getDistance()>dist.getOrDefault(u.getStation(),Integer.MAX_VALUE))
                continue;

            List<Edge> edges=graph.getOrDefault(u.getStation(),null);
            if(edges!=null)
            {
                for(Edge e:edges)
                {

                        int newDistance=dist.get(u.getStation())+e.getDistance();
                        if(newDistance<dist.get(e.getDestination()))
                        {
                            dist.put(e.getDestination(),newDistance);
                            parents.put(e.getDestination(),u.getStation());
                            pq.add(new State(e.getDestination(),newDistance));
                        }

                }
            }
        }
        List<Station> shortestPath=new ArrayList<>();
        Station currentStation=destination;
        if(dist.getOrDefault(destination,Integer.MAX_VALUE)==Integer.MAX_VALUE)
            return new ArrayList<>();
        while(currentStation!=null)
        {
            shortestPath.add(currentStation);
            currentStation=parents.get(currentStation);
        }
        java.util.Collections.reverse(shortestPath);
            return shortestPath;

    }
    public boolean dfs(Station station,HashMap<Station,Boolean> visited,HashMap<Station,Boolean> path)
    {
        visited.put(station,true);
        path.put(station,true);
        for(Edge edge:graph.getOrDefault(station,new ArrayList<>()))
        {
            if(!visited.getOrDefault(edge.getDestination(),false))
            {
                if(dfs(edge.getDestination(),visited,path))
                return true;
            }
            else if(path.getOrDefault(edge.getDestination(),false))
                return true;
        }
        path.put(station,false);
        return false;
    }

    public boolean ifTheGraphHasCycle()
    {
        HashMap<Station,Boolean> visited=new HashMap<>();
        HashMap<Station,Boolean> path=new HashMap<>();
        for(Station station:graph.keySet())
        {
            if(!visited.getOrDefault(station,false)) {
                if(dfs(station, visited, path))
                return true;
            }
        }
        return false;
    }
    
    
   

        short numOfEdgesOfEveryStation(Station station)
    {
        return (short)(graph.getOrDefault(station,null)==null?0:graph.getOrDefault(station,null).size());
    }

    ArrayList<Station> stationsOrderedByNumOfEdges()
    {
        ArrayList<Station> orderedStations=new ArrayList<>();
        for(Station station:graph.keySet())
        {
            orderedStations.add(station);
        }
        for(short i=0;i<orderedStations.size()-1;i++)
        {
            for(short j=0;j<orderedStations.size()-i-1;j++)
            {
                if(numOfEdgesOfEveryStation(orderedStations.get(j))<numOfEdgesOfEveryStation(orderedStations.get(j+1)))
                {
                    Station temp=orderedStations.get(j);
                    orderedStations.set(j,orderedStations.get(j+1));
                    orderedStations.set(j+1,temp);
                }
            }
        }
        return orderedStations;
    }
        
        public Set<Station> getStations() {
            return graph.keySet();
        }
        public HashMap<Station, List<Edge>> getGraph() {
            return graph;
        }

}