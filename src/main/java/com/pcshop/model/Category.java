package com.pcshop.model;
public class Category {
    private int catId; private String catName, description, icon; private boolean active;
    public int getCatId(){return catId;} public void setCatId(int v){catId=v;}
    public String getCatName(){return catName;} public void setCatName(String v){catName=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getIcon(){return icon;} public void setIcon(String v){icon=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
