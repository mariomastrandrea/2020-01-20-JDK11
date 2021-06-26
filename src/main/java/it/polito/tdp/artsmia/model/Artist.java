package it.polito.tdp.artsmia.model;

public class Artist
{
	private final int id;
	private final String name;
	
	
	public Artist(int artistId, String name)
	{
		this.id = artistId;
		this.name = name;
	}

	public int getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String toString()
	{
		return String.format("%d - %s", this.id, this.name);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + this.id;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artist other = (Artist) obj;
		if (this.id != other.id)
			return false;
		return true;
	}
}
