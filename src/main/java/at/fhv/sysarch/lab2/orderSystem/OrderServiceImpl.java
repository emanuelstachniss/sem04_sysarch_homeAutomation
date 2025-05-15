package at.fhv.sysarch.lab2.orderSystem;

import akka.actor.typed.ActorRef;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OrderServiceImpl implements OrderService {

    private final ActorRef<String> orderProcessor;

    public OrderServiceImpl(ActorRef<String> orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public CompletionStage<OrderReply> order(OrderRequest in) {
        System.out.println("Received order: " + in.getProduct());
        // TODO: process order through actor OrderProcessor
        orderProcessor.tell(in.getProduct());

        return CompletableFuture.completedFuture(OrderReply.newBuilder().setSuccessful(true).build());
    }
}
