package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

public class Product {

    private String name;
    private int quantity;
    private int weight;

    public Product(String name, int quantity, int weight) {
        this.name = name;
        this.quantity = quantity;
        this.weight = weight;
    }

    // Getter und Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return name + " x" + quantity + " (" + weight + "kg)";
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }
}
