package at.fhv.sysarch.lab2.orderSystem;

import java.util.List;

public class Receipt {
    private final List<Product> products;

    public Receipt(List<Product> products) {
        this.products = products;
    }

    public List<Product> products() {
        return products;
    }
}
