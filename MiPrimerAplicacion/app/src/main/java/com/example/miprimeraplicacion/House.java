package com.example.miprimeraplicacion;

import java.util.List;

public class House {
    private String id;
    private String provincia;
    private String canton;
    private String price;
    private String capacity;
    private String owner;
    private List<String> imageUrls;
    private List<String> amenities;

    public House(String id, String provincia, String canton, String price, String capacity, String owner, List<String> imageUrls, List<String> amenities) {
        this.id = id;
        this.provincia = provincia;
        this.canton = canton;
        this.price = price;
        this.capacity = capacity;
        this.owner = owner;
        this.imageUrls = imageUrls;
        this.amenities = amenities;
    }
    public String getId() {
        return id;
    }
    public String getProvincia() {
        return provincia;
    }

    public String getCanton() {
        return canton;
    }

    public String getPrice() {
        return price;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public List<String> getAmenities() {
        return amenities;
    }
}
