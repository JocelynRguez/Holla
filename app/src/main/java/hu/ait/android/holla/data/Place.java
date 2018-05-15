package hu.ait.android.holla.data;

import android.widget.ImageView;

public class Place {

    private String name;
    private String rating;
    private boolean open;
    private String imgURL;
    private String address;
    private boolean fav;


    public Place(String name, String rating, boolean open, String address, String imgURL, boolean fav) {
        this.name = name;
        this.rating = rating;
        this.open = open;
        this.imgURL = imgURL;
        this.address = address;
        this.fav = fav;
    }

    public Place() {
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public boolean getDescription() {
        return open;
    }

    public void setDescription(boolean description) {
        this.open = open;
    }

    public String getImg() {
        return imgURL;
    }

    public void setImg(String imgURL) {
        this.imgURL = imgURL;
    }
}
