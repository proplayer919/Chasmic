package dev.proplayer919.chasmic.gui;

import lombok.Getter;

@Getter
public class GuiClickAction {
    private final GuiClickActionType actionType;
    private GuiScreen screenToOpen;
    private String commandToExecute;

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
}
