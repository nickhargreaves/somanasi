package com.infinitedimensions.somanami.models;

/**
 * Created by Nick on 1/3/15.
 */

public class Friend {

    private String id;
    private String name;
    private String email;
    private String fid;

    public void setId(String _id){this.id=_id;}
    public void setName(String _name){this.name=_name;}
    public void setEmail(String _email){this.email=_email;}
    public void setFid(String _fid){this.fid=_fid;}

    public String getId(){return id;}
    public String getName(){return name;}
    public String getEmail(){return email;}
    public String getFid(){return fid;}

}
