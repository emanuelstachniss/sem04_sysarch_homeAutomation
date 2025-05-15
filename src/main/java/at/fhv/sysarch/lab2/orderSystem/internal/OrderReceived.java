package at.fhv.sysarch.lab2.orderSystem.internal;

import akka.actor.typed.ActorRef;

public record OrderReceived(String product, ActorRef<OrderReplyInternal> replyTo) implements OrderCommand {

}
