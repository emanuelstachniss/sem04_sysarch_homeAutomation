package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import akka.actor.typed.ActorRef;

import java.util.List;

public record QueryOrderHistory(ActorRef<List<Receipt>> replyTo) implements FridgeCommand {
}
