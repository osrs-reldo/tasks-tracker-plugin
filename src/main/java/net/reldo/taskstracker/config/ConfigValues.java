package net.reldo.taskstracker.config;


public class ConfigValues {

    public enum CompletedFilterValues
    {
        COMPLETE_AND_INCOMPLETE,
        COMPLETE,
        INCOMPLETE;
    }

    public enum TrackedFilterValues
    {
        TRACKED_AND_UNTRACKED,
        TRACKED,
        UNTRACKED;
    }

    public enum IgnoredFilterValues
    {
        NOT_IGNORED,
        IGNORED_AND_NOT_IGNORED,
        IGNORED;
    }

    public enum TaskListTabs
    {
        TRACKED,
        ALL,
        CUSTOM;
    }

    public enum SortDirections
    {
        ASCENDING,
        DESCENDING;
    }

}
