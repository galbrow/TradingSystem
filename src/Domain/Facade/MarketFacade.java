package Domain.Facade;

import java.util.LinkedList;
import java.util.List;

import Domain.StoreModule.Purchase.Purchase;
import Domain.StoreModule.Policy.DiscountPolicy;
import Domain.StoreModule.Purchase.UserPurchase;
import Domain.StoreModule.Purchase.UserPurchaseHistory;
import Domain.Utils.ErrorLogger;
import Domain.Utils.Response;
import Domain.StoreModule.Product.Product;
import Domain.StoreModule.Product.ProductInformation;
import Domain.StoreModule.Store.Store;
import Domain.StoreModule.Store.StoreInformation;
import Domain.ExternSystems.PaymentAdapter;
import Domain.ExternSystems.SupplyAdapter;
import Domain.Statistics.Statistic;
import Domain.UserModule.*;
import Domain.StoreModule.*;
import Domain.Utils.SystemLogger;
import com.google.gson.Gson;

import java.util.Map;

// TODO: when we leave the system - should call logout()

public class MarketFacade implements iFacade {
    private UserController user_controller;
    private StoreController store_controller;
    private int loggedUser;                  //id
    private boolean isGuest;                 //represents the state
    private PaymentAdapter payment_adapter;
    private SupplyAdapter supply_adapter;
    private ErrorLogger error_logger;
    private SystemLogger system_logger;

    public MarketFacade(PaymentAdapter payment_adapter, SupplyAdapter supply_adapter) {
        this.isGuest = true;
        this.user_controller = UserController.getInstance();
        this.store_controller = StoreController.get_instance();
        //Requirement 2.1.1 - guest log in
        this.loggedUser = user_controller.guest_login();
        this.payment_adapter = payment_adapter;
        this.supply_adapter = supply_adapter;
        this.error_logger = ErrorLogger.getInstance();
        this.system_logger = SystemLogger.getInstance();
    }

    private String toJson(Response r) {
        return new Gson().toJson(r);
    }



    /**
     * Requirement 2.1.2 & 2.3.1
     * logout - logout assign user
     * @return a string with informative of success/failure to client
     */
    @Override
    // @TODO : seperate assign user & guest logout
    public String logout() {
        Response response = null;
        if (isGuest) {
            response = new Response(new Exception("guest cannot logout from the system"));
            error_logger.add_log(new Exception("Guests cannot logout, action failed."));
        } else {
            system_logger.add_log("User logged out from the system.");
            response = new Response(null,"Logout Successfully");
            user_controller.logout(loggedUser);
            this.isGuest = true;
        }
        return toJson(response);
    }

    /**
     * Requirement 2.1.3
     * @param Email of new user
     * @param pw requested encrypted password
     * @param name the first name of the new user
     * @param lastName the last name of the new user
     * @return a string with informative of success/failure to client
     */

