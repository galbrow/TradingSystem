package TradingSystem.server.Domain.StoreModule.Store;

import TradingSystem.server.Domain.StoreModule.Appointment;
import TradingSystem.server.Domain.StoreModule.Product.Product;
import TradingSystem.server.Domain.StoreModule.Purchase.StorePurchaseHistory;
import TradingSystem.server.Domain.UserModule.AssignUser;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreInformation {
    private int store_id;
    private String founder_email;
    private String name;
    public String foundation_date;
    private Map<Product, Integer> inventory; // product & quantity
    private StoreReview storeReview;


    // ------------------------------ constructors ------------------------------
    public StoreInformation(Store store) {
        String founder_email1 = "";
        try{ founder_email1 = store.getFounder().get_user_email();}
        catch (Exception e){}
        this.store_id = store.getStore_id();
        this.founder_email = founder_email1;
        this.name = store.getName();
        this.foundation_date = store.getFoundation_date();
        this.storeReview = store.getStoreReview();
        this.inventory = store.getInventory();
    }

    public StoreInformation() {
    }

    // ------------------------------ setters  ------------------------------

    public void setFounder_email(String founder_email) {
        this.founder_email = founder_email;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInventory(Map<Product, Integer> inventory) {
        this.inventory = inventory;
    }

    public void setStoreReview(StoreReview storeReview) {
        this.storeReview = storeReview;
    }

    public void setFoundation_date(String foundation_date) {
        this.foundation_date = foundation_date;
    }


    //    public String toString() {
//        String founder_name = "----------------------";
//        StringBuilder info = new StringBuilder();
//        info.append("Store info: "+this.name+"\n");
//        info.append("\tStore founder: "+ founder_name +"\n");
//        info.append("\tStore owners: ");
//        for (String email : stuff_emails_and_appointments.keySet())
//        {
//            String name = "";
//            info.append(name+", ");
//        }
//        info.append("\n");
//        info.append("\tStore managers: ");
//        for (String email : stuff_emails_and_appointments.keySet())
//        {
//            String name = "";
//            info.append(name+", ");
//        }
//        info.append("\n");
//        info.append("\tfoundation date: "+ Utils.DateToString(this.foundation_date)+"\n");
//
//
//        //products
//
//
//        String is_active;
//        if (active)
//            is_active="Yes";
//        else
//            is_active="No";
//
//        info.append("\tactive: "+ is_active+"\n");
//        info.append("\tpurchase policy: "+ this.purchasePolicy+"\n");
//        info.append("\tdiscount policy: "+ this.discountPolicy+"\n");
//
//        return info.toString();
//
//    }

    // ------------------------------ getters ------------------------------
    public String getFounder_email() {
        return founder_email;
    }

    public String getFoundation_date() {
        return foundation_date;
    }

    public int getStore_id() {
        return store_id;
    }

    public String getName() {
        return name;
    }

    public Map<Product, Integer> getInventory() {
        return inventory;
    }

    public StoreReview getStoreReview() {
        return storeReview;
    }
}
