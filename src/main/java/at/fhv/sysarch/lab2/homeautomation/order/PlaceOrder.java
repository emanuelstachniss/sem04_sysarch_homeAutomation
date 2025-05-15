package at.fhv.sysarch.lab2.homeautomation.order;

public class PlaceOrder {
    public final String productName;
    public final int quantity;

    public PlaceOrder(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Order [productName=" + productName + ", quantity=" + quantity + "]";
    }
}
