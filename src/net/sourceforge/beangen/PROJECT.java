package net.sourceforge.beangen;

public class PROJECT {
	protected String name = null;
	protected String unix_name = "";
	protected CACHE lazy = new CACHE();

	protected String getName()
	{
		return this.name==null?"Default":this.name;
	}
}