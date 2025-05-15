package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import at.fhv.sysarch.lab2.homeautomation.devices.Product;

import java.util.List;

public record Fridge(List<Product> product) implements FridgeCommand{
}
