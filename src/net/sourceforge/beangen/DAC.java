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

public class DAC
{
	protected String name = null;
	protected String app_package = null;
	protected String query = null;
	protected String description = "";
	protected String author = "";
	protected String transaction = "Supports";
	protected String jndi_basepath = null;

	protected Vector resources = new Vector();
	protected Vector params = new Vector();
	protected Vector columns = new Vector();

	public String getJNDIPath()
	{
		return this.jndi_basepath==null?"":(this.jndi_basepath + "/");
	}

	public String getHomePackage()
	{
		if(this.app_package == null)
			return "j2ee.home";
		else
			return this.app_package + ".j2ee.home";
	}

	public String getRemotePackage()
	{
		if(this.app_package == null)
			return "j2ee.remote";
		else
			return this.app_package + ".j2ee.remote";
	}

	public String getDACPackage()
	{
		if(this.app_package == null)
			return "j2ee.dac";
		else
			return this.app_package + ".j2ee.dac";
	}

	public String getPackage()
	{
		if(this.app_package == null)
			return "";
		else
			return this.app_package;
	}

	public String getType()
	{
		return "Session";
	}
}
