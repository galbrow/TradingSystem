package Domain.StoreModule;

import Domain.Utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Date;
import java.util.List;


public class Store {
    // -- fields
    private int store_id;
    private int founder_id;
    private HashMap<Integer, Permission> owner_ids;
    private HashMap<Integer, Permission> manager_ids;
    private String name;
    private Date foundation_date;
    private HashMap<Integer, Product> products;
    private int product_ids;
    private boolean active;
    private PurchasePolicy purchasePolicy;
    private DiscountPolicy discountPolicy;
    private HashMap<Integer, Question> users_questions; // question_id x question
    private HashMap<Integer, Purchase> purchases_history;

    // -- constructor

    public Store(int store_id, int founder_id, String name) {
        this.store_id = store_id;
        this.founder_id = founder_id;
        this.name = name;
        this.product_ids = 1;
    }

    // -- methods
    public boolean close_store_temporarily(int user_id) throws IllegalAccessException {
        this.check_permission(user_id, StorePermission.close_store_temporarily);
        if (!this.is_active())
            return false;
        this.active = false;
        // TODO: 22/04/2022 : send message to all of the managers & owners.
        return true;
    }
    public boolean open_close_store(int user_id) throws IllegalAccessException {
        this.check_permission(user_id, StorePermission.open_close_store);
        if (this.is_active())
            return false;
        this.active = true;
        // TODO: 22/04/2022 : send message to all of the managers & owners.
        return true;
    }
    public StoreManagersInfo view_store_management_information(int user_id) throws IllegalAccessException {
        this.check_permission(user_id, StorePermission.view_permissions);
        return new StoreManagersInfo(this.manager_ids);
    }
    public void set_permissions(int user_id, int manager_id, LinkedList<StorePermission> permissions) {
        // check that the manager appointed by the user
        if (this.get_appointer(manager_id) != user_id)
            throw new IllegalArgumentException("The manager is not appointed by user");
        // check that the user is not trying to change his permissions
        if (manager_id == user_id)
            throw new IllegalArgumentException("User cant change himself permissions");

        Permission manager_permission = this.owner_ids.get(manager_id);
        manager_permission.set_permissions(permissions);

    }
    public boolean is_active() {
        return this.active;
    }
    public int get_appointer(int manager_id) {
        return this.manager_ids.get(manager_id).getAppointer_id();
    }


    public HashMap<Integer, Question> view_store_questions(int user_id) throws IllegalAccessException {
        this.check_permission(user_id, StorePermission.view_users_questions);
        return this.users_questions;
    }

    public void answer_question(int store_id, int user_id, int question_id, String answer) throws IllegalAccessException {
        this.check_permission(user_id, StorePermission.view_users_questions);
        Question question = this.users_questions.get(question_id);
        question.setAnswer(answer);
    }

    public HashMap<Integer, Purchase> view_store_purchases_history(int user_id) throws IllegalAccessException {
        this.check_permission(user_id, StorePermission.view_purchases_history);
        return this.purchases_history;
    }

    public boolean close_store_permanently(int user_id) {
        if (!this.is_active())
            return false;
        this.active = false;
        // TODO: 22/04/2022 : send message to all of the managers & owners.
        this.founder_id = -1;
        this.manager_ids = null;
        return true;
    }

    public void check_permission(int user_id, StorePermission permission) throws IllegalAccessException {
        // not just managers - FIX
        if(!(this.manager_ids.get(user_id).has_permission(permission)))
            throw new IllegalAccessException("User has no permissions!");
    }






    public String get_information() {
        String founder_name = "----------------------";
        StringBuilder info = new StringBuilder();
        info.append("Store info: "+this.name+"\n");
        info.append("\tStore founder: "+ founder_name +"\n");
        info.append("\tStore owners: ");
        for (Integer id : owner_ids.keySet())
        {
            String name = "";
            info.append(name+", ");
        }
        info.append("\n");
        info.append("\tStore managers: ");
        for (Integer id : manager_ids.keySet())
        {
            String name = "";
            info.append(name+", ");
        }
        info.append("\n");
        info.append("\tfoundation date: "+ Utils.date_to_string(this.foundation_date)+"\n");


        //products


        String is_active;
        if (active)
            is_active="Yes";
        else
            is_active="No";

        info.append("\tactive: "+ is_active+"\n");
        info.append("\tpurchase policy: "+ this.purchasePolicy+"\n");
        info.append("\tdiscount policy: "+ this.discountPolicy+"\n");

        return info.toString();

    }

    public boolean is_product_exist(int product_id) {
        return products.containsKey(product_id);
    }

    public String get_product_information(int product_id) {
        //already check that product exists in the store
        return this.products.get(product_id).toString();
    }
    public Product find_product_by(String identify) {
        for (Product p:products.values()) {
            if (p.getName().equals(identify))
            {
                return p;
            }
        }
        return  null;
    }

    public void add_product(Product product) {
        products.put(product_ids, product);
        this.product_ids++;
    }









    //------------------------------------------------------Getters--------------------------------------------------------------------

    public Date getFoundation_date() {
        return foundation_date;
    }

    public DiscountPolicy getDiscount_policy() {
        return discountPolicy;
    }

    public HashMap<Integer, Permission> getManager_ids() {
        return manager_ids;
    }

    public HashMap<Integer, Permission> getOwner_ids() {
        return owner_ids;
    }

    public HashMap<Integer, Product> getProducts() {
        return products;
    }

    public HashMap<Integer, Purchase> getPurchase_history() {
        return purchases_history;
    }

    public int getFounder_id() {
        return founder_id;
    }

    public int getStore_id() {
        return store_id;
    }

    public PurchasePolicy getPurchase_policy() {
        return purchasePolicy;
    }

    public String getName() {
        return name;
    }



    //------------------------------------------------------Setters--------------------------------------------------------------------

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public void setFounder_id(int founder_id) {
        this.founder_id = founder_id;
    }

    public void setOwner_ids(HashMap<Integer, Permission> owner_ids) {
        this.owner_ids = owner_ids;
    }

    public void setManager_ids(HashMap<Integer, Permission> manager_ids) {
        this.manager_ids = manager_ids;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFoundation_date(Date foundation_date) {
        this.foundation_date = foundation_date;
    }

    public void setProducts(HashMap<Integer, Product> products) {
        this.products = products;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPurchasePolicy(PurchasePolicy purchasePolicy) {
        this.purchasePolicy = purchasePolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public void setUsers_questions(HashMap<Integer, Question> users_questions) {
        this.users_questions = users_questions;
    }

    public void setPurchases_history(HashMap<Integer, Purchase> purchases_history) {
        this.purchases_history = purchases_history;
    }


    public void delete_product(int product_id) {
        //already checks that product exist
        products.remove(product_id);
        //manage product ids after product deletion
    }

//    public void edit_product(int product_id) {
//        Product to_edit = products.get(product_id);
//
//    }

    public boolean edit_product_name(int product_id, String name) {
        Product to_edit = products.get(product_id);
        to_edit.setName(name);
        return true;
    }

    public boolean edit_product_price(int product_id, double price) {
        Product to_edit = products.get(product_id);
        to_edit.setPrice(price);
        return true;
    }

    public boolean edit_product_category(int product_id, String category) {
        Product to_edit = products.get(product_id);
        to_edit.setCategory(category);
        return true;
    }

    public boolean edit_product_key_words(int product_id, List<String> key_words) {
        Product to_edit = products.get(product_id);
        to_edit.setKey_words(key_words);
        return true;
    }
}


