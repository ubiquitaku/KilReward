package ubiquitaku.killreward;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {
    Economy economy;

    public VaultManager() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    //残高
    public double getBalance(Player player){
        return economy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()).getPlayer());
    }

    //プレイヤーの所持金を減らす
    //引き出し完了でtrue,失敗でfalseが返る
    public boolean withdraw(Player player, double money){
        try {
            OfflinePlayer p = Bukkit.getOfflinePlayer(player.getUniqueId());
            if(p == null){
                Bukkit.getLogger().info("指定したプレイヤーが見つからなかったためプレイヤーの残高を減少させることができませんでした");
                return false;
            } EconomyResponse resp = economy.withdrawPlayer(p,money); if(resp.transactionSuccess()){
                if(p.isOnline()) {
                    p.getPlayer().sendMessage(ChatColor.YELLOW + "$" + (int) money + "支払いました");
                }
                return true;
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    //残高を増やす
    //完了でtrue,失敗でfalse
    public boolean deposit(Player player,double money){
        try {
            OfflinePlayer p = Bukkit.getOfflinePlayer(player.getUniqueId());
            if(p == null){
                Bukkit.getLogger().info("指定したプレイヤーが見つからなかったため残高の増加ができませんでした");
                return false;
            }
            EconomyResponse resp = economy.depositPlayer(p,money);
            if(resp.transactionSuccess()){
                if(p.isOnline()){
                    p.getPlayer().sendMessage(ChatColor.YELLOW + "$"+(int)money+"受取りました");
                }
                return true;
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
