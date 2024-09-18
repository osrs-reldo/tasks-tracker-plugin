package net.reldo.taskstracker.data.jsondatastore.types;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TaskTypeDefinition
{
	public String slug; // "LEAGUE_3",
    public String name; // "League III: Shattered Relics",
	@SerializedName("json")
    public String jsonFilename; // "tasks/LEAGUE_3.json",
	public boolean enabled; // true,
	public boolean skillFilter; // true,
	public TaskTypePropertyDefinition[] properties;
    public Integer[] taskVarps;
    public Integer[] otherVarps; // [2614, 3276],
    public Integer[] varbits;
}
