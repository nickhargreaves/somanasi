package com.infinitedimensions.somanami;


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
    private String categories;
    private String owner;

    public String getOwner(){return this.owner;}
    public String getId(){return this.id;}
    public String getThumb_url(){
        return this.thumb_url;
    }
    public String getTitle(){
        return this.title;
    }
    public String getDescription(){
        return this.description;
    }
    public String getDate(){
        return this.date;
    }
    public String getUrl(){
        return this.url;
    }
    public String getAuthors(){return this.authors;}
    public String getCategories(){return this.categories;}

    public void setId(String _id){
        this.id = _id;
    }

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
                    }
                }
            }
        }
    }

}