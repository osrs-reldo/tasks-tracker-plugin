package net.reldo.taskstracker.data;

public interface CallbackCommand<T>
{
	void execute(T result);
}
