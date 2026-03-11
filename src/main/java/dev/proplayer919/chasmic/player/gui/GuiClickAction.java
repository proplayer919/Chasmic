package dev.proplayer919.chasmic.player.gui;

import lombok.Getter;
import net.minestom.server.event.inventory.InventoryPreClickEvent;

import java.util.function.Consumer;

@Getter
public class GuiClickAction {
    private final GuiClickActionType actionType;
    private GuiScreen screenToOpen;
    private String commandToExecute;
    private Consumer<InventoryPreClickEvent> customAction;

    public GuiClickAction(GuiClickActionType actionType) {
        this.actionType = actionType;
    }

    public GuiClickAction(GuiClickActionType actionType, GuiScreen screenToOpen) {
        this(actionType);
        this.screenToOpen = screenToOpen;
    }

    public GuiClickAction(GuiClickActionType actionType, String commandToExecute) {
        this(actionType);
        this.commandToExecute = commandToExecute;
    }

    public GuiClickAction(GuiClickActionType actionType, Consumer<InventoryPreClickEvent> customAction) {
        this(actionType);
        this.customAction = customAction;
    }
}
