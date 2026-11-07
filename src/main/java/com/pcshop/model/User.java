package com.pcshop.model;
import java.time.LocalDateTime;
public class User {
    private int userId; private String username, password, fullName, email, phone, address, role;
    private boolean active; private LocalDateTime createdAt, lastLogin;
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getLastLogin(){return lastLogin;} public void setLastLogin(LocalDateTime v){lastLogin=v;}
    public boolean isAdmin(){return "admin".equals(role);}
    public boolean isStaff(){return "staff".equals(role)||"admin".equals(role);}
    public boolean isCustomer(){return "customer".equals(role);}
}
