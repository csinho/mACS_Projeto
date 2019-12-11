package models.domain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import flexjson.transformer.DateTransformer;
import models.general.CommunicationObject;
import models.general.PaginacaoRetorno;
import models.general.UserData;
import models.general.Usuario;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.MimeTypes;
import sun.misc.BASE64Decoder;
import util.SerializeUtil;

/**
 * Events
 * @author joaoraf
 *
 */
@Entity(name="event")
public class Event extends Model{
	public static final String EVENTS_PATH = "public/images/events/";
	public static final String EVENTS_IMG_EXT = ".png";
	
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	private String imageName;

	@ManyToOne
	@JoinColumn(name = "id_municipio")
	private Municipio municipio;
	
	@ManyToOne
	@JoinColumn(name = "id_unidadesaude")
	private UnidadeSaude unidadeSaude;
	
	@ManyToOne
	@JoinColumn(name = "id_equipe")
	private Equipe equipe;
	
	@Transient
	private String image;
	
	// Logical exclusion
	private Boolean deleted = false;
	
	private Long previousId = null;

	/**
	 * Given a base64 string, return the original binary data
	 * @param base64 
	 * @return binary data
	 * @throws IOException
	 */
	public static byte[] getGetByteBase64(String base64) throws IOException {
		String base64Substring = base64.substring(base64.indexOf(',') + 1);
		return (new BASE64Decoder()).decodeBuffer(base64Substring);
	}
	
	/**
	 * Save the image associated with the event to disk.
	 * @param event
	 * @return The name (not path) to the stored image or null if not saved
	 */
	public static String saveImageToDisk(Event event){
		String img = event.getImage();
		if(img != null){
			try {
				// Convert the image to its binary format to store it
				byte[] binaryImg = getGetByteBase64(img);
				
				InputStream is = new ByteArrayInputStream(binaryImg);
				Blob blob = new Blob();
				blob.set(is, MimeTypes.getContentType("blob" + EVENTS_IMG_EXT));
				return blob.getFile().getName();
			} catch (IOException e) {
				//Do nothing
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Removes the image associated with the event from disk
	 * @param event
	 * @return true is the image was successfully deleted
	 */
	public static boolean removeImageFromDisk(Event event){
		try {
			File file = new File(EVENTS_PATH + event.imageName);
			return file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static CommunicationObject salvarForm(Event event){
		CommunicationObject commObj = new CommunicationObject();
		
		// Validations
		if(event.getTitle() == null){
        	commObj.setMsg("Campo Título é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
		}
		
		if(event.getDescription() == null){
        	commObj.setMsg("Campo Descrição é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
		}
		
		if(event.getDate() == null){
        	commObj.setMsg("Campo Data é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
		}
		
		if(event.getDate().compareTo(new Date()) < 0){
        	commObj.setMsg("Não é possível cadastrar uma data anterior ou igual à data atual!");
        	commObj.setSuccess(false);
            return commObj;
		}

		// Try to save the associated image to disk. 
		String imageName = saveImageToDisk(event);
		event.setImageName(imageName);
		
		try {
			
			if(event.id != null){
				// We update the old event by setting it to null
				Event oldEvent = Event.findById(event.id);
				oldEvent.setDeleted(true);
				oldEvent.save();
				
				Model.em().detach(event); // This event is no longer linked (if it was)
				event.setPreviousId(event.id);
				event.id = null; // We tell it should be a brand new event
				event.save(); // We can save without merge here
			}
			else {
				((Event)event.merge()).save();
			}
		}
		catch(Exception e){
        	commObj.setMsg("Erro ao tentar salvar o evento!");
        	commObj.setSuccess(false);
            return commObj; 
		}
		
		return commObj;
	}
	
	/**
	 * Get removed events (ids)
	 * @return List of ids of the excluded events
	 */
	public static List<Long> removedList(){
		return Event.em().createQuery("select id from event where deleted = true").getResultList();
	}
	
	/**
	 * Remove the image from the database
	 * @param id - Id of the event to be deleted
	 * @return CommunicationObject
	 */
    public static CommunicationObject remove(Long id) {
    	CommunicationObject obj = new CommunicationObject();

    	Event event = Event.findById(id);
    	
        try {
            //event.delete();
        	event.setDeleted(true);
        	((Event)event.merge()).save();
        	
            removeImageFromDisk(event);
            obj.setMsg("Evento excluído com sucesso");
        } catch (Exception e) {
            obj.setMsg("Erro ao tentar excluir o evento, tente novamente!");
            obj.setSuccess(false);
        }

        return obj;
    }
    
    
	/**
	 * Get an event
	 * @param page
	 */
	public static PaginacaoRetorno get(UserData udata, Integer page) {
		PaginacaoRetorno pr = new PaginacaoRetorno();
		
		StringBuilder q = new StringBuilder("1=1 ");

		// We should use the logged user. But how this will work for mobile?
		//Secure.usuarioLogado()
		
		List<Event> events;
		Long total = Event.count();
		
		if(udata != null){
			q.append(" AND (equipe = :equipe OR equipe is null) ");
			q.append(" AND (unidadeSaude = :unidadeSaude OR unidadeSaude is null) ");
			q.append(" AND (municipio = :municipio OR municipio is null) ");
			JPAQuery query = Event.find(q.toString() + "AND deleted = false order by date desc");
			query.setParameter("equipe", udata.getEquipe());
			query.setParameter("unidadeSaude", udata.getUnidadeSaude());
			query.setParameter("municipio", udata.getMunicipio());
			if(page == null) 
				events = query.fetch();
			else events = query.fetch(page, 10);
			
			// We do the same query but to count only
			Query countQuery = Event.em().createQuery("select count (e.id) from event e where " + q.toString());
			countQuery.setParameter("equipe", udata.getEquipe());
			countQuery.setParameter("unidadeSaude", udata.getUnidadeSaude());
			countQuery.setParameter("municipio", udata.getMunicipio());
			
			total = (Long) countQuery.getSingleResult();
		}
		else {
			JPAQuery query = Event.find("deleted = false order by date desc");
			if(page != null) 
				events = query.fetch(page, 10);
			else events = query.fetch();
		}

		pr.setLista(events);
		pr.setTotal(total);
		return pr;
	}
    
    
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return the description
	 */
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * Image name (not url) associated with the event
	 * @return
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * @param imageName - Name of the image associated with the event
	 */
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
	/**
	 * Get the base64 image associated with the event
	 * @return
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Defines the base64 image associated with the event
	 * @param image
	 */
	public void setImage(String image) {
		this.image = image;
	}


	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public UnidadeSaude getUnidadeSaude() {
		return unidadeSaude;
	}

	public void setUnidadeSaude(UnidadeSaude unidadeSaude) {
		this.unidadeSaude = unidadeSaude;
	}

	public Equipe getEquipe() {
		return equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	public Long getPreviousId() {
		return previousId;
	}

	public void setPreviousId(Long previousId) {
		this.previousId = previousId;
	}
}
