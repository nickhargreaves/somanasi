package com.infinitedimensions.somanami;


import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;

/**
 * Created by nick on 12/12/14.
 */
public class TrayItem {

    private String id;
    private String date_due;
    private String borrowed;
    private String lent;
    private String person_id;
    private String person_name;
    private String book_id;
    private String book_thumb;
    private String book_title;

    public String getId(){return this.id;}
    public String getDate_due(){return this.date_due;}
    public String getBorrowed(){return this.borrowed; }
    public String getLent(){return this.lent;}
    public String getPerson_id(){return this.person_id;}
    public String getPerson_name(){return this.person_name;}
    public String getBook_id(){return this.book_id;}
    public String getBook_thumb(){return this.book_thumb;}
    public String getBook_title(){return this.book_title;}


    public void setId(String _id){this.id=_id;}
    public void setDate_due(String _date_due){this.date_due=_date_due;}
    public void setBorrowed(String _borrowed){this.borrowed=_borrowed; }
    public void setLent(String _lent){this.lent=_lent;}
    public void setPerson_id(String _person_id){this.person_id=_person_id;}
    public void setPerson_name(String _person_name){this.person_name=_person_name;}
    public void setBook_id(String _book_id){this.book_id=_book_id;}
    public void setBook_thumb(String _book_thumb){this.book_thumb=_book_thumb;}
    public void setBook_title(String _book_title){this.book_title=_book_title;}


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
                    } else if (0 == fieldName.compareToIgnoreCase("date_due")) {
                        this.date_due = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("borrowed")) {
                        this.borrowed = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("lent")) {
                        this.lent = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("person_id")) {
                        this.person_id = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("person_name")) {
                        this.person_name = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("book_id")) {
                        this.book_id = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("book_thumb")) {
                        this.book_thumb = parser.getText();
                    }else if (0 == fieldName.compareToIgnoreCase("book_title")) {
                        this.book_title = parser.getText();
                    }
                }
            }
        }
    }

}