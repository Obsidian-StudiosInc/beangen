package net.sourceforge.beangen;

public class RESOURCE {

	public static final int DATASOURCE = 0;

	protected int type = -1;
	protected String name = null;
	protected String jndi = null;
	protected String engine = null;
	protected String key_gen = null;

	protected String getResourceType()
	{
		if(this.type == RESOURCE.DATASOURCE)
			return "javax.sql.DataSource";
		else
			System.err.println("getResourceType() : " + this.type + " Unsupported yet!");

		return "Unknown";
	}
}