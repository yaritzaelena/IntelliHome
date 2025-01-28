package com.example.miprimeraplicacion;

import java.util.List;

public class House {
    private String description;
    private String rules;
    private String price;
    private String capacity;
    private String provincia;
    private String canton;
    private String location;
    private List<String> photos;
    private List<String> amenities;

    public House(String description, String rules, String price, String capacity,
                 String provincia, String canton, String location, List<String> photos, List<String> amenities) {
        this.description = description;
        this.rules = rules;
        this.price = price;
        this.capacity = capacity;
        this.provincia = provincia;
        this.canton = canton;
        this.location = location;
        this.photos = photos;
        this.amenities = amenities;
    }

    public String getDescription() { return description; }
    public String getRules() { return rules; }
    public String getPrice() { return price; }
    public String getCapacity() { return capacity; }
    public String getProvincia() { return provincia; }
    public String getCanton() { return canton; }
    public String getLocation() { return location; }
    public List<String> getPhotos() { return photos; }
    public List<String> getAmenities() { return amenities; }
}

