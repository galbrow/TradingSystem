package Domain.UserModule;

public abstract class AssignState{
    public boolean login(String pw) throws Exception {throw new Exception("Assign user cannot log in");}

    public void addPurchase(UserPurchase purchase) { }

    public void check_if_user_buy_from_this_store(int store_id) throws Exception {throw new Exception("Guest Hasn't made any purchases yet.");}

    public void check_if_user_buy_this_product(int storeID, int productID) throws Exception {throw new Exception("Guest Hasn't made any purchases yet.");}

    public UserHistory view_user_purchase_history() throws Exception {throw new Exception("Guest Hasn't made any purchases yet.");}

    public String get_user_name() throws Exception {throw new Exception("Guest doesnt have name.");}

    public String get_user_last_name() throws Exception {throw new Exception("Guest doesnt have last name.");}

    public String get_user_email() throws Exception {throw new Exception("Guest doesnt have last name.");}

    public void check_admin_permission() throws Exception { throw new Exception("only admin have permissions for this operation.");}

    public void unregister(String password) throws Exception { throw new Exception("guest cant unregister from the system");}

    public void edit_name(String pw, String new_name) throws Exception { throw new Exception("guest cant change is name");}

    public void edit_password(String pw, String password) throws Exception { throw new Exception("guest cant change is password");}

    public void edit_last_name(String pw, String new_last_name) throws Exception { throw new Exception("guest cant change his last name");}
}
