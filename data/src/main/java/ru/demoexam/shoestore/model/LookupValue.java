package ru.demoexam.shoestore.model;

public record LookupValue(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
