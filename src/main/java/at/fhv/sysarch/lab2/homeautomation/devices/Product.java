package at.fhv.sysarch.lab2.orderSystem;

public class Product {
    private final String name;
    private final int quantity;
    private final double weight;

    public Product(String name, int quantity, double weight) {
        this.name = name;
        this.quantity = quantity;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getWeight() {
        return weight;
    }
}
