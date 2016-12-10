package rh.test.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdModel {
	@JsonProperty(value = "id")
	private String id;
	
	@JsonProperty(value = "name")
	private String name;

	private String normalizedName;
	
	public IdModel() {		
	}
	
	public IdModel(String id, String name) {
		setId(id);
		setName(name);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.normalizedName = Util.normalize(name);
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	@Override
	public String toString() {
		return "IdModel [id=" + id + ", name=" + name + ", normalizedName=" + normalizedName + "]";
	}
}
