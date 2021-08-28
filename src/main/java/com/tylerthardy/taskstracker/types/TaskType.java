package com.tylerthardy.taskstracker.types;

import com.tylerthardy.taskstracker.types.combattask.CombatTask;
import com.tylerthardy.taskstracker.types.league1.League1Task;
import com.tylerthardy.taskstracker.types.league2.League2Task;
import com.tylerthardy.taskstracker.types.testtask.TestTask;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
public enum TaskType
{

    COMBAT("Combat Tasks", "combat_tasks.min.json", CombatTask.class),
    LEAGUE_1("League I: Twisted", "league1_tasks.min.json", League1Task.class),
    LEAGUE_2("League II: Trailblazer", "league2_tasks.min.json", League2Task.class),
    //LEAGUE_3("League III", "league3_tasks.min.json", League3Task.class),
    TEST("Test Tasks", "test_tasks.json", TestTask.class);

    public final String name;
    public final String dataFileName;
    public final Type classType;

    TaskType(String name, String dataFileName, Type classType)
    {
        this.name = name;
        this.dataFileName = dataFileName;
        this.classType = classType;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
