/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 4. Products derived from this Software may not be called "BeanGen"
 *    nor may "BeanGen" appear in their names without prior written
 *    permission of BeanGen Group.
 *
 * 5. Due credit should be given to the BeanGen Project
 *    (http://beangen.sourceforge.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY BEANGEN GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * BEANGEN GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2003 (C) Paulo Lopes. All Rights Reserved.
 *
 * $Id$
 */
package net.sourceforge.beangen;

import java.util.Vector;

public class EJB {
	public static final int BMP = 0;
	public static final int CMP = 1;
	public static final int STATEFULL = 2;
	public static final int STATELESS = 3;
	public static final int MESSAGE = 4;

	protected int type = -1;
	protected String db_name = null;
	protected String name = null;
	protected String app_package = "";
	protected String description = "";
	protected String author = "";
	protected String jndi_basepath = null;
	protected String transaction = "Supports";
	protected Vector fields = new Vector();
	protected Vector resources = new Vector();
	protected Vector collections = new Vector();

	protected String getJNDIPath()
	{
		return this.jndi_basepath==null?"":(this.jndi_basepath + "/");
	}

	protected String getPkName()
	{
		for(int i=0; i<this.fields.size(); i++)
		{
			if(((FIELD)this.fields.elementAt(i)).pk)
			{
				return ((FIELD)this.fields.elementAt(i)).name;
			}
		}
		//System.err.println("No PK found for :" + this.db_name);
		return "null";
	}

	protected String getPkType()
	{
		for(int i=0; i<this.fields.size(); i++)
		{
			if(((FIELD)this.fields.elementAt(i)).pk)
			{
				return ((FIELD)this.fields.elementAt(i)).type;
			}
		}

		System.err.println("No PK found for :" + this.db_name + " assuming CompoundKey");
		return TextUtils.toSunClassName(this.name + "_p_k");
	}

	protected String getType()
	{
		if(this.type==EJB.BMP || this.type==EJB.CMP)
			return "Entity";
		else if(this.type==EJB.STATEFULL || this.type == EJB.STATELESS)
			return "Session";
		else if(this.type==EJB.MESSAGE)
			System.err.println("Don't Know how to handle MessageBeans yet...");

		return "Unknown";
	}

	protected String getPkDbName()
	{
		for(int i=0; i<this.fields.size(); i++)
		{
			if(((FIELD)this.fields.elementAt(i)).pk)
			{
				return ((FIELD)this.fields.elementAt(i)).db_name;
			}
		}
		//System.err.println("No PK found for :" + this.db_name);
		return "null";
	}

	protected String getManagementType()
	{
		if(this.type==EJB.BMP)
			return "Bean";
		else if(this.type==EJB.CMP)
			return "Container";
		else
			System.err.println("getManagementType() : " + type + " - Not Supported yet!");

		return "Unknown";
	}

	protected boolean needsPkObject()
	{
		int pk = 0;
		int fk = 0;
		for(int i=0; i<this.fields.size(); i++)
		{
			if(((FIELD)this.fields.elementAt(i)).pk)
			{
				pk++;
			}
			else if(((FIELD)this.fields.elementAt(i)).fk)
			{
				fk++;
			}
		}

		if(pk == 0 && fk != 0)
			return true;

		return false;
	}
}