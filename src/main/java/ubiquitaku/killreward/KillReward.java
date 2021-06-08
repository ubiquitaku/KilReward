package ubiquitaku.killreward;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class KillReward extends JavaPlugin implements @NotNull Listener {
    FileConfiguration config;
    double rem;
    double get;
    VaultManager vault;

    @Override
    public void onEnable() {
        // Plugin startup logic
        vault = new VaultManager();
        saveDefaultConfig();
        configReload();
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("killrew")) {
            if (!sender.hasPermission("killrew.op")) {
                sender.sendMessage("§c§lあなたはこのコマンドを使用する権限を持っていません");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("説明面倒だからこれで察して");
                sender.sendMessage("/killrew rem <消す割合(%)>");
                sender.sendMessage("/killrew get <殺人鬼が得る割合(%)>");
                sender.sendMessage("消す割合より得る割合がでかいと増えます");
                sender.sendMessage("どちらの割合も単位は%で保存されます(10ってやったら10%)");
                vault.deposit((Player) sender,10000);
                return true;
            }
            if (args[0].equals("rem")) {
                if (args.length != 2) {
                    sender.sendMessage("argsの数が間違っています");
                    return true;
                }
                config.set("removeMoney",args[1]);
                configReload();
                sender.sendMessage("消す割合を設定しました");
                return true;
            }
            if (args[0].equals("get")) {
                if (args.length != 2) {
                    sender.sendMessage("argsの数が間違っています");
                    return true;
                }
                config.set("getMoney",args[1]);
                configReload();
                sender.sendMessage("得る割合を設定しました");
            }
//            if ()
//            if (!(sender instanceof Player)) return true;
//            Player player = (Player) sender;
//            vault.deposit(player, 100);
        }
        return true;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        if (e.getEntity().hasPermission("killrew.op")) {
            return;
        }
        if (e.getEntity().hasPermission("killrew.not")) {
            return;
        }
        Player dead;
        Player killer;
        try {
            dead = e.getEntity();
            killer = e.getEntity().getKiller();
        } catch (NullPointerException exception) {
            return;
        }
        double value = vault.getBalance(dead);
        vault.withdraw(dead,value*rem);
        vault.deposit(killer,value*get);
    }

    public void configReload() {
        saveConfig();
        config = getConfig();
        rem = config.getDouble("removeMoney")/100;
        get = config.getDouble("getMoney")/100;
    }
}
