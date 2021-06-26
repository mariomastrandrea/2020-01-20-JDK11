package it.polito.tdp.artsmia.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import it.polito.tdp.artsmia.db.ArtsmiaDAO;

public class Model 
{
	private List<String> allRoles;
	private final ArtsmiaDAO dao;
	private Graph<Artist, DefaultWeightedEdge> graph;
	private String role;
	private final Map<Integer, Artist> artistsIdMap;
	
	private Collection<List<Artist>> bestPaths;
	private int maxPathLength;
	private int numExhibitions;
	
	
	public Model()
	{
		this.dao = new ArtsmiaDAO();
		this.artistsIdMap = new HashMap<>();
		this.bestPaths = new HashSet<>();
	}
	
	public List<String> getAllRoles()
	{
		if(this.allRoles == null)
			this.allRoles = this.dao.getAllAuthorsRoles();
		
		return this.allRoles;
	}
	
	public void createGraph(String role)
	{
		this.graph = GraphTypeBuilder.<Artist, DefaultWeightedEdge>undirected()
									.allowingMultipleEdges(false)
									.allowingSelfLoops(false)
									.weighted(true)
									.edgeClass(DefaultWeightedEdge.class)
									.buildGraph();
		
		this.role = role;
		
		//add vertices
		Collection<Artist> vertices = this.dao.getAllArtistsOfRole(role, this.artistsIdMap);
		Graphs.addAllVertices(this.graph, vertices);
		
		//add edges
		Collection<Adjacence> adjacences = this.dao.getAdjacences(role, this.artistsIdMap);
		
		for(Adjacence a : adjacences)
		{
			Artist artist1 = a.getArtist1();
			Artist artist2 = a.getArtist2();
			int weight = a.getOccurrencies();
			
			Graphs.addEdge(this.graph, artist1, artist2, (double)weight);
		}					
	}

	public int getNumVertices() { return this.graph.vertexSet().size(); }
	public int getNumEdges() { return this.graph.edgeSet().size(); }
	public String getRole() { return this.role; } 
	public boolean isGraphCreated() { return this.graph != null; }
	
	public List<Adjacence> getOrderedArtistCouples()
	{
		if(this.graph == null) return null;
		
		List<Adjacence> adjacences = new ArrayList<>();
		
		for(var edge : this.graph.edgeSet())
		{
			Artist a1 = this.graph.getEdgeSource(edge);
			Artist a2 = this.graph.getEdgeTarget(edge);
			int occurrencies = (int)this.graph.getEdgeWeight(edge);
			
			adjacences.add(new Adjacence(a1, a2, occurrencies));
		}
		
		adjacences.sort((a1, a2) -> Integer.compare(a2.getOccurrencies(), a1.getOccurrencies()));
		return adjacences;
	}
	
	public boolean existsArtist(int artistId)
    {
    	if(!this.artistsIdMap.containsKey(artistId))
    		return false;
    	
    	Artist artist = this.artistsIdMap.get(artistId);
    	
    	return this.graph.vertexSet().contains(artist);
    }

	public Collection<List<Artist>> searchBestPathsFrom(int idArtist)
	{
		Artist startArtist = this.artistsIdMap.get(idArtist);
		
		if(startArtist == null) return null;
		
		this.maxPathLength = 0;
		this.numExhibitions = 0;
		
		List<Artist> partialSolution = new ArrayList<>();
		Set<Artist> partialSolutionSet = new HashSet<>();
		partialSolution.add(startArtist);
		
		this.searchBestPathsRecursively(partialSolution, partialSolutionSet, 0);
		
		return this.bestPaths;
	}
	
	private void searchBestPathsRecursively(List<Artist> partialSolution, 
			Set<Artist> partialSolutionSet, int commonNumExhibition)
	{
		Artist lastNode = partialSolution.get(partialSolution.size() - 1);
		boolean flag = false;	//it indicates if exists at least 1 valid adjacent artist
		
		for(var edge : this.graph.edgesOf(lastNode))
		{
			int weight = (int)this.graph.getEdgeWeight(edge);
			Artist nextNode = Graphs.getOppositeVertex(this.graph, edge, lastNode);
			
			if((commonNumExhibition != 0 && commonNumExhibition != weight)
					|| partialSolutionSet.contains(nextNode)) 
				continue; //no way
			
			flag = true;
			partialSolution.add(nextNode);
			partialSolutionSet.add(nextNode);
			this.searchBestPathsRecursively(partialSolution, partialSolutionSet, weight);
			
			partialSolution.remove(partialSolution.size() - 1); //backtracking	
			partialSolutionSet.remove(nextNode);
		}
		
		if(!flag)	//updates only if there are not next nodes
		{
			//path ended
			int pathLength = partialSolution.size();
			
			if(pathLength >= this.maxPathLength)
			{
				if(pathLength > this.maxPathLength)
				{
					this.bestPaths = new HashSet<>();
					this.numExhibitions = commonNumExhibition;
					this.maxPathLength = pathLength;
				}
				
				this.bestPaths.add(new ArrayList<>(partialSolution));
			}
		}
	}

	public int getPathNumExhibitions()
	{
		return this.numExhibitions;
	}
}
