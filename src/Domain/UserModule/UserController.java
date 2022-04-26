package Domain.UserModule;

import Domain.Basket;

import java.util.HashMap;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UserController {
    
    // ------------------- fields -------------------------------------
    private Map<String,User> users;              // email,user
    private Map<Integer,User> onlineUsers;       // id,user
    private AtomicInteger ID;
    private AtomicInteger purchaseID;
    private Object usersLock;
    private Object onlineUsersLock;
    private Object idLock;

    // ------------------- singleton class ----------------------------
    private static class SingletonHolder{
        private static UserController instance = new UserController();
    }
    
    // ------------------- constructors --------------------------------
    public UserController() {
        this.ID = new AtomicInteger(0);
        this.purchaseID = new AtomicInteger(0);
        this.users = Collections.synchronizedMap(new HashMap<>()) ;        //thread safe
        this.onlineUsers = Collections.synchronizedMap(new HashMap<>()) ;  //thread safe
        this.usersLock = new Object();
        this.onlineUsersLock = new Object();
        this.idLock = new Object();
    }

    
    // ------------------ methods --------------------------------------
    /**
     * @return the instance of the user controller
     */
    public static UserController getInstance(){
        return SingletonHolder.instance;
    }

    /**
     * function that connects new guest to the system.
     * @return guest's online id
     */
    public int guest_login() {
        int cur_guest_id = ID.getAndIncrement();    // synchronized
        User newUser = new User();
        onlineUsers.put(cur_guest_id, newUser);
        return cur_guest_id;
    }

    /**
     * non synchronized function
     * function that check if user email is already registered in the system
     * @param email represnts the user email
     * @return true if the email is registered in the system else false
     */
    private boolean isRegistered(String email){
        return users.containsKey(email);
    }

//    /**
//     * function that checks if current id exists in online
//     * @param id represent the id of specific online member
//     * @return true if the member is online
//     */
//    private boolean isOnline(int id){
//        return onlineUsers.containsKey(id);
//    }

    /**
     * function that register new user to the system.
     * new user gets an empty cart
     * @param email new email of the user
     * @param pw    new password of the user
     * @param name  the user's name
     * @param lastName the user's last name
     */
    public void register(int ID, String email, String pw, String name, String lastName) throws IllegalAccessException {
        synchronized (usersLock) {
            if (isRegistered(email))
                throw new IllegalAccessException("user email already exists in the system"); //todo cannot notify about existing email
            User user = onlineUsers.get(ID);
            user.register(email, pw, name, lastName);
            users.put(email, user);
        }
    }

    /**
     * @param ID the user's id in the system
     * @param email the user email to log-in
     * @param password the user password
     * @return the status if log-in succeed
     */
    public boolean login(int ID,String email, String password) {
        if(isRegistered(email) && users.get(email).login(password)){
            User user = users.get(email);
            onlineUsers.put(ID, user);
            return true;
        }
        return false;
    }

    /**
     * @param user id
     * @return the user's cart
     */
    public Cart view_user_cart(int user) {
        return onlineUsers.get(user).getCart();
    }


    /**
     * function that gets the basket by store ID
     * @param userID represents the user id in the online map
     * @param storeID represents the store id
     * @return the store basket from the user's cart
     */
    public Basket getBasketByStoreID(int userID , int storeID) {
        User user = onlineUsers.get(userID);
        return user.getBasketByStoreID(storeID);
    }

    /**
     * function that add basket to the cart
     * @param userID the online user ID.
     * @param storeID represents the store id
     * @param basket
     */
    public void addBasket(int userID, int storeID, Basket basket) {
        User user = onlineUsers.get(userID);
        user.addBasket(storeID,basket);
    }

    /**
     * function that remove basket in case its empty
     * @param loggedUser
     * @param storeID
     * @param storeBasket
     */
    public void removeBasketIfNeeded(int loggedUser, int storeID, Basket storeBasket) {
        User user = onlineUsers.get(loggedUser);
        user.removeBasketIfNeeded(storeID, storeBasket);
    }


    /**
     * function that gets all the baskets from the user's cart
     * @param loggedUser
     * @return cart's baskets
     */
    public Map<Integer, Basket> getBaskets(int loggedUser) {
        User user = onlineUsers.get(loggedUser);
        return user.getBaskets();
    }


    /**
     * function that returns the logged user cart
     * @param loggedUser represents the user ID
     * @return the user's cart
     */
    public Cart getCart(int loggedUser) {
        User user = onlineUsers.get(loggedUser);
        return user.getCart();
    }

    /**
     * function that makes a new purchase and add it to the history & clears the cart
     * @param loggedUser
     */
    public void buyCart(int loggedUser) {
        User user = onlineUsers.get(loggedUser);
        user.buyCart(purchaseID.getAndIncrement());
    }


    public void check_if_user_buy_from_this_store(int loggedUser,int store_id) throws Exception {
        User user = onlineUsers.get(loggedUser);
        user.check_if_user_buy_from_this_store(store_id);
    }

    public void check_if_user_buy_this_product(int loggedUser,int productID, int storeID) throws Exception {
        User user = onlineUsers.get(loggedUser);
        user.check_if_user_buy_this_product(storeID,productID);
    }

    public UserHistory view_user_purchase_history(int loggedUser) throws Exception {
        User user = onlineUsers.get(loggedUser);
        return user.view_user_purchase_history();
    }

    public String get_user_name(int loggedUser) throws Exception {
        User user = onlineUsers.get(loggedUser);
        return user.get_user_name();
    }

    public String get_user_last_name(int loggedUser) throws Exception {
        User user = onlineUsers.get(loggedUser);
        return user.get_user_last_name();
    }
}