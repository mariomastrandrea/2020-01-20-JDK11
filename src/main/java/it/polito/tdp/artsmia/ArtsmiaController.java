package it.polito.tdp.artsmia;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.artsmia.model.Adjacence;
import it.polito.tdp.artsmia.model.Artist;
import it.polito.tdp.artsmia.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ArtsmiaController 
{
	private Model model;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCreaGrafo;

    @FXML
    private Button btnArtistiConnessi;

    @FXML
    private Button btnCalcolaPercorso;

    @FXML
    private ComboBox<String> boxRuolo;

    @FXML
    private TextField txtArtista;

    @FXML
    private TextArea txtResult;

    
    @FXML
    void doCreaGrafo(ActionEvent event) 
    {
    	String selectedRole = this.boxRuolo.getValue();
    	
    	if(selectedRole == null || selectedRole.isBlank())
    	{
    		this.txtResult.setText("Errore: devi selezionare un ruolo dal menù a tendina");
    		return;
    	}
    	
    	selectedRole = selectedRole.trim();
    	
    	this.model.createGraph(selectedRole);
    	
    	String output = this.printGraphInfo();
    	this.txtResult.setText(output);
    }
    
    private String printGraphInfo()
	{
		int numVertices = this.model.getNumVertices();
		int numEdges = this.model.getNumEdges();
		
		if(numVertices == 0)
			return "Errore: grafo vuoto!";
		
		String output = String.format("Grafo creato\n# Vertici: %d\n#Archi: %d",
				numVertices, numEdges);
    	
		return output;
	}

	@FXML
    void doArtistiConnessi(ActionEvent event) 
    {
    	List<Adjacence> orderedArtistCouples = this.model.getOrderedArtistCouples();
    	
    	if(orderedArtistCouples == null)
    	{
    		this.txtResult.setText("Errore: creare prima il grafo!");
    		return;
    	}
    	
    	String list = this.printCollection(orderedArtistCouples, "\n");
    	this.txtResult.setText(String.format("Coppie di artisti per numero di apparizioni comuni:\n%s", list));
    }

    private <T> String printCollection(Collection<T> collection, String separator)
	{
		StringBuilder sb = new StringBuilder();
		
		for(T element : collection)
		{
			if(sb.length() > 0)
				sb.append(separator);
			
			sb.append(element.toString());
		}
		
		return sb.toString();
	}

	@FXML
    void doCalcolaPercorso(ActionEvent event) 
    {
		if(!this.model.isGraphCreated())
		{
			this.txtResult.setText("Errore: devi prima creare il grafo");
    		return;
		}
		
    	String idArtistInput = this.txtArtista.getText();
    	
    	if(idArtistInput == null || idArtistInput.isBlank())
    	{
    		this.txtResult.setText("Errore: inserire un ID di un artista");
    		return;
    	}
    	
    	idArtistInput = idArtistInput.trim();
    	
    	int idArtist;
    	try
		{
			idArtist = Integer.parseInt(idArtistInput);
		}
		catch(NumberFormatException nfe)
		{
			this.txtResult.setText("Errore: inserire un valore intero per ID artista");
    		return;
		}
    	
    	boolean exists = this.model.existsArtist(idArtist);
    	
    	if(!exists)
    	{
    		this.txtResult.setText("Errore: non esiste alcun artista con ID = " + idArtist + 
    				" e ruolo " + this.model.getRole());
    		return;
    	}
    	
    	Collection<List<Artist>> bestPaths = this.model.searchBestPathsFrom(idArtist);
    	int numExhibitions = this.model.getPathNumExhibitions();
    	
    	String pathsOutput = this.printPaths(bestPaths);
    	char c1 = bestPaths.size() > 1 ? 'i' : 'o';
    	char c2 = bestPaths.size() > 1 ? 'i' : 'e';
    	
    	this.txtResult.setText(String.format("Percors%c miglior%c a partire dall'artista con ID = %d:\n\n%s\n\nNumero di esposizioni in comune: %d", 
    			c1, c2, idArtist, pathsOutput, numExhibitions));
    }

    private String printPaths(Collection<List<Artist>> bestPaths)
	{
		int numPaths = bestPaths.size();
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		for(List<Artist> path : bestPaths)
		{
			if(sb.length() > 0)
				sb.append("\n\n");
			
			String pathArtists = this.printCollection(path, ", ");
			
			if(numPaths > 1)
				sb.append("-".repeat(7)).append(" ").append(++count).append(" ").append("-".repeat(7)).append("\n");
			
			sb.append(" * ").append(pathArtists);
		}
		
		return sb.toString();
	}

	@FXML
    void initialize() 
    {
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Artsmia.fxml'.";
        assert btnArtistiConnessi != null : "fx:id=\"btnArtistiConnessi\" was not injected: check your FXML file 'Artsmia.fxml'.";
        assert btnCalcolaPercorso != null : "fx:id=\"btnCalcolaPercorso\" was not injected: check your FXML file 'Artsmia.fxml'.";
        assert boxRuolo != null : "fx:id=\"boxRuolo\" was not injected: check your FXML file 'Artsmia.fxml'.";
        assert txtArtista != null : "fx:id=\"txtArtista\" was not injected: check your FXML file 'Artsmia.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Artsmia.fxml'.";
    }
    
    public void setModel(Model model) 
    {
    	this.model = model;
    	
    	List<String> allRoles = this.model.getAllRoles();
    	this.boxRuolo.getItems().addAll(allRoles);
    }
    
    
}
