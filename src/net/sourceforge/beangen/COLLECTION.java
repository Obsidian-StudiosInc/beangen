package net.sourceforge.beangen;

public class COLLECTION {
	public static final int ONE_TO_MANY = 0;
	public static final int MANY_TO_MANY = 1;

	protected int type = -1;
	protected String obj_name = null;
	protected String alias = null;

	protected String getAlias()
	{
		return alias==null?obj_name:alias;
	}
}