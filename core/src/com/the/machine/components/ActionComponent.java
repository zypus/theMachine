package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class ActionComponent extends AbstractComponent {
    private ActionType type;

    public ActionComponent(ActionType newType) {
        type = newType;
    }

    public void setActionType(ActionType newType) {type = newType; }

    public ActionType getActionType() { return type; }

    public enum ActionType {
        WINDOW_DESTROY,
        DOOR_OPEN,
        DOOR_CLOSE,
        TOWER_ENTER,
        TOWER_LEAVE,
        SOUND_ALARM           // Only for guards which are inside a tower
    }
}
