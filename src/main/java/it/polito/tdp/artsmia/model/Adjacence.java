package it.polito.tdp.artsmia.model;

public class Adjacence
{
	private final Artist artist1;
	private final Artist artist2;
	private final int occurrencies;
	
	
	public Adjacence(Artist artist1, Artist artist2, int occurrencies)
	{
		this.artist1 = artist1;
		this.artist2 = artist2;
		this.occurrencies = occurrencies;
	}

	public Artist getArtist1()
	{
		return this.artist1;
	}

	public Artist getArtist2()
	{
		return this.artist2;
	}

	public int getOccurrencies()
	{
		return this.occurrencies;
	}	
	
	@Override
	public String toString()
	{
		return String.format("%s  -  %s  (#%d)", this.artist1.toString(), 
				this.artist2.toString(), this.occurrencies);
	}
}
