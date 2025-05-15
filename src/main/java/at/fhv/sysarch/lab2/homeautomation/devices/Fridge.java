package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;
import at.fhv.sysarch.lab2.orderSystem.OrderServiceClientActor;

import java.util.*;

public class Fridge extends AbstractBehavior<FridgeCommand> {

    // Constants
    private static final int MAX_PRODUCTS = 10;
    private static final int MAX_WEIGHT = 500;

    // State
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Product> productInfo = new HashMap<>();
    private final List<Receipt> orderHistory = new ArrayList<>();
    private final ActorRef<FridgeCommand> self;
    private final ActorRef<OrderServiceClientActor.OrderCommand> orderServiceClient;

    public static Behavior<FridgeCommand> create(ActorRef<OrderServiceClientActor.OrderCommand> orderServiceClient) {
        return Behaviors.setup(ctx -> new Fridge(ctx, orderServiceClient));
    }

    private Fridge(ActorContext<FridgeCommand> ctx, ActorRef<OrderServiceClientActor.OrderCommand> orderServiceClient) {
        super(ctx);
        this.self = ctx.getSelf();
        this.orderServiceClient = orderServiceClient;

        // Sample products for demo
        Product milk = new Product("Milk", 1, 20);
        Product bread = new Product("Bread", 1, 10);
        productInfo.put(milk.getName(), milk);
        productInfo.put(bread.getName(), bread);
        stock.merge(milk.getName(), 3, Integer::sum);
        stock.merge(bread.getName(), 2, Integer::sum);
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceiveReceipt.class, this::onReceiveReceipt)
                .onMessage(QueryStock.class, this::onQueryStock)
                .onMessage(QueryOrderHistory.class, this::onQueryOrderHistory)
                .build();
    }



    private Behavior<FridgeCommand> onReceiveReceipt(ReceiveReceipt msg) {
        Receipt receipt = msg.receipt();
        getContext().getLog().info("Order received: {}", receipt);

        for (Product p : receipt.products()) {
            stock.merge(p.getName(), p.getQuantity(), Integer::sum);
            productInfo.putIfAbsent(p.getName(), p);
        }

        orderHistory.add(receipt);

        return this;
    }


    private Behavior<FridgeCommand> onQueryStock(QueryStock msg) {
        FridgeState state = new FridgeState(new HashMap<>(stock));
        msg.replyTo().tell(state);
        return this;
    }

    private Behavior<FridgeCommand> onQueryOrderHistory(QueryOrderHistory msg) {
        msg.replyTo().tell(orderHistory);
        return this;
    }
}
