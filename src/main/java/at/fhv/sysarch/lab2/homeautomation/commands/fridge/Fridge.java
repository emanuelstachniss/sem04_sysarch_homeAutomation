package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import at.fhv.sysarch.lab2.orderSystem.Product;

import java.util.List;

public record Fridge(List<Product> product) implements FridgeCommand{
}
