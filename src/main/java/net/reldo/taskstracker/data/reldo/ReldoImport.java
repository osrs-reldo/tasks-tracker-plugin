package net.reldo.taskstracker.data.reldo;

import java.util.HashMap;
import lombok.Data;

@Data
public class ReldoImport
{
	public int version;
	public String rsn;
	private HashMap<Integer, ReldoTaskSave> tasks;
}
