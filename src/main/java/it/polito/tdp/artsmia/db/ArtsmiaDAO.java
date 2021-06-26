package it.polito.tdp.artsmia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;

import it.polito.tdp.artsmia.model.Adjacence;
import it.polito.tdp.artsmia.model.Artist;

public class ArtsmiaDAO 
{
	public List<String> getAllAuthorsRoles()
	{
		final String sqlQuery = String.format("%s %s %s",
										"SELECT DISTINCT role",
										"FROM authorship",
										"ORDER BY role ASC");
		
		List<String> allRoles = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next()) 
			{
				String role = queryResult.getString("role");
				allRoles.add(role);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return allRoles;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllAuthorsRoles()", sqle);
		}
	}

	public Collection<Artist> getAllArtistsOfRole(String role, Map<Integer, Artist> artistsIdMap)
	{
		final String sqlQuery = String.format("%s %s %s",
				"SELECT DISTINCT ar.artist_id AS id, ar.name AS name",
				"FROM authorship AS au, artists AS ar",
				"WHERE au.artist_id = ar.artist_id AND au.role = ?");
		
		Collection<Artist> artists = new HashSet<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setString(1, role);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next()) 
			{
				int artistId = queryResult.getInt("id");				
				String artistName = queryResult.getString("name");
				
				if(!artistsIdMap.containsKey(artistId))
				{
					Artist newArtist = new Artist(artistId, artistName);
					artistsIdMap.put(artistId, newArtist);
					artists.add(newArtist);
				}
				else
					artists.add(artistsIdMap.get(artistId));
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return artists;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllArtistsOfRole()", sqle);
		}		
	}

	public Collection<Adjacence> getAdjacences(String role, Map<Integer, Artist> artistsIdMap)
	{
		final String sqlQuery = String.format("%s %s %s %s %s %s",
				"SELECT a1.artist_id AS id1, a2.artist_id AS id2, COUNT(DISTINCT e1.exhibition_id) AS count",
				"FROM authorship AS a1, authorship AS a2, exhibition_objects AS e1, exhibition_objects AS e2",
				"WHERE a1.object_id = e1.object_id AND a2.object_id = e2.object_id",
						"AND a1.role = a2.role AND a1.role = ?",
						"AND a1.artist_id < a2.artist_id AND e1.exhibition_id = e2.exhibition_id",
				"GROUP BY a1.artist_id, a2.artist_id");
		
		Collection<Adjacence> adjacences = new HashSet<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setString(1, role);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next()) 
			{
				try
				{
					int id1 = queryResult.getInt("id1");
					int id2 = queryResult.getInt("id2");
					
					if(!artistsIdMap.containsKey(id1) || !artistsIdMap.containsKey(id2))
						throw new RuntimeException("Artist id not found in idMap");
					
					Artist artist1 = artistsIdMap.get(id1);
					Artist artist2 = artistsIdMap.get(id2);
					int occurrencies = queryResult.getInt("count");
					
					Adjacence newAdjacence = new Adjacence(artist1, artist2, occurrencies);
					adjacences.add(newAdjacence);
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return adjacences;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAdjacences()", sqle);
		}		
	}
	
}
