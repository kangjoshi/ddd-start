public class OrderLine {

    private Product product;
    private int price;
    private int quantity;
    private int amount;

    public OrderLine(Product product, int price, int quantity) {
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.amount = calculateAmounts();
    }

    private int calculateAmounts() {
        return price * quantity;
    }

    public int getAmount() {
        return amount;
    }
}
