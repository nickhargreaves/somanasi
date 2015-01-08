package com.infinitedimensions.somanami.models;

/**
 * Created by nick on 1/3/15.
 */
public class NotificationGCM {

    private String id;
    private String book;
    private String message;
    private String type;
    private String user;

    public void setId(String _id){this.id=_id;}
    public void setBook(String _book){this.book=_book;}
    public void setMessage(String _message){this.message=_message;}
    public void setType(String _type){this.type=_type;}
    public void setUser(String _user){this.user=_user;}

    public String getId(){return this.id;}
    public String getBook(){return this.book;}
    public String getMesage(){return this.message;}
    public String getType(){return this.type;}
    public String getUser(){return this.user;}

}
