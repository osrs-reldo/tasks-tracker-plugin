package net.reldo.taskstracker.data.reldo;

import com.google.gson.Gson;
import java.util.HashMap;
import lombok.Data;

@Data
public class ReldoImport
{
	public int version;
	public String rsn;
	private HashMap<Integer, ReldoTaskSave> tasks;

	public static ReldoImport fromJson(String json)
	{
		Gson gson = new Gson();
		return gson.fromJson(json, ReldoImport.class);
	}
}
