package net.sourceforge.beangen;

import java.util.Vector;

public class FIELD {

	protected String db_type = null;
	protected String db_name = null;
	protected String name = null;
	protected String type = null;
	protected boolean nullable = false;
	protected boolean pk = false;
	protected boolean fk = false;
	protected boolean ro = false;
	protected String db_alias = "";

	public String toString()
	{
		return db_type + ":" + db_name + ":" + name + ":" + type + ":" +
			nullable + ":" + pk + ":" + fk + ":" + ro + ":" + db_alias;
	}
}