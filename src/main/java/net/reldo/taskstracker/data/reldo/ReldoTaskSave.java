package net.reldo.taskstracker.data.reldo;

import lombok.Data;

@Data
public class ReldoTaskSave
{
	long completed;
	long todo;
	long ignored;
	int order;
	String notes;
	long lastUpdated;
}
