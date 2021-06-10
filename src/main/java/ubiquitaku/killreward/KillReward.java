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
    boolean objectDeath;
    String prefix = "§l[§r§a§lKillReward§r§l]§r";
    int rem;
    int get;
    VaultManager vault;

    @Override
    public void onEnable() {
        // Plugin startup logic
        vault = new VaultManager();
        saveDefaultConfig();
        config = getConfig();
        objectDeath = config.getBoolean("obj");
        rem = config.getInt("removeMoney");
        get = config.getInt("getMoney");
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
                sender.sendMessage("--------------------"+prefix+"--------------------");
                sender.sendMessage("§d説明面倒だからこれで察して");
                sender.sendMessage("§d/killrew reload : 設定ファイル(config.yml)のリロードを行います");
                sender.sendMessage("§d/killrew rem <殺された人から消える金額の割合(%)>");
                sender.sendMessage("§d/killrew get <殺した人が殺された人の所持金から得る割合(%)>");
                sender.sendMessage("§d/killrew obj <on or off> : PK以外で死んだときに所持金の削除を行うか");
                sender.sendMessage("§d↑※行う場合は削除のみ、変更は再起動等を行っても維持されます");
                sender.sendMessage("§d消す割合より得る割合がでかいと増殖などが可能となります");
                sender.sendMessage("§dどちらの割合も単位は%で保存されます(10ってやったら10%)");
                sender.sendMessage("--------------------------------------------------");
                return true;
            }
            if (args[0].equals("rem")) {
                if (args.length != 2) {
                    sender.sendMessage(prefix+"argsの数が間違っています");
                    return true;
                }
                config.set("removeMoney",args[1]);
                rem = Integer.parseInt((args[1]));
                configReload();
                sender.sendMessage(prefix+"殺されたプレイヤーから削除される割合を変更しました");
                return true;
            }
            if (args[0].equals("get")) {
                if (args.length != 2) {
                    sender.sendMessage(prefix+"argsの数が間違っています");
                    return true;
                }
                config.set("getMoney",args[1]);
                get = Integer.parseInt((args[1]));
                configReload();
                sender.sendMessage(prefix+"殺したプレイヤーが得る割合を変更しました");
                return true;
            }
            if (args[0].equals("reload")) {
                reloadConfig();
                config = getConfig();
                objectDeath = config.getBoolean("obj");
                rem = config.getInt("removeMoney");
                get = config.getInt("getMoney");
                sender.sendMessage(prefix+"リロード完了");
                return true;
            }
            if (args[0].equals("obj")) {
                if (args.length != 2) {
                    sender.sendMessage(prefix+"argsの数が間違っています");
                    return true;
                }
                if (args[1].equals("on")) {
                    if (objectDeath) {
                        sender.sendMessage(prefix+"既にonになっています");
                        return true;
                    }
                    objectDeath = true;
                    config.set("obj",true);
                    configReload();
                    sender.sendMessage(prefix+"PK以外の死亡でも所持金が減るようになりました");
                    return true;
                }
                if (args[1].equals("off")) {
                    if (!objectDeath) {
                        sender.sendMessage(prefix+"既にoffになっています");
                        return true;
                    }
                    objectDeath = false;
                    config.set("obj",false);
                    configReload();
                    sender.sendMessage(prefix+"PK以外の死亡による所持金削除が無効化されました");
                    return true;
                }
            }
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
        dead = e.getEntity();
        Player killer;
        double value = vault.getBalance(dead);
        if (!Bukkit.getOnlinePlayers().contains(e.getEntity().getKiller())) {
            if (objectDeath) {
                vault.withdraw(dead,value*rem/100);
            }
            return;
        }
        try {
            killer = e.getEntity().getKiller();
        } catch (NullPointerException exception) {
            return;
        }
        vault.withdraw(dead,Math.round(value*rem/100));
        vault.deposit(killer,Math.round(value*get/100));
    }

    public void configReload() {
        saveConfig();
        config = getConfig();
    }
}
