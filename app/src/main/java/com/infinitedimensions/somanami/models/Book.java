package com.infinitedimensions.somanami.models;


import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
/**
 * Created by nick on 12/12/14.
 */
public class Book {

    private String id;
    private String thumb_url;

    private String title;
    private String description;
    private String authors;
    private String date;
    private String url;

    private String owner;
    private String owner_name;
    private String publisher;
    private String gid;
    private String pages;
    private String categories;

    public String getOwner(){return this.owner;}
    public String getThumb_url(){
        return this.thumb_url;
    }
    public String getDate(){
        return this.date;
    }
    public String getUrl(){
        return this.url;
    }
    public String getAuthors(){return this.authors;}
    public String getOwnerName(){return this.owner_name;}
    public String getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getDescription(){return this.description;}
    public String getPublisher(){return this.publisher;}
    public String getGid(){return this.gid;}
    public String getPages(){return this.pages;}
    public String getCategories(){return this.categories;}

    public void setId(String _id){
        this.id = _id;
    }
    public void setPublisher(String _publisher){this.publisher=_publisher;}
    public void setGid(String _gid){this.gid=_gid;}
    public void setPages(String _pages){this.pages=_pages;}
    public void setThumb_url(String _thumburl){
        this.thumb_url = _thumburl;
    }
    public void setTitle(String _title){
        this.title = _title;
    }
    public void setDescription(String _description){
        this.description = _description;
    }
    public void setCategories(String _categories){this.categories = _categories; }
    public void setOwner(String _owner){this.owner= _owner; }
    public void setOwnerName(String _ownername){this.owner_name= _ownername; }
    public void setAuthors(String _authors){this.authors= _authors; }
    public void setUrl(String _url){this.owner_name= _url; }
    public void setDate(String _date){this.owner= _date; }

    public void parse(JsonParser parser) throws JsonParseException,
            IOException {
        JsonToken token = parser.nextToken();

        if (token == JsonToken.START_OBJECT) {

            while (token != JsonToken.END_OBJECT) {

                token = parser.nextToken();
                if (token == JsonToken.FIELD_NAME) {

                    String fieldName = parser.getCurrentName();

                    parser.nextToken();
                    if (0 == fieldName.compareToIgnoreCase("id")) {
                        this.id = parser.getText();
                    } else if (0 == fieldName.compareToIgnoreCase("thumb")) {
                        this.thumb_url = parser.getText();
                    } else if (0 == fieldName.compareToIgnoreCase("title")) {
                        this.title = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("description")) {
                        this.description = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("authors")) {
                        this.authors = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("categories")) {
                        this.categories = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("owner")) {
                        this.owner = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("owner_name")) {
                        this.owner_name = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("publisher")) {
                        this.publisher = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("url")) {
                        this.url = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("date")) {
                        this.date = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("gid")) {
                        this.gid = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("pages")) {
                        this.pages = parser.getText();
                    }
                }
            }
        }
    }

}