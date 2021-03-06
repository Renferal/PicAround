package com.project.pervsys.picaround.domain;

import java.util.HashMap;

public class Place {

    private String id;
    private String name;
    private double lat;
    private double lon;
    private String type;
    private HashMap<String, Picture> pictures;
    private double popularity = 1.;


    public Place(){
        // Default constructor required for calls to DataSnapshot.getValue(Point.class)
    }

    public void addPicture(Picture picture){
        if (pictures == null)
            pictures = new HashMap<>();
        pictures.put(picture.getId(), picture);
    }

    public HashMap<String, Picture> getPictures(){
        return pictures;
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
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public void setPopularity(double popularity){
        this.popularity = popularity;
    }

    public double getPopularity(){
        return popularity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Place place = (Place) o;

        return id.equals(place.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Point{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", type='" + type + '\'' +
                ", popularity=" + popularity +
                '}';
    }
}
