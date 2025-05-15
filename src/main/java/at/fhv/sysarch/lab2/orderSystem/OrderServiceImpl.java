package at.fhv.sysarch.lab2.orderSystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import at.fhv.sysarch.lab2.homeautomation.grpc.Order;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderService;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderCommand;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderReceived;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderReplyInternal;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OrderServiceImpl implements OrderService {

    private final ActorRef<OrderCommand> orderProcessor;
    private final ActorSystem<?> system;

    public OrderServiceImpl(ActorRef<OrderCommand> orderProcessor, ActorSystem<?> system) {
        this.orderProcessor = orderProcessor;
        this.system = system;
    }

    @Override
    public CompletionStage<OrderReply> order(OrderRequest in) {
        System.out.println("Received order: " + in.getProduct());
        // TODO: process order through actor OrderProcessor
        //orderProcessor.tell(in.getProduct());

        CompletionStage<OrderReplyInternal> ask =  AskPattern.<OrderCommand, OrderReplyInternal>ask(
                orderProcessor,
                replyTo -> new OrderReceived(in.getProduct(), replyTo),
                Duration.ofSeconds(3),
                system.scheduler()

        );

        CompletionStage<OrderReply> reply = ask.thenApply(orderReplyInternal -> OrderReply.newBuilder().setSuccessful(true).build());
        return reply;
    }
}
