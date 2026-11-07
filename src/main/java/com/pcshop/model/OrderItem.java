package com.pcshop.model;
import java.math.BigDecimal;
public class OrderItem {
    private int itemId, orderId, productId, quantity; private String productName;
    private BigDecimal unitPrice, subtotal;
    public int getItemId(){return itemId;} public void setItemId(int v){itemId=v;}
    public int getOrderId(){return orderId;} public void setOrderId(int v){orderId=v;}
    public int getProductId(){return productId;} public void setProductId(int v){productId=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
    public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
}
