public enum OrderState {
    PAYMENT_WATTING {
        @Override
        public boolean isShippingChangeable() {
            return true;
        }
    },
    PREPARING {
        @Override
        public boolean isShippingChangeable() {
            return true;
        }
    },
    SHIPPED, DELIVERING, DELIVERY_COMPLETED, CANCEL;

    public boolean isShippingChangeable() {
        return false;
    }
}
