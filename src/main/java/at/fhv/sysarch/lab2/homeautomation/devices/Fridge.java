package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;
import at.fhv.sysarch.lab2.orderSystem.OrderServiceClientActor;
import at.fhv.sysarch.lab2.orderSystem.Product;
import at.fhv.sysarch.lab2.orderSystem.Receipt;

import java.util.*;
import java.util.stream.Collectors;

public class Fridge extends AbstractBehavior<FridgeCommand> {

    private final List<Product> storedProducts = new ArrayList<>();
    private final List<Receipt> orderHistory = new ArrayList<>();

    private final int maxProducts = 20;
    private final double maxWeight = 100.0;

    private final ActorRef<OrderServiceClientActor.OrderCommand> orderClient;

    private final ActorRef<OrderServiceClientActor.OrderResponse> orderResponseAdapter;

    public static Behavior<FridgeCommand> create(ActorRef<OrderServiceClientActor.OrderCommand> orderClient) {
        return Behaviors.setup(ctx -> new Fridge(ctx, orderClient));
    }

    private Fridge(ActorContext<FridgeCommand> context, ActorRef<OrderServiceClientActor.OrderCommand> orderClient) {
        super(context);
        this.orderClient = orderClient;

        this.orderResponseAdapter = context.messageAdapter(OrderServiceClientActor.OrderResponse.class, OrderResponseWrapper::new);
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ConsumeProduct.class, this::onConsumeProduct)
                .onMessage(OrderProduct.class, this::onOrderProduct)
                .onMessage(OrderResponseWrapper.class, this::onOrderResponse)
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
                orderClient.tell(new OrderServiceClientActor.PlaceOrder(product.getName(), 1, orderResponseAdapter));
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
            orderClient.tell(new OrderServiceClientActor.PlaceOrder(msg.productName, msg.quantity, orderResponseAdapter));
            msg.replyTo.tell(new FridgeResponse(true, "Order placed"));
        }

        return this;
    }

    private Behavior<FridgeCommand> onOrderResponse(OrderResponseWrapper msg) {
        Receipt receipt = msg.response.receipt();
        orderHistory.add(receipt);
        storedProducts.addAll(receipt.products());

        getContext().getLog().info("Order arrived: {}", receipt);
        return this;
    }

    private Behavior<FridgeCommand> onQueryStock(QueryStock msg) {
        Map<String, Long> stockMap = storedProducts.stream()
                .collect(Collectors.groupingBy(Product::getName, Collectors.counting()));

        msg.replyTo.tell(new FridgeState(stockMap));
        return this;
    }

    private Behavior<FridgeCommand> onQueryOrderHistory(QueryOrderHistory msg) {
        msg.replyTo.tell(new OrderHistoryMessage(orderHistory));
        return this;
    }

    // Wrapper für Adapter
    private static class OrderResponseWrapper implements FridgeCommand {
        final OrderServiceClientActor.OrderResponse response;
        public OrderResponseWrapper(OrderServiceClientActor.OrderResponse response) {
            this.response = response;
        }
    }
}
