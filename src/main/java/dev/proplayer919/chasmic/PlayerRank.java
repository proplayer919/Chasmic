package dev.proplayer919.chasmic;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
public enum PlayerRank {
    DEFAULT("default", null, 0, NamedTextColor.WHITE, NamedTextColor.GRAY,
            List.of()),

    VIP("vip", Component.text("[VIP]").color(NamedTextColor.GREEN), 1, NamedTextColor.GREEN, NamedTextColor.WHITE,
            List.of()),

    VIP_PLUS("vip_plus", Component.text("[").color(NamedTextColor.GREEN)
            .append(Component.text("VIP").color(NamedTextColor.GREEN))
            .append(Component.text("+").color(NamedTextColor.GOLD))
            .append(Component.text("]").color(NamedTextColor.GREEN)), 2, NamedTextColor.GREEN, NamedTextColor.WHITE,
            List.of()),

    VIP_PLUS_PLUS("vip_plus_plus", Component.text("[").color(NamedTextColor.GREEN)
            .append(Component.text("VIP").color(NamedTextColor.GREEN))
            .append(Component.text("++").color(NamedTextColor.GOLD))
            .append(Component.text("]").color(NamedTextColor.GREEN)), 3, NamedTextColor.GREEN, NamedTextColor.WHITE,
            List.of()),

    PRO("pro", Component.text("[PRO]").color(NamedTextColor.AQUA), 4, NamedTextColor.AQUA, NamedTextColor.WHITE,
            List.of()),

    PRO_PLUS("pro_plus", Component.text("[").color(NamedTextColor.AQUA)
            .append(Component.text("PRO").color(NamedTextColor.AQUA))
            .append(Component.text("+").color(NamedTextColor.GOLD))
            .append(Component.text("]").color(NamedTextColor.AQUA)), 5, NamedTextColor.AQUA, NamedTextColor.WHITE,
            List.of()),

    PRO_PLUS_PLUS("pro_plus_plus", Component.text("[").color(NamedTextColor.AQUA)
            .append(Component.text("PRO").color(NamedTextColor.AQUA))
            .append(Component.text("++").color(NamedTextColor.GOLD))
            .append(Component.text("]").color(NamedTextColor.AQUA)), 6, NamedTextColor.AQUA, NamedTextColor.WHITE,
            List.of()),

    MEDIA("media", Component.text("[MEDIA]").color(NamedTextColor.RED), 7, NamedTextColor.RED, NamedTextColor.WHITE,
            List.of("command.media.record", "command.media.stream")),

    BUILDER("builder", Component.text("[BUILDER]").color(NamedTextColor.YELLOW), 8, NamedTextColor.YELLOW, NamedTextColor.WHITE,
            List.of("builder.*")),

    ADMIN("admin", Component.text("[ADMIN]").color(NamedTextColor.LIGHT_PURPLE), 9, NamedTextColor.LIGHT_PURPLE, NamedTextColor.WHITE,
            List.of("admin.*")),

    OWNER("owner", Component.text("[OWNER]").color(NamedTextColor.GOLD), 10, NamedTextColor.GOLD, NamedTextColor.WHITE,
            List.of("*"));

    private final String id;
    private final Component name;
    private final int priority;
    private final TextColor usernameColor;
    private final TextColor messageColor;
    private final List<String> defaultPermissions;

    PlayerRank(String id, Component name, int priority, TextColor usernameColor, TextColor messageColor, List<String> defaultPermissions) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.usernameColor = usernameColor;
        this.messageColor = messageColor;
        this.defaultPermissions = defaultPermissions;
    }

    public Component getDisplayName() {
        return name != null ? name : Component.text("Default").color(NamedTextColor.WHITE);
    }

    public List<String> getInheritedDefaultPermissions() {
        Set<String> inheritedPermissions = new LinkedHashSet<>();

        for (PlayerRank rank : values()) {
            if (rank.priority <= this.priority) {
                inheritedPermissions.addAll(rank.defaultPermissions);
            }
        }

        return new ArrayList<>(inheritedPermissions);
    }
}
