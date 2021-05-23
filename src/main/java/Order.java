import java.util.List;

public class Order {

    private List<OrderLine> orderLines;
    private int totalAmounts;

    private OrderState orderState;
    private ShippingInfo shippingInfo;

    public Order(List<OrderLine> orderLines, ShippingInfo shippingInfo) {
        setOrderLines(orderLines);
        setShippingInfo(shippingInfo);
    }

    private void setShippingInfo(ShippingInfo shippingInfo) {
        if (shippingInfo == null) {
            throw new IllegalArgumentException("배송 정보는 필수로 입력 되어야 합니다.");
        }
        this.shippingInfo = shippingInfo;
    }

    private void setOrderLines(List<OrderLine> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("적어도 하나 이상의 상품이 포함되어야 합니다.");
        }

        this.orderLines = orderLines;
        calculateTotalAmounts();
    }

    private void calculateTotalAmounts() {
        this.totalAmounts = orderLines.stream()
                .mapToInt(OrderLine::getAmount)
                .sum();
    }

    public void changeShippingInfo(ShippingInfo newShippingInfo) {
        if (!orderState.isShippingChangeable()) {
            throw new IllegalStateException("배송 상태를 변경할 수 없습니다.");
        }

        this.shippingInfo = newShippingInfo;
    }

    public void changeShipped() {
        this.orderState = OrderState.SHIPPED;
    }

    public void cancel() {
        if (!orderState.isShippingChangeable()) {
            throw new IllegalStateException("배송 중인 상품은 취소할 수 없습니다.");
        }
        this.orderState = OrderState.CANCEL;
    }

    public void completePayment() {

    }

}
