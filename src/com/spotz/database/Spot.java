package com.spotz.database;

public class Spot {

	private int id;
    private String name;
    private String description;
    private String type;
    private String typeId;
    private String latitude;
    private String longitude;
    private String userid;
    private String imagePath;

    public Spot(){}
 
    public Spot(String name, String description, String type, String typeId, String latitude, String longitude, String userid, String imagePath) {
        super();
        this.name = name;
        this.description = description;
        this.type = type;
        this.typeId = typeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userid = userid;
        this.imagePath = imagePath;
    }
 
    //getters & setters
    public int getId(){
    	return id;
    }
    
    public void setId(int id){
    	this.id = id;
    }
    
    public String getName(){
    	return name;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public String getDescription(){
    	return description;
    }
    
    public void setDescription(String description){
    	this.description = description;
    }
    
    public String getType(){
    	return type;
    }
    
    public void setType(String type){
    	this.type = type;
    }
    
    public String getTypeId(){
    	return typeId;
    }
    
    public void setTypeId(String typeId){
    	this.typeId = typeId;
    }
    
    public String getLatitude(){
    	return latitude;
    }
    
    public void setLatitude(String latitude){
    	this.latitude = latitude;
    }
    
    public String getLongitude(){
    	return longitude;
    }
    
    public void setLongitude(String longitude){
    	this.longitude = longitude;
    }
    
    public String getUserid(){
    	return userid;
    }
    
    public void setUserid(String userid){
    	this.userid = userid;
    }
    
    public String getImagepath(){
    	return imagePath;
    }
    
    public void setImagepath(String imagePath){
    	this.imagePath = imagePath;
    }
    
    @Override
    public String toString() {
        return "Spot [id=" + id + ", nam=" + name + ", description=" + description+ " TYPEID = "+ typeId
                + "Image = "+imagePath+"]";
    }
}
