package sandtechnology.redpacket.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import sandtechnology.redpacket.RedPacketPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;

public class CompatibilityHelper {
    //NMS名： "org.bukkit.craftbukkit.v1_x_Rx"->{"org","bukkit","craftbukkit","v1_x_Rx"}->"v1_x_Rx"
    private static final String nmsName;
    private static final int version;

    static {
        try {
            // 使用Bukkit版本直接解析主版本号
            String bukkitVersion = Bukkit.getBukkitVersion().split("-")[0];
            String[] versionParts = bukkitVersion.split("\\.");
            version = Integer.parseInt(versionParts[1]);
            // 构造NMS名（假设为v1_21_R0）
            nmsName = "v1_" + version + "_R0";
        } catch (Exception e) {
            RedPacketPlugin.log(Level.SEVERE, "版本解析失败: " + e.getMessage());
            throw new IllegalStateException("插件不兼容当前版本！");
        }
    }

    private static Class<?> IChatBaseComponent;
    private static Class<?> chatSerializer;
    private static Class<?> craftPlayer;
    private static Class<?> entityPlayer;
    private static Class<?> PacketPlayOutTitle;
    private static Class<?> EnumTitleAction;
    private static Class<?> PlayerConnection;
    private static Enum<? extends Enum>[] EnumTitleActions;
    private static Method getHandle;
    private static Method sendMessage;
    private static Method toComponent;
    private static Method sendPacket;
    private static Constructor<?> CPacketPlayOutTitle;

    private CompatibilityHelper() {
    }


    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsName + "." + name);
    }

    public static void setup() {
        if (version < 8) {
            RedPacketPlugin.log(Level.SEVERE, "插件只支持1.8+版本！");
            throw new IllegalStateException("插件只支持1.8+版本！");
        }
        // 1.12及以上版本无需反射
        if (version <= 12) {
            try {
                // 若需要兼容旧版本（1.8-1.12），可在此处保留必要反射逻辑
                // 例如：CraftPlayer类路径可能需要动态拼接
                String craftPlayerPath = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".entity.CraftPlayer";
                craftPlayer = Class.forName(craftPlayerPath);
                getHandle = craftPlayer.getMethod("getHandle");
            } catch (Exception e) {
                RedPacketPlugin.log(Level.WARNING, "反射加载旧版本类失败，部分功能可能受限：" + e.getMessage());
            }
        }
    }

    private static Object invoke(Method method, Object obj, Object... objs) {
        try {
            return method.invoke(obj, objs);
        } catch (Exception e) {
            throw new RuntimeException("在反射调用方法时发生错误！" + method.getName(), e);
        }
    }

    private static Object newInstance(Constructor constructor, Object... objs) {
        try {
            return constructor.newInstance(objs);
        } catch (Exception e) {
            throw new RuntimeException("在反射实例化类时发生错误！类名：", e);
        }
    }

    public static void playLevelUpSound(Player player) {
        // 统一使用1.9+名称
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
    }

    public static void playMeowSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 100, 1);
    }

    private static void playSound(Player player, String name) {
        player.playSound(player.getLocation(), Sound.valueOf(name), 100, 1);
    }

    private static Object getDeclaredFieldAndGetIt(Class<?> target, String field, Object instance) {
        try {
            return target.getDeclaredField(field).get(instance);
        } catch (Exception e) {
            throw new RuntimeException("在反射获取字段时发生错误！方法名：", e);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        // 1.11+直接使用API
        player.sendTitle(title, subtitle, -1, -1, -1);
    }

    public static void sendJSONMessage(Player player, BaseComponent... components) {
        // 1.12+直接使用Spigot API
        player.spigot().sendMessage(components);
    }

}
