package com.tylerthardy.combattaskstracker.widgets;

import net.runelite.api.widgets.WidgetID;

public enum CombatTasksWidgetInfo {
    ;
    // FAIRY_RING_LEFT_ORB_CLOCKWISE(WidgetID.FAIRY_RING_GROUP_ID, CombatTasksWidgetID.CombatTasks);

    private final int groupId;
    private final int childId;

    CombatTasksWidgetInfo(int groupId, int childId)
    {
        this.groupId = groupId;
        this.childId = childId;
    }

    /**
     * Gets the ID of the group-child pairing.
     *
     * @return the ID
     */
    public int getId()
    {
        return groupId << 16 | childId;
    }

    /**
     * Gets the group ID of the pair.
     *
     * @return the group ID
     */
    public int getGroupId()
    {
        return groupId;
    }

    /**
     * Gets the ID of the child in the group.
     *
     * @return the child ID
     */
    public int getChildId()
    {
        return childId;
    }

    /**
     * Gets the packed widget ID.
     *
     * @return the packed ID
     */
    public int getPackedId()
    {
        return groupId << 16 | childId;
    }

    /**
     * Utility method that converts an ID returned by {@link #getId()} back
     * to its group ID.
     *
     * @param id passed group-child ID
     * @return the group ID
     */
    public static int TO_GROUP(int id)
    {
        return id >>> 16;
    }

    /**
     * Utility method that converts an ID returned by {@link #getId()} back
     * to its child ID.
     *
     * @param id passed group-child ID
     * @return the child ID
     */
    public static int TO_CHILD(int id)
    {
        return id & 0xFFFF;
    }

    /**
     * Packs the group and child IDs into a single integer.
     *
     * @param groupId the group ID
     * @param childId the child ID
     * @return the packed ID
     */
    public static int PACK(int groupId, int childId)
    {
        return groupId << 16 | childId;
    }

}
