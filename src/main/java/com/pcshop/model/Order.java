package com.pcshop.model;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public class Order {
    private int orderId, staffId; private Integer userId;
    private String customerName, customerPhone, customerEmail, shippingAddr, orderType, status, paymentMethod, paymentStatus, notes;
    private String staffName, customerUsername;
    private BigDecimal totalAmount, discount, finalAmount;
    private LocalDateTime createdAt, updatedAt;
    private List<OrderItem> items;
    public int getOrderId(){return orderId;} public void setOrderId(int v){orderId=v;}
    public Integer getUserId(){return userId;} public void setUserId(Integer v){userId=v;}
    public int getStaffId(){return staffId;} public void setStaffId(int v){staffId=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public String getCustomerPhone(){return customerPhone;} public void setCustomerPhone(String v){customerPhone=v;}
    public String getCustomerEmail(){return customerEmail;} public void setCustomerEmail(String v){customerEmail=v;}
    public String getShippingAddr(){return shippingAddr;} public void setShippingAddr(String v){shippingAddr=v;}
    public String getOrderType(){return orderType;} public void setOrderType(String v){orderType=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;}
    public String getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(String v){paymentStatus=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public String getStaffName(){return staffName;} public void setStaffName(String v){staffName=v;}
    public String getCustomerUsername(){return customerUsername;} public void setCustomerUsername(String v){customerUsername=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public BigDecimal getDiscount(){return discount;} public void setDiscount(BigDecimal v){discount=v;}
    public BigDecimal getFinalAmount(){return finalAmount;} public void setFinalAmount(BigDecimal v){finalAmount=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public List<OrderItem> getItems(){return items;} public void setItems(List<OrderItem> v){items=v;}
}
