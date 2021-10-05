package com.nic.Inspection.Model;

/**
 * Created by nimble on 12/1/2017.
 */

public class ImageModel {
    private String url;
    private int id;


    public ImageModel(String url) {
        this.url = url;
    }

    public ImageModel(int id, String url, int questionId) {
        this.id = id;
        this.url = url;

    }


    public ImageModel(int id, String url) {
        this.id = id;
        this.url = url;
    }

    public ImageModel() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