    @Override
    public String register(String Email, String pw, String name, String lastName) {
        Response response = null;
        try {
            if (!isGuest) {
                throw new Exception("Assigned User cannot register");
            }
            user_controller.register(loggedUser, Email, pw, name, lastName);
            this.isGuest = false;
            response = new Response<>(null, "Registration done successfully");
            system_logger.add_log(name + " " + lastName + " has registered to the system");
        } catch (Exception e) {
            response = new Response(e);
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.1.4
     * @param Email of the user
     * @param password encrypted
     * @return a string with informative of success/failure to client
     */
    @Override
    public String login(String Email, String password) {
        Response response = null;
        try {
            if(!isGuest){
                Exception e = new Exception("You are already logged in!");
                error_logger.add_log(e);
                response = new Response(e);
            }
            else{
                user_controller.login(loggedUser, Email, password);
                String user_name = this.user_controller.get_user_name(loggedUser) + " " + this.user_controller.get_user_last_name(loggedUser);
                isGuest = false;
                response = new Response<>(null, "Hey +" + user_name + ", Welcome to the trading system market!");
                system_logger.add_log("User " + Email + " logged-in");
            }
        } catch (Exception e) {
            response = new Response(new Exception("Incorrect email or password, please try again."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.2.1 - Store
     * @param store_id represents the store id
     * @return store information or action failure reason
     */
    @Override
    public String find_store_information(int store_id) {
        Response<StoreInformation> response = null;
        try {
            Store store = this.store_controller.find_store_information(store_id);
            StoreInformation storeInformation = new StoreInformation(store);
            response = new Response<>(storeInformation, "Store information received successfully");
            system_logger.add_log("Store (" + store_id + ") information found successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to find store information."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.2.1 - Product
     * @param product_id of the asked product
     * @param store_id who has the product
     * @return product information or action failure reason
     */
    @Override
    public String find_product_information(int product_id, int store_id) {
        Response<ProductInformation> response = null;
        try {
            Product product = this.store_controller.find_product_information(product_id, store_id);
            ProductInformation productInformation = new ProductInformation(product);
            response = new Response<>(productInformation, "Product information received successfully");
            system_logger.add_log("Product (" + product_id + " from store " + store_id + ") information found successfully.");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to find product information."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.2.2 - Name
     * @param product_name the name of the desired product
     * @return List of Products with the specific name or action failure reason
     */
    @Override
    public String find_products_by_name(String product_name) {
        Response<List<Product>> response = null;
        try {
            List<Product> products = this.store_controller.find_products_by_name(product_name);
            response = new Response<>(products, "Product list received successfully");
            system_logger.add_log("List of product " + product_name + " found successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to find product " + product_name + "."));
            error_logger.add_log(e);


        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.2.2 - Category
     * @param category the category of the desired product
     * @return List of Products with the specific category or action failure reason
     */
    @Override
    public String find_products_by_category(String category) {
        Response<List<Product>> response = null;
        try {
            List<Product> products = this.store_controller.find_products_by_category(category);
            response = new Response<>(products, "Products received successfully");
            system_logger.add_log("List of products from category " + category + " found successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to find products from category " + category + "."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.2.2 - Key_words
     * @param key_words the keywords of the desired product
     * @return List of Products with the specific key_word or action failure reason
     */
    @Override
    public String find_products_by_keywords(String key_words) {
        Response<List<Product>> response = null;
        try {
            List<Product> products = this.store_controller.find_products_by_key_words(key_words);
            response = new Response<>(products, "Products received successfully");
            system_logger.add_log("List of products with key words- " + key_words + " found successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to find products with key words- " + key_words + "."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.2.3 - Add
     * @param storeID who stores the product
     * @param productID we want to add to the cart
     * @param quantity of the product
     * @return success/failure message
     */
    @Override
    public String add_product_to_cart(int storeID, int productID, int quantity) {
        Response response = null;
        try {
            Basket storeBasket = user_controller.getBasketByStoreID(loggedUser, storeID);
            Product p = store_controller.checkAvailablityAndGet(storeID, productID, quantity);
            storeBasket.addProduct(p, quantity);
            user_controller.addBasket(loggedUser, storeID, storeBasket);
            response = new Response("", "product " + productID + " added to cart");
            system_logger.add_log("User added to cart " + quantity + " of product- " + productID + " from store- " + storeID);

        } catch (Exception e) {
            response = new Response(new Exception("Failed to add product to cart."));
            error_logger.add_log(e);

        }
        return toJson(response);
    }

    

    /**
     * Requirement 2.2.4
     * @param storeID who stores the product
     * @param productID we want to change the quantity
     * @param quantity new amount
     * @return success/failure message
     */
    @Override
    public String edit_product_quantity_in_cart(int storeID, int productID, int quantity) {
        Response response = null;
        try {
            Basket storeBasket = user_controller.getBasketByStoreID(loggedUser, storeID);
            Product p = store_controller.checkAvailablityAndGet(storeID, productID, quantity);
            storeBasket.changeQuantity(p, quantity);
            response = new Response("", "product " + productID + " quantity has changed to " + quantity);
            system_logger.add_log("User quantity of product- " + productID + " from store- " + storeID + " in cart to " + quantity);

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change product quantity."));
            error_logger.add_log(e);

        }
        return toJson(response);
    }
    

    /**
     * Requirement 2.2.3 - Remove
     * @param storeID who stores the product
     * @param productID we want to remove from the cart
     * @return success/failure message
     */
    @Override
    public String remove_product_from_cart(int storeID, int productID) {
        Response response = null;
        try {
            Product p = store_controller.getProduct_by_product_id(storeID, productID);
            Basket storeBasket = user_controller.getBasketByStoreID(loggedUser, storeID);
            storeBasket.removeProduct(p);
            user_controller.removeBasketIfNeeded(loggedUser, storeID, storeBasket);
            response = new Response("", "product " + productID + " has removed from cart");
            system_logger.add_log("User removed from cart product- " + productID + " from store- " + storeID);

        } catch (Exception e) {
            response = new Response(new Exception("Failed to remove product from cart."));
            error_logger.add_log(e);

        }
        return toJson(response);
    }


    /**
     * Requirement 2.2.4
     * @return the user cart information
     */
    @Override
    public String view_user_cart() {
        Map<Integer, Basket> cart = user_controller.getBaskets(loggedUser);
        Response<Map<Integer, Basket>> response = new Response<>(cart, "successfully received user's cart");
        system_logger.add_log("User viewed his cart successfully");
        return toJson(response);
    }


    /**
     * Requirement 2.2.5
     * @param paymentInfo info of payment
     * @param SupplyInfo  info of supply
     * @return success/failure message
     */
    @Override
    public String buy_cart(String paymentInfo, String SupplyInfo) {
        Response<UserPurchase> response = null;
        try {
            // get information about the payment & supply
            Cart cart = this.user_controller.getCart(this.loggedUser);
            double cart_total_price = this.store_controller.check_cart_available_products_and_calc_price(cart);
            this.payment_adapter.payment(cart_total_price, paymentInfo);
            this.supply_adapter.supply(SupplyInfo);
            // acquire lock of : edit/delete product, both close_store, discount & purchase policy, delete user from system.
            Map<Integer, Purchase> store_id_purchase = this.store_controller.update_stores_inventory(cart);
            UserPurchase userPurchase = this.user_controller.buyCart(this.loggedUser, store_id_purchase, cart_total_price);
            response = new Response(userPurchase, "Purchase done successfully");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to purchase cart."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }
    /**
     * Requirement 2.3.2
     * @param store_name name of the store to be opened
     * @precondition : GUI check store name is valid
     * @return success/failure message
     */
    @Override
    public String open_store(String store_name) {
        Response response = null;
        try {
            String email = this.user_controller.get_email(this.loggedUser);
            int store_id = this.store_controller.open_store(email, store_name);
            response = new Response<>(null, "Store opened successfully");
            system_logger.add_log("Store " + store_name + " with id = " + store_id + "opened successfully");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to open store."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.3
     * @param product_id - the product to review
     * @param store_id   - the store who has this product
     * @param review     - user message about the product
     * @return success/failure message
     */
    @Override
    public String add_product_review(int product_id, int store_id, String review) {
        Response response = null;
        try {
            this.user_controller.check_if_user_buy_this_product(this.loggedUser, product_id, store_id);
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.add_review(user_email, product_id, store_id, review);
            response = new Response<>(null, "Review added successfully");
            system_logger.add_log("New review added for product (" + product_id + ") form store (" + store_id + ") and user:"+user_email);
        } catch (Exception e) {
            response = new Response(new Exception("Failed to add review."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.3.4 - Product
     * @param product_id of the product to rate
     * @param store_id who stores the product
     * @param rate number in range 1-5
     * @return success/failure message
     */
    @Override
    public String rate_product(int product_id, int store_id, int rate) {
        Response response = null;
        try {
            this.user_controller.check_if_user_buy_this_product(this.loggedUser, product_id, store_id);
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.rate_product(user_email, product_id, store_id, rate);
            response = new Response<>(null, "Rating added successfully to the product");
            system_logger.add_log("New rating (" + rate + ") added for product (" + product_id + ") form store (" + store_id + ")");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to add rating."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.4 - Store
     * @param store_id id of the store
     * @param rate     the rating enter by the user
     * @return success/failure message
     */

    @Override
    public String rate_store(int store_id, int rate) {
        Response response = null;
        try {
            this.user_controller.check_if_user_buy_from_this_store(this.loggedUser, store_id);
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.rate_store(user_email, store_id, rate);
            response = new Response<>(null, "Rating added successfully to the store");
            system_logger.add_log("New rating (" + rate + ") added for store (" + store_id + ") from user : "+user_email);

        } catch (Exception e) {
            response = new Response(new Exception("Failed to add rating."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.3.5
     * @param store_id - the question is for a specific store
     * @param question - member question
     * @return success/failure message
     */
    @Override
    public String send_question_to_store(int store_id, String question) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.user_controller.check_if_user_buy_from_this_store(this.loggedUser, store_id);
            this.store_controller.add_question(user_email, store_id, question);
            response = new Response<>(null, "Question send to the store successfully");
            system_logger.add_log("New question sent to store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to send question to store."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }



    /**
     * Requirement 2.3.6
     * @param question to admin
     * @return success/failure message
     */
    @Override
    public String send_question_to_admin(String question) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.user_controller.send_question_to_admin(user_email, question);
            response = new Response<>(null, "Question send to the admin successfully");
            system_logger.add_log("New question sent to admin");

        } catch (Exception e) {
            response = new Response(e);
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.7
     * @return user purchase history or failure message
     */
    @Override
    public String view_user_purchase_history() {
        Response<UserPurchaseHistory> response = null;
        try {
            UserPurchaseHistory userPurchaseHistory = user_controller.view_user_purchase_history(loggedUser);
            response = new Response<>(userPurchaseHistory, "successfully received user's product history");
            system_logger.add_log("User viewed his purchase history successfully");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to view purchase history."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.3.8 - View
     * @return user email or failure message
     */
    @Override
    public String get_user_email() {
        Response response = null;
        try {
            String email = user_controller.get_email(loggedUser);
            response = new Response<>(email, "successfully received user's email");
            system_logger.add_log("Got user's email successfully");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to get user's email."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - View
     * @return user name or failure message
     */
    @Override
    public String get_user_name() {
        Response response = null;
        try {
            String name = user_controller.get_user_name(loggedUser);
            response = new Response<>(name, "successfully received user's name");
            system_logger.add_log("Got user's name successfully");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to get user's name."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - View
     * @return user last name or failure message
     */
    @Override
    public String get_user_last_name() {
        Response response = null;
        try {
            String last_name = this.user_controller.get_user_last_name(loggedUser);
            response = new Response<>(last_name, "Last name received successfully");
            system_logger.add_log("Got user's last name successfully");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to get user's last name."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.3.8 - edit
     * @param old_password
     * @param password
     * @return success/failure message
     */
    @Override
    public String edit_password(String old_password, String password) {
        Response response = null;
        try {
            String email = user_controller.edit_password(loggedUser, old_password, password);
            response = new Response(password, email + " password has been changed successfully");
            system_logger.add_log("User's (" + email + ")  password has been changed successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to change password."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - edit
     * @param pw password
     * @param new_name new first name
     * @return success/failure message
     */
    @Override
    public String edit_name(String pw, String new_name) {
        Response response = null;
        try {
            String email = user_controller.edit_name(loggedUser, pw, new_name);
            response = new Response(new_name, email + " name changed to " + new_name);
            system_logger.add_log("User's (" + email + ") name has been successfully changed to " + new_name + ".");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change name."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - edit
     * @param pw password
     * @param new_last_name new last name
     * @return success/failure message
     */
    @Override
    public String edit_last_name(String pw, String new_last_name) {
        Response response = null;
        try {
            String email = user_controller.edit_last_name(loggedUser, pw, new_last_name);
            response = new Response(new_last_name, email + " last name changed to " + new_last_name);
            system_logger.add_log("User's (" + email + ") last name has been successfully changed to " + new_last_name + ".");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change user's last name."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - unregister
     * @param password
     * @return success/failure message
     */
    @Override
    public String unregister(String password) {
        Response response = null;
        try {
            // TODO: version 2
            // close stores permanently
            String email = user_controller.unregister(loggedUser, password);
            // remove user from all owners and managers
            // remove all users complains & questions
            response = new Response(email, email + " unregistered successfully");
            system_logger.add_log("User (" + email + ") has been successfully unregistered from the system.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to unregister user."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - edit
     * @param pw password
     * @param new_name new first name
     * @param answer for the security question
     * @return success/failure message
     */



    @Override
    public String edit_name_premium(String pw, String new_name, String answer) {
        Response response = null;
        try {
            String email = user_controller.edit_name_premium(loggedUser, pw, new_name,answer);
            response = new Response(new_name, email + " name changed to " + new_name);
            system_logger.add_log("User's (" + email + ") name has been successfully changed to " + new_name + ".");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change name."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - edit
     * @param pw password
     * @param new_last_name new last name
     * @param answer for the security question
     * @return success/failure message
     */

    @Override
    public String edit_last_name_premium(String pw, String new_last_name, String answer) {
        Response response = null;
        try {
            String email = user_controller.edit_last_name_premium(loggedUser, pw, new_last_name,answer);
            response = new Response(new_last_name, email + " last name changed to " + new_last_name);
            system_logger.add_log("User's (" + email + ") last name has been successfully changed to " + new_last_name + ".");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change last name."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.8 - edit
     * @param old_password  ,
     * @param new_password .
     * @param answer for the security question
     * @return success/failure message
     */
    @Override
    public String edit_password_premium(String old_password, String new_password, String answer) {
        Response response = null;
        try {
            String email = user_controller.edit_passsword_premium(loggedUser, old_password, new_password,answer);
            response = new Response(null, email + " password changed");
            system_logger.add_log("User's (" + email + ") password has been successfully changed.");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change password."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.3.9
     * @return the security question of the user or failure message
     */
    @Override
    public String get_user_security_question() {
        Response response = null;
        try {
            String question = user_controller.get_user_security_question(loggedUser);
            response = new Response<>(question, "successfully received security question");
            system_logger.add_log("Got user's security question successfully");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to get user's security question."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.3.9
     * @param password for secure the action
     * @param question for improve security
     * @param answer of the security question
     * @return success or failure message
     */
    @Override
    public String improve_security(String password, String question, String answer) {
        Response response = null;
        try {
            String email = user_controller.improve_security(loggedUser,password,question,answer);
            response = new Response(null, email + " improved security");
            system_logger.add_log("User's (" + email + ") security has been successfully improved.");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to improve security."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.4.1 - Add
     * @param store_id adding product to a store
     * @param quantity for store's inventory
     * @param name product name
     * @param price for one unit
     * @param category for the product
     * @param key_words for searching product
     * @return success/failure message
     */
    @Override
    public String add_product_to_store(int store_id, int quantity,
                                       String name, double price, String category, List<String> key_words) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            store_controller.add_product_to_store(user_email, store_id, quantity, name, price, category, key_words);
            response = new Response<>(null, "Product added successfully");
            system_logger.add_log("New product (" + name + ") added to store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to add product to store."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.1 - Delete
     * @param product_id of the product
     * @param store_id .
     * @return success/failure message
     */
    @Override
    public String delete_product_from_store(int product_id, int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.delete_product_from_store(user_email, product_id, store_id);
            response = new Response<>(null, "Product deleted successfully");
            system_logger.add_log("Product (" + product_id + ") was deleted from store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to delete product from store."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.4.1 - Edit name
     * @param product_id of the product
     * @param store_id who stores the product
     * @param name we want to set for the product
     * @return success/failure message
     */
    @Override
    public String edit_product_name(int product_id, int store_id, String name) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.edit_product_name(user_email, product_id, store_id, name);
            response = new Response<>(null, "Product name edit successfully");
            system_logger.add_log("Product (" + product_id + ") name has been changed to " + name + " in store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change product name."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.1 - Edit Price
     * @param product_id of the item
     * @param store_id who stores the product
     * @param price to set for the product
     * @return success/failure message
     */
    
    @Override
    public String edit_product_price(int product_id, int store_id, double price) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.edit_product_price(user_email, product_id, store_id, price);
            response = new Response<>(null, "Product price edit successfully");
            system_logger.add_log("Product (" + product_id + ") price has been changed to " + price + " in store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change product price."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.1 - Edit category
     * @param product_id of the item
     * @param store_id who stores the product
     * @param category
     * @return success/failure message
     */
    @Override
    public String edit_product_category(int product_id, int store_id, String category) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.edit_product_category(user_email, product_id, store_id, category);
            response = new Response<>(null, "Product category edit successfully");
            system_logger.add_log("Product (" + product_id + ") category has been changed to " + category + " in store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change product category."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /** 
     * Requirement 2.4.1 - Edit key words
     * @param product_id of the item
     * @param store_id who stores the product
     * @param key_words
     * @return success/failure message
     */

    @Override
    public String edit_product_key_words(int product_id, int store_id, List<String> key_words) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.edit_product_key_words(user_email, product_id, store_id, key_words);
            response = new Response<>(null, "Product key_words edit successfully");
            system_logger.add_log("Product's (" + product_id + ") key words have been changed to " + key_words + " in store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to edit product key words."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.2 - purchase policy
     * @param store_id   id for the store
     * @param policy     the rules to set
     * @return success/failure message            
     */
    @Override
    public String set_store_purchase_policy(int store_id, PurchasePolicy policy) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            store_controller.set_store_purchase_policy(store_id, user_email, policy);
            response = new Response<>(null, "Store purchase rules set successfully");
        } catch (Exception e) {
            response = new Response(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.2 - discount policy
     * @param store_id   id for the store
     * @param policy     the rules to set
     * @return success/failure message  
     */
    @Override
    public String set_store_discount_policy(int store_id, DiscountPolicy policy) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            store_controller.set_store_discount_policy(store_id, user_email, policy);
            response = new Response<>(null, "Store discount rules set successfully");
        } catch (Exception e) {
            response = new Response(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.3
     * @param store_id
     * @param rule
     * @return success/failure message  
     */
    @Override
    public String set_store_purchase_rules(int store_id, Rule rule) {
        Response response = null;
        try {
            store_controller.set_store_purchase_rules(store_id, rule);
            response = new Response<>(null, "Store purchase rules set successfully");
            system_logger.add_log("Store's (" + store_id + ") purchase rules have been set");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to change store purchase rules."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }


    

    /**
     * Requirement 2.4.4
     * @param user_email_to_appoint - the user to appoint
     * @param store_id              - the relevant store for the appointment
     * @return success/failure message 
     */
    @Override
    public String add_owner(String user_email_to_appoint, int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.add_owner(user_email, user_email_to_appoint, store_id);
            response = new Response<>(null, "Owner added successfully");
            system_logger.add_log("User- " + user_email_to_appoint + " has been appointed by user- " + user_email + " to store (" + store_id + ") owner");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to add new owner."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.4.5
     * @param user_email_to_delete_appointment the email of the user to be remove his appointment
     * @param store_id                         the id of the store to removed the appoitment from
     * @return success/failure message 
     */
    
    @Override
    public String delete_owner(String user_email_to_delete_appointment, int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.remove_owner(user_email, user_email_to_delete_appointment, store_id);
            response = new Response<>(null, "Owner removed successfully");
            system_logger.add_log("User- " + user_email_to_delete_appointment + " has been unappointed by user- " + user_email + " from store (" + store_id + ") owner");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to remove owner."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.4.6
     * @param user_email_to_appoint - the user to appoint
     * @param store_id              - the relevant store for the appointment
     * @return success/failure message 
     */

    @Override
    public String add_manager(String user_email_to_appoint, int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.add_manager(user_email, user_email_to_appoint, store_id);
            response = new Response<>(null, "Manager added successfully");
            system_logger.add_log("User- " + user_email_to_appoint + " has been appointed by user- " + user_email + " to store (" + store_id + ") manager");


        } catch (Exception e) {
            response = new Response(new Exception("Failed to add new manager."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }
    

    /**
     * Requirement 2.4.7
     * @param manager_email - the user we want to change his permission
     * @param store_id - the store for the permissions
     * @param permissions list of the new permissions - just the relevant
     * @return success/failure message 
     */
    @Override
    public String edit_manager_permissions(String manager_email, int store_id, LinkedList<StorePermission> permissions) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.edit_manager_specific_permissions(user_email, manager_email, store_id, permissions);
            response = new Response<>(null, "Manager permission edit successfully");
            system_logger.add_log("Manager's (" + manager_email + ") permissions have been updated by user - " + user_email + " in store (" + store_id + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to edit manager permissions."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.4.8
     * @param user_email_to_delete_appointment email of user manager to remove
     * @param store_id                         id of the store
     * @return success/failure message 
     */

    @Override
    public String delete_manager(String user_email_to_delete_appointment, int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.remove_manager(user_email, user_email_to_delete_appointment, store_id);
            response = new Response<>(null, "Manager removed successfully");
            system_logger.add_log("User- " + user_email_to_delete_appointment + " has been unappointed by user- " + user_email + " from store (" + store_id + ") manager");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to remove manager."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.9
     * @param store_id of the store
     * @precondition the store is open
     * @return success/failure message
     */
    @Override
    public String close_store_temporarily(int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.close_store_temporarily(user_email, store_id);
            response = new Response<>(null, "Store closed temporarily");
            system_logger.add_log("Store (" + store_id + ") has been closed temporarily by user (" + user_email + ")");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to close store temporarily."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.10
     * @param store_id - the store we want to re-open
     * @precondition the store is close
     * @return success/failure message
     */
    @Override
    public String open_close_store(int store_id) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.open_close_store(user_email, store_id);
            response = new Response<>(null, "Store re-open successfully");
            system_logger.add_log("Store (" + store_id + ") has been re-opened by user (" + user_email + ")");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to re-open store."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.11
     * @param store_id - the store we want to get information about
     * @return store management information or failure message
     */
    @Override
    public String view_store_management_information(int store_id) {
        Response<String> response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            String answer = this.store_controller.view_store_management_information(user_email, store_id);
            response = new Response<>(answer, "Store information received successfully");
            system_logger.add_log("Store's (" + store_id + ") management information has been viewed by user (" + user_email + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to view store management information."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.4.12
     * @param store_id - the store we want to get information about
     * @return all the questions for a store or failure message
     */
    @Override
    public String manager_view_store_questions(int store_id) {
        Response<List<String>> response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            List<String> store_questions = this.store_controller.view_store_questions(user_email, store_id);
            response = new Response<>(store_questions, "Store questions received successfully");
            system_logger.add_log("Store's (" + store_id + ") questions has been viewed by user (" + user_email + ")");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to view store questions."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.12
     * @param store_id    - each question is related to store
     * @param question_id - each time the manager answer one question only
     * @param answer      - manager answer
     * @return success/failure message
     */
    @Override
    public String manager_answer_question(int store_id, int question_id, String answer) {
        Response response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            this.store_controller.answer_question(user_email, store_id, question_id, answer);
            response = new Response("", "manager answer the question successfully");
            system_logger.add_log("Store (" + store_id + ") question (" + question_id + ") has been answered by user (" + user_email + ")");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to answer question."));
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.4.13
     * @param store_id ,
     * @return store purchase history or failure message
     */

    @Override
    public String view_store_purchases_history(int store_id) {
        Response<String> response = null;
        try {
            String user_email = this.user_controller.get_email(this.loggedUser);
            String answer = this.store_controller.view_store_purchases_history(user_email, store_id);
            response = new Response<>(answer, "Store purchases history received successfully");
            system_logger.add_log("User received (" + user_email + ") store's (" + store_id + ") purchase history successfully.");

        } catch (Exception e) {
            response = new Response(new Exception("Failed get stores purchase history."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.6.1
     * @param store_id we want to close
     * @preconition the store is open
     * @return success/failure message
     */
    public String close_store_permanently(int store_id) {
        Response response = null;
        try {
            user_controller.check_admin_permission(loggedUser); // throws
            this.store_controller.close_store_permanently(store_id);
            response = new Response<>(null, "Store closed permanently");
            system_logger.add_log("Store (" + store_id + ") closed permanently.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to close store permanently."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }


    /**
     * Requirement 2.6.2
     * @param email of the user we want to cancel his subscribe
     * @return success/failure message
     */
    @Override
    public String remove_user(String email) {
        Response response = null;
        try {
            user_controller.check_admin_permission(loggedUser);
            // TODO: version 2
            // close store permanently
            user_controller.remove_user(loggedUser, email);
            // remove user from all owners and managers
            // remove all users complains & questions
            response = new Response<>(email, email + "Has been removed successfully from the system");
            system_logger.add_log("Removed user (" + email + ") from the system.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to remove user."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.6.3
     * @return all the questions for an admin or failure message
     */
    @Override
    public String admin_view_users_questions() {
        Response response = null;
        try {
            user_controller.check_admin_permission(loggedUser); // throws
            this.user_controller.view_users_questions();
            List<String> users_questions = this.user_controller.view_users_questions();
            response = new Response(null, "Admin received users complains successfully.");
            system_logger.add_log("Admin viewed users complains successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to get users complains."));
            error_logger.add_log(e);
        }
        return this.toJson(response);

    }

    /**
     * Requirement 2.6.3
     * @param question_id answer a specific question
     * @param answer of the admin
     * @return success/failure message
     */

    @Override
    public String admin_answer_user_question(int question_id, String answer) {
        Response response = null;
        try {
            user_controller.check_admin_permission(loggedUser); // throws
            this.user_controller.answer_user_question(question_id, answer);
            response = new Response(null, "Admin answered user complaint successfully.");
            system_logger.add_log("Admin answered user's complaint successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to answer user's complaint."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.6.4
     * @param store_id who we want to see her purchases
     * @return purchases history information or failure message
     */
    @Override
    public String admin_view_store_purchases_history(int store_id) {
        Response<String> response = null;
        try {
            user_controller.check_admin_permission(loggedUser); // throws
            String answer = this.store_controller.admin_view_store_purchases_history(store_id);
            response = new Response<>(answer, "Store purchases history received successfully");
            system_logger.add_log("Admin received store's (" + store_id + ") purchase history successfully.");

        } catch (Exception e) {
            response = new Response(new Exception("Failed get stores purchase history."));
            error_logger.add_log(e);

        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.6.4
     * @param user_email who we want to see his purchases
     * @return purchases history information or failure message
     */
    @Override
    public String admin_view_user_purchases_history(String user_email) {
        Response response = null;
        try {
            user_controller.check_admin_permission(loggedUser);
            UserPurchaseHistory userPurchaseHistory = user_controller.admin_view_user_purchase_history(user_email);
            response = new Response(userPurchaseHistory, "received user's purchase history successfully");
            system_logger.add_log("Admin received user's (" + user_email + ") purchase history successfully.");
        } catch (Exception e) {
            response = new Response(new Exception("Failed to get user's purchase history."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }

    /**
     * Requirement 2.6.5
     * @return stats of the market or failure message
     */
    @Override
    public String get_market_stats() {
        Response response = null;
        try {
            user_controller.check_admin_permission(loggedUser);
            Statistic stats = user_controller.get_statistics();
            response = new Response(stats, "Received market statistics successfully");
            system_logger.add_log("Admin received market statistics successfully.");

        } catch (Exception e) {
            response = new Response(new Exception("Failed to get market statistics."));
            error_logger.add_log(e);
        }
        return this.toJson(response);
    }


    // TODO: testing functions
    public boolean is_logged(){
        return !isGuest;
    }
}
