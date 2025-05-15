package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;
import at.fhv.sysarch.lab2.homeautomation.order.PlaceOrder;
import java.util.*;

public class Fridge extends AbstractBehavior<FridgeCommand> {

    public static class ConsumeProduct implements FridgeCommand {
        public final String productName;

        public ConsumeProduct(String product) {
            this.productName = product;
        }
    }

    public static class OrderProduct implements FridgeCommand {
        public final String productName;
        public final Integer quantity;
        public final ActorRef<FridgeCommand> replyTo;

        public OrderProduct(String product, Integer quantity, ActorRef<FridgeCommand> replyTo) {
            this.productName = product;
            this.quantity = quantity;
            this.replyTo = replyTo;
        }
    }

    public static class FridgeResponse implements FridgeCommand {
        public final Boolean success;
        public final String message;

        public FridgeResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class FridgeState implements FridgeCommand {
        public final ActorRef<FridgeCommand> replyTo;

        public FridgeState(ActorRef<FridgeCommand> replyTo) {
            this.replyTo = replyTo;
        }
    }


    private final List<Product> storedProducts = new ArrayList<>();
//    private final List<Receipt> orderHistory = new ArrayList<>();

    private final int maxProducts = 20;
    private final double maxWeight = 100.0;

    private final ActorRef<PlaceOrder> orderExecutor;


    public static Behavior<FridgeCommand> create(ActorRef<PlaceOrder> orderExecutor) {
        return Behaviors.setup(context -> new Fridge(context, orderExecutor));
    }

    private Fridge(ActorContext<FridgeCommand> context, ActorRef<PlaceOrder> orderExecutor) {
        super(context);
        this.orderExecutor = orderExecutor;

        // Sample products for demo
        Product milk = new Product("Milk", 1, 20);
        Product bread = new Product("Bread", 1, 10);
        storedProducts.add(milk);
        storedProducts.add(bread);
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return this.<FridgeCommand>newReceiveBuilder()
                .onMessage(ConsumeProduct.class, this::onConsumeProduct)
                .onMessage(OrderProduct.class, this::onOrderProduct)
                .onMessage(FridgeState.class, this::onFridgeState)
                .onMessage(FridgeResponse.class, msg -> Behaviors.same())
                .build();
    }

    private Behavior<FridgeCommand> onConsumeProduct(ConsumeProduct msg) {
        Optional<Product> productOpt = storedProducts.stream()
                .filter(p -> p.getName().equalsIgnoreCase(msg.productName))
                .findFirst();

        productOpt.ifPresentOrElse(product -> {
            storedProducts.remove(product);
            getContext().getLog().info("Consumed {}", product.getName());

            long remaining = storedProducts.stream().filter(p -> p.getName().equals(product.getName())).count();
            if (remaining == 0) {
                getContext().getLog().info("{} is empty, auto reordering 1 unit", product.getName());
                // Sende die Nachricht in korrektem Format an den OrderExecutor
                orderExecutor.tell(new PlaceOrder(product.getName(), 1));
            }
        }, () -> getContext().getLog().warn("Product {} not found", msg.productName));

        return this;
    }


    private Behavior<FridgeCommand> onOrderProduct(OrderProduct msg) {
        int currentCount = storedProducts.size();
        double currentWeight = storedProducts.stream().mapToDouble(Product::getWeight).sum();

        Product sample = new Product(msg.productName, 1, 10); // dummy product
        int newTotal = currentCount + msg.quantity;
        double newWeight = currentWeight + (sample.getWeight() * msg.quantity);

        if (newTotal > maxProducts || newWeight > maxWeight) {
            msg.replyTo.tell(new FridgeResponse(false, "Not enough capacity or weight"));
        } else {
            // Sende Bestellung an den OrderExecutor
            orderExecutor.tell(new PlaceOrder(msg.productName, msg.quantity));
            msg.replyTo.tell(new FridgeResponse(true, "Order placed"));
        }

        return this;
    }


//    private Behavior<FridgeCommand> onOrderResponse(OrderResponseWrapper msg) {
//        Receipt receipt = msg.response.receipt();
//        orderHistory.add(receipt);
//        storedProducts.addAll(receipt.products());
//
//        getContext().getLog().info("Order arrived: {}", receipt);
//        return this;
//    }

    private Behavior<FridgeCommand> onFridgeState(FridgeState msg) {
        String storedProductsString = storedProducts.toString();

        msg.replyTo.tell(new FridgeResponse(true, storedProductsString));

        return this;
    }

//    private Behavior<FridgeCommand> onQueryOrderHistory(QueryOrderHistory msg) {
//        msg.replyTo.tell(new OrderHistoryMessage(orderHistory));
//        return this;
//    }

}
