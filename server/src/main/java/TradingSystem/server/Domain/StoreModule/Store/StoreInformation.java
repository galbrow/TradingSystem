package TradingSystem.server.Domain.StoreModule.Store;

public class StoreInformation {

    private final String founder_email;
    private final String store_name;
    String foundation_date;
    private StoreReviewInformation storeReviewInformation;

    public StoreInformation(Store store) {
        String founder_email1 = "";
        try{ founder_email1 = store.getFounder().get_user_email();}
        catch (Exception e){}
        this.founder_email = founder_email1;
        this.store_name = store.getName();
        this.foundation_date = store.getFoundation_date();
        this.storeReviewInformation = new StoreReviewInformation(store.getStoreReview());
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


    public String getFounder_email() {
        return founder_email;
    }

    public String getStore_name() {
        return store_name;
    }

    public String getFoundation_date() {
        return foundation_date;
    }

    public StoreReviewInformation getStoreReviewInformation() {
        return storeReviewInformation;
    }
}
