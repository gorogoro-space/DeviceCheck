package space.gorogoro.devicecheck;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.DeviceOs;
import org.geysermc.floodgate.util.InputMode;
import org.geysermc.floodgate.util.UiProfile;

/*
 * DeviceCheck
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2026
 * @author     kubotan
 * @see        <a href="https://github.com/gorogoro-space/DeviceCheck">DeviceCheck on GitHub</a>
 */
public final class DeviceCheck extends JavaPlugin implements Listener {

    // PlugMan で途中ロードしても動くよう、起動時に状態を持たず、
    // コマンド実行のたびに各プラグインの API を参照する作りにしている。

    @Override
    public void onEnable() {
        // 途中ロード時に Floodgate が無効になっていたら、自分を停止する
        if (!Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            getLogger().severe("Floodgate が有効になっていないため、DeviceCheck を停止します");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!viaEnabled()) {
            getLogger().warning("ViaVersion が見つかりません。Java版のクライアントバージョンは表示されません");
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        getLogger().info("DeviceCheck を読み込みました");
    }

    @Override
    public void onDisable() {
        // リスナーは停止時に自動で解除され、タスクは登録していないので、後片付けは不要
        getLogger().info("DeviceCheck を停止しました");
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        event.getCommands().remove("device");
        event.getCommands().remove("devicecheck:device");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // OP 専用。コンソールも isOp() が true になる
        if (!sender.isOp()) {
            sendUnknownCommand(sender, label, args);
            return true;
        }

        // 対象プレイヤーを決める（引数なしなら自分）
        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("プレイヤーが見つかりません: " + args[0], NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(Component.text("コンソールからはプレイヤー名を指定してください", NamedTextColor.RED));
            return false;
        }

        FloodgateApi api = FloodgateApi.getInstance();
        if (api.isFloodgatePlayer(target.getUniqueId())) {
            showBedrock(sender, target, api.getPlayer(target.getUniqueId()));
        } else {
            showJava(sender, target);
        }
        return true;
    }

    // ---- 統合版 ----

    private void showBedrock(CommandSender sender, Player target, FloodgatePlayer fp) {
        DeviceOs os = fp.getDeviceOs();
        sender.sendMessage(Component.text(target.getName() + " は統合版です", NamedTextColor.GREEN));
        line(sender, "種類", category(os));
        line(sender, "OS", String.valueOf(os));
        line(sender, "操作", inputName(fp.getInputMode()));
        line(sender, "UI", uiName(fp.getUiProfile()));
        line(sender, "バージョン", fp.getVersion());
        line(sender, "言語", fp.getLanguageCode());
        line(sender, "Ping", target.getPing() + "ms");
        line(sender, "Java版とのリンク", fp.isLinked() ? "あり" : "なし");
    }

    /** OSをスマホ / PC / ゲーム機 に分類する */
    private static String category(DeviceOs os) {
        return switch (os) {
            case GOOGLE, IOS, AMAZON, WINDOWS_PHONE -> "スマホ・タブレット";
            case UWP, WIN32, OSX -> "PC";
            case NX, PS4, XBOX -> "ゲーム機";
            // LINUX はコンパイルに使う API(geyser common 2.2.1)に無いため、名前で判定する
            default -> os.name().equals("LINUX") ? "PC" : "不明";
        };
    }

    private static String inputName(InputMode input) {
        return switch (input) {
            case KEYBOARD_MOUSE -> "キーボード・マウス";
            case TOUCH -> "タッチ";
            case CONTROLLER -> "コントローラー";
            case VR -> "VR";
            default -> "不明";
        };
    }

    private static String uiName(UiProfile ui) {
        return switch (ui) {
            case CLASSIC -> "クラシック（PC向け）";
            case POCKET -> "ポケット（スマホ向け）";
        };
    }

    // ---- Java版 ----

    private void showJava(CommandSender sender, Player target) {
        sender.sendMessage(Component.text(target.getName() + " はJava版です", NamedTextColor.AQUA));
        line(sender, "クライアント", brand(target.getClientBrandName()));
        line(sender, "バージョン", viaEnabled() ? ViaHook.clientVersion(target.getUniqueId()) : "不明（ViaVersionなし）");
        line(sender, "言語", target.locale().toString());
        line(sender, "Ping", target.getPing() + "ms");
        line(sender, "描画距離", target.getClientViewDistance() + "チャンク");
    }

    /** ブランド名をわかりやすく表示する。接続直後は null のことがある */
    private static String brand(String raw) {
        if (raw == null || raw.isBlank()) {
            return "不明";
        }
        String lower = raw.toLowerCase();
        if (lower.equals("vanilla")) return "バニラ";
        if (lower.contains("neoforge")) return "NeoForge";
        if (lower.contains("forge")) return "Forge";
        if (lower.contains("fabric")) return "Fabric";
        if (lower.contains("quilt")) return "Quilt";
        if (lower.contains("lunar")) return "Lunar Client";
        if (lower.contains("feather")) return "Feather Client";
        if (lower.contains("badlion")) return "Badlion Client";
        return raw; // 知らないものはそのまま表示
    }

    // ---- 共通 ----

    /** バニラの「不明なコマンド」と同じ見た目の文を送る(クライアントの言語で表示される) */
    private static void sendUnknownCommand(CommandSender sender, String label, String[] args) {
        String input = args.length == 0 ? label : label + " " + String.join(" ", args);
        sender.sendMessage(Component.translatable("command.unknown.command", NamedTextColor.RED));
        sender.sendMessage(Component.text("", NamedTextColor.GRAY)
                .append(Component.text(input, NamedTextColor.RED, TextDecoration.UNDERLINED))
                .append(Component.translatable("command.context.here", NamedTextColor.RED, TextDecoration.ITALIC)));
    }

    private static boolean viaEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
    }

    private static void line(CommandSender sender, String label, String value) {
        sender.sendMessage(Component.text("  " + label + ": ", NamedTextColor.YELLOW)
                .append(Component.text(value == null ? "不明" : value, NamedTextColor.WHITE)));
    }
}
