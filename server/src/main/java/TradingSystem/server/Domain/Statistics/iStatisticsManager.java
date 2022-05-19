package TradingSystem.server.Domain.Statistics;

public interface iStatisticsManager {
    void inc_login_count();
    void inc_logout_count();
    void inc_connect_system_count();
    void inc_register_count();
    void inc_buy_cart_count();
    Statistic get_system_statistics();
}

