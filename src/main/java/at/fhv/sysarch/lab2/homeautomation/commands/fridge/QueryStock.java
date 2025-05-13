package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import akka.actor.typed.ActorRef;

public final class QueryStock implements FridgeCommand {
    private final ActorRef<FridgeState> replyTo;

    public QueryStock(ActorRef<FridgeState> replyTo) {
        this.replyTo = replyTo;
    }

    public ActorRef<FridgeState> replyTo() {
        return replyTo;
    }
}


