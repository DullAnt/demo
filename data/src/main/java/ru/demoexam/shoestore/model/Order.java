package ru.demoexam.shoestore.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private LookupValue pickupPoint;
    private String clientFullName;
    private int pickupCode;
    private String status;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private final List<OrderItem> items = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LookupValue getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(LookupValue pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public void setClientFullName(String clientFullName) {
        this.clientFullName = clientFullName;
    }

    public int getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(int pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void replaceItems(List<OrderItem> orderItems) {
        items.clear();
        items.addAll(orderItems);
    }

    public String getItemsSummary() {
        return items.stream()
            .map(item -> item.getProduct().getArticle() + ", " + item.getQuantity())
            .reduce((left, right) -> left + ", " + right)
            .orElse("");
    }
}
