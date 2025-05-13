package at.fhv.sysarch.lab2.homeautomation.commands.fridge;


import java.util.Map;

public class FridgeState {
    private final Map<String, Integer> stock;

    public FridgeState(Map<String, Integer> stock) {
        this.stock = stock;
    }

    public Map<String, Integer> getStock() {
        return stock;
    }

}
