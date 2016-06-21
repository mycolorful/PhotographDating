package per.yrj.photographdating.action;

import android.content.Context;

import java.util.Map;

public abstract class Action {

	public abstract String getAction();

	public abstract void doAction(Context context, Map<String, Object> data);
}