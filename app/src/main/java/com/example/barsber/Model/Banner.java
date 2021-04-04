package com.example.barsber.Model;

public class Banner {
    //Lookbook and banner is same
    private String image;


    //new
    private String nameSalon;
    public String getNameSalon() {
        return nameSalon;
    }
    public void setNameSalon(String nameSalon) {
        this.nameSalon = nameSalon;
    }
    public Banner(String image, String nameSalon) {
        this.image = image;
        this.nameSalon = nameSalon;
    }
    //

    public Banner(){}

    public Banner(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
