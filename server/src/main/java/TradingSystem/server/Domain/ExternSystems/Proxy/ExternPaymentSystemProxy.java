package TradingSystem.server.Domain.ExternSystems.Proxy;

public class ExternPaymentSystemProxy {
    public boolean payment(double total_price, String paymentInfo) {
        return true;
    }
    public boolean can_pay(double total_price, String paymentInfo) {return true;}
    public boolean connect(){return true;}
}