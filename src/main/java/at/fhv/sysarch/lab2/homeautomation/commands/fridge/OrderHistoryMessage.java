package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import java.util.List;

public record OrderHistoryMessage(List<Receipt> receipts) {
}
