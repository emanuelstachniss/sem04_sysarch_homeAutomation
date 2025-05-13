package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import java.util.List;

public class Receipt {
    private final List<Product> products;

    public Receipt(List<Product> products) {
        this.products = products;
    }

    public List<Product> products() {
        return products;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "products=" + products +
                '}';
    }
}
