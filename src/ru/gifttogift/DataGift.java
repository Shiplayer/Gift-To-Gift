package ru.gifttogift;

import java.util.ArrayList;

public class DataGift {
    private int id;
    private String name;
    private String url;
    private String category;
    private String description;
    private double price;
    private String code;
    private ArrayList<String> images;

    public DataGift(int id, String name, String description, String category, String url, double price, String code){
        this.id = id;
        this.name = name;
        this.url = url;
        this.category = category;
        this.description = description;
        this.price = price;
        this.code = code;
        images = new ArrayList<>();
    }

    public DataGift(String name, String url, String giftsCategory, String description, double price, String codeGift) {
        this.name = name;
        this.url = url;
        this.category = giftsCategory;
        this.description = description;
        this.price = price;
        this.code = codeGift;
        images = new ArrayList<>();
    }

    public void AddImagePath(String path){
        images.add(path);
    }

    public int getId(){
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    @Override
    public String toString() {
        return id + "\t" + name + "\t" + description + "\t" + category + "\t" + url + "\t" + price + "\t" + code + "\n" + images;
    }
}
