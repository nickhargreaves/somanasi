package com.infinitedimensions.somanami.models;

/**
 * Created by Nick on 1/3/15.
 */

public class Book {

    private String id;
    private String title;
    private String description;
    private String thumb;
    private String publisher;
    private String gid;
    private String author;
    private String pages;
    private String categories;
    private String user;

    public void setId(String _id){this.id=_id;}
    public void setTitle(String _title){this.title=_title;}
    public void setDescription(String _description){this.description=_description;}
    public void setThumb(String _thumb){this.thumb=_thumb;}
    public void setPublisher(String _publisher){this.publisher=_publisher;}
    public void setGid(String _gid){this.gid=_gid;}
    public void setAuthor(String _author){this.author=_author;}
    public void setPages(String _pages){this.pages=_pages;}
    public void setCategories(String _categories){this.categories=_categories;}
    public void setUser(String _user){this.user=_user;}

    public String getId(){return id;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public String getThumb(){return thumb;}
    public String getPublisher(){return publisher;}
    public String getGid(){return gid;}
    public String getAuthor(){return author;}
    public String getPages(){return pages;}
    public String getCategories(){return categories;}
    public String getUser(){return user;}

}
