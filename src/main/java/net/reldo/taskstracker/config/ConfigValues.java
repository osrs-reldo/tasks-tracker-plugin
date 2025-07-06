package net.reldo.taskstracker.config;


public class ConfigValues {

    public enum CompletedFilterValues
    {
        COMPLETE_AND_INCOMPLETE("Both"),
        COMPLETE("Complete"),
        INCOMPLETE("Incomplete");

        public final String name;

        public String toString() { return name; }

        CompletedFilterValues(String name)
        {
            this.name = name;
        }
    }

    public enum TrackedFilterValues
    {
        TRACKED_AND_UNTRACKED("Both"),
        TRACKED("Tracked"),
        UNTRACKED("Untracked");

        public final String name;

        public String toString() { return name; }

        TrackedFilterValues(String name)
        {
            this.name = name;
        }
    }

    public enum IgnoredFilterValues
    {
        NOT_IGNORED("Not Ignored"),
        IGNORED_AND_NOT_IGNORED("Both"),
        IGNORED("Ignored");

        public final String name;

        public String toString() { return name; }

        IgnoredFilterValues(String name)
        {
            this.name = name;
        }
    }

    public enum TaskListTabs
    {
        TAB_ONE("tab1"),
        TAB_TWO("tab2"),
        TAB_THREE("tab3");

        public final String configID;

        TaskListTabs(String configID)
        {
            this.configID = configID;
        }
    }

    public enum SortDirections
    {
        ASCENDING,
        DESCENDING;
    }

}
