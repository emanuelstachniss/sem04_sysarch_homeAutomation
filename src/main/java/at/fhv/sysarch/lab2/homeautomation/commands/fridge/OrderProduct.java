package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

public record OrderProduct(String productName, int quantity) implements FridgeCommand {
}
