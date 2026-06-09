package ru.demoexam.shoestore.model;

import java.util.Objects;

public class Product {
    private String article;
    private String name;
    private LookupValue category;
    private String description;
    private LookupValue manufacturer;
    private LookupValue supplier;
    private double price;
    private String unit;
    private int stockQuantity;
    private int discountPercent;
    private String imagePath;

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LookupValue getCategory() {
        return category;
    }

    public void setCategory(LookupValue category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LookupValue getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(LookupValue manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LookupValue getSupplier() {
        return supplier;
    }

    public void setSupplier(LookupValue supplier) {
        this.supplier = supplier;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getDiscountedPrice() {
        return price - (price * discountPercent / 100.0);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Product product)) {
            return false;
        }
        return Objects.equals(article, product.article);
    }

    @Override
    public int hashCode() {
        return Objects.hash(article);
    }
}
