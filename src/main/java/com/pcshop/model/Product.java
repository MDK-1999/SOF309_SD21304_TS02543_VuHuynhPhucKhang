package com.pcshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Product {
    private int productId, catId, stockQty;
    private String productName, brand, model, description, specs, imageUrl, catName;
    private String officialUrl, officialSourceName;
    private List<ProductSpec> specificationItems = List.of();
    private BigDecimal price;
    private boolean active;
    private LocalDateTime createdAt;

    public int getProductId(){return productId;} public void setProductId(int v){productId=v;}
    public int getCatId(){return catId;} public void setCatId(int v){catId=v;}
    public String getCatName(){return catName;} public void setCatName(String v){catName=v;}
    public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
    public String getBrand(){return brand;} public void setBrand(String v){brand=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getSpecs(){return specs;} public void setSpecs(String v){specs=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public String getOfficialUrl(){return officialUrl;} public void setOfficialUrl(String v){officialUrl=v;}
    public String getOfficialSourceName(){return officialSourceName;} public void setOfficialSourceName(String v){officialSourceName=v;}
    public List<ProductSpec> getSpecificationItems(){return specificationItems;}
    public void setSpecificationItems(List<ProductSpec> v){specificationItems=v==null?List.of():List.copyOf(v);}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
    public int getStockQty(){return stockQty;} public void setStockQty(int v){stockQty=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public String getStockStatus(){ return stockQty==0?"Hết hàng":stockQty<5?"Sắp hết":"Còn hàng"; }
}
