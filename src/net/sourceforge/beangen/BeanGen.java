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

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * EJB Bean Generator Tool for OpenEJB
 * @author Paulo Lopes
 */
public final class BeanGen
{
	// the collection of ejbs to generate
	private Vector ejbs = new Vector();
	private Vector dacs = new Vector();
	private Vector resources = new Vector();
	private PROJECT project = new PROJECT();
	private CONFIG config = new CONFIG();

	// line feed
	private static String lf = System.getProperty("line.separator");
	private static final String DOLLAR = "$";
	// version
	public static final String VERSION = "0.9 - Fluorine";

	// generates a simple header for all classes
	// it's a simple copyright message compatible with open source
	private String getHeader()
	{
		StringBuffer code = new StringBuffer();

		code.append("/**" + lf);
		code.append(" * License Grant-" + lf);
		code.append(" * " + lf);
		code.append(" * Permission to use, copy, modify, and distribute this Software and its " + lf);
		code.append(" * documentation for NON-COMMERCIAL or COMMERCIAL purposes and without fee is " + lf);
		code.append(" * hereby granted.  " + lf);
		code.append(" * " + lf);
		code.append(" * This Software is provided \"AS IS\".  All express warranties, including any " + lf);
		code.append(" * implied warranty of merchantability, satisfactory quality, fitness for a " + lf);
		code.append(" * particular purpose, or non-infringement, are disclaimed, except to the" + lf);
		code.append(" * extent that such disclaimers are held to be legally invalid." + lf);
		code.append(" * " + lf);

		return code.toString();
	}

	// Generate the web.xml file for the webapp
	// it won't generate the full file, just the needed part to enable JNDI work
	// and be available under the full webapp
	private String tomcat_webapp_web_xml()
	{
		FIELD field = null;
		EJB ejb = null;
		DAC dac = null;

		StringBuffer code = new StringBuffer();

		// base for every web.xml file
		code.append(
			"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + lf +
			"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">" + lf +
			"<web-app>" + lf + lf
		);

		// for each bean we need to have a ejb-ref entry in web.xml for the webapp
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			// define the ejb-ref
			code.append(
				"  <ejb-ref>" + lf +
				"    <description>EJB reference to bean " + TextUtils.toSunClassName(ejb.name) + "</description>" + lf +
				"    <ejb-ref-name>ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "</ejb-ref-name>" + lf +
				"    <ejb-ref-type>" + ejb.getType() + "</ejb-ref-type>" + lf +
				"    <home>" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home</home>" + lf +
				"    <remote>" + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote</remote>" + lf +
				"  </ejb-ref>" + lf + lf
			);

		}

		// for each bean we need to have a ejb-ref entry in web.xml for the webapp
		for(int i=0; i<dacs.size(); i++)
		{
			dac = (DAC)dacs.elementAt(i);

			// define the ejb-ref
			code.append(
				"  <ejb-ref>" + lf +
				"    <description>EJB reference to DAC bean " + TextUtils.toSunClassName(dac.name) + "</description>" + lf +
				"    <ejb-ref-name>dac/" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "</ejb-ref-name>" + lf +
				"    <ejb-ref-type>" + dac.getType() + "</ejb-ref-type>" + lf +
				"    <home>" + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home</home>" + lf +
				"    <remote>" + dac.getRemotePackage() + "." + TextUtils.toSunClassName(dac.name) + "Remote</remote>" + lf +
				"  </ejb-ref>" + lf + lf
			);

		}
		code.append("</web-app>" + lf);
		return code.toString();
	}

	// Generates the server.xml portion for tomcat conf
	// Although we have the web.xml part in order to enable the JNDI in the webapp
	// it must be defined in server.xml
	private String tomcat_server_xml()
	{
		FIELD field = null;
		EJB ejb = null;
		DAC dac = null;

		StringBuffer code = new StringBuffer();

		// for each ejb we need to create an entry Ejb with the type and Home/Remote
		// Interfaces and the factory information
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			// server.xml
			code.append(
				"<Ejb name=\"ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\" type=\"" + ejb.getType() + "\" home=\"" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home\" remote=\"" + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote\" />" + lf +
				"<ResourceParams name=\"ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\">" + lf +
				"  <parameter>" + lf +
				"    <name>factory</name>" + lf +
				"    <value>org.openejb.client.TomcatEjbFactory</value>" + lf +
				"  </parameter>" + lf +
				"  <parameter>" + lf +
				"    <name>openejb.naming.factory.initial</name>" + lf +
				"    <value>org.openejb.client.LocalInitialContextFactory</value>" + lf +
				"  </parameter>" + lf +
				"  <parameter>" + lf +
				"    <name>openejb.ejb-link</name>" + lf +
				"    <value>" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "</value>" + lf +
				"  </parameter>" + lf +
				"</ResourceParams>" + lf
			);
		}

		for(int i=0; i<dacs.size(); i++)
		{
			dac = (DAC)dacs.elementAt(i);

			// server.xml
			code.append(
				"<Ejb name=\"dac/" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "\" type=\"" + dac.getType() + "\" home=\"" + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home\" remote=\"" + dac.getRemotePackage() + "." + TextUtils.toSunClassName(dac.name) + "Remote\" />" + lf +
				"<ResourceParams name=\"dac/" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "\">" + lf +
				"  <parameter>" + lf +
				"    <name>factory</name>" + lf +
				"    <value>org.openejb.client.TomcatEjbFactory</value>" + lf +
				"  </parameter>" + lf +
				"  <parameter>" + lf +
				"    <name>openejb.naming.factory.initial</name>" + lf +
				"    <value>org.openejb.client.LocalInitialContextFactory</value>" + lf +
				"  </parameter>" + lf +
				"  <parameter>" + lf +
				"    <name>openejb.ejb-link</name>" + lf +
				"    <value>" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "</value>" + lf +
				"  </parameter>" + lf +
				"</ResourceParams>" + lf
			);
		}

		return code.toString();
	}

	// Generate the ejb-jar.xml
	// When we make a jar with all out beans, we need to generate a descriptor
	// sometime we use some tools to help generate these descriptor, but since
	// we're already generating code why not generate the descritor too?
	private String ejb_jar_xml()
	{
		FIELD field = null;
		EJB ejb = null;
		DAC dac = null;

		StringBuffer code = new StringBuffer();
		code.append(
			"<?xml version=\"1.0\"?>" + lf +
			"<ejb-jar>" + lf +
			"  <description>Auto Generated EJB Descriptor, please Validate</description>" + lf +
			"  <enterprise-beans>" + lf
		);

		// Each bean need a entry in this descriptor
		// right now only BMP/CMP are being generated i'll add Session beans later
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			if(ejb.type==EJB.BMP || ejb.type==EJB.CMP)
			{
				code.append(
					"    <entity>" + lf +
					"       <ejb-name>" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "</ejb-name>" + lf +
					"       <home>" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home</home>" + lf +
					"       <remote>" + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote</remote>" + lf +
					"       <ejb-class>" + ejb.getEJBPackage() + "." + TextUtils.toSunClassName(ejb.name) + "EJB</ejb-class>" + lf +
					"       <persistence-type>" + ejb.getManagementType() + "</persistence-type>" + lf
				);
				// sql types are managed from jdbc driver so it's a better choice than FieldType
				if(ejb.needsPkObject())
					code.append("       <prim-key-class>" + ejb.getPkPackage() + "." + TextUtils.toSunClassName(ejb.name + "_p_k") + "</prim-key-class>" + lf);
				else
					code.append("       <prim-key-class>" + TextUtils.toSQLType(ejb.getPkType()) + "</prim-key-class>" + lf);

				if(ejb.type == EJB.CMP)	// CMP
				{
					if(!ejb.needsPkObject())
						code.append("       <primkey-field>" + TextUtils.toSunMethodName(ejb.getPkName()) + "</primkey-field>" + lf);
				}

				code.append("       <reentrant>False</reentrant>" + lf);

				if(ejb.type == EJB.CMP)	// CMP
				{
					for(int j=0; j<ejb.fields.size(); j++)
					{
						field = (FIELD)ejb.fields.elementAt(j);
						code.append("       <cmp-field><field-name>" + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "</field-name></cmp-field>" + lf);
					}
				}

				// Define resources:
				for(int j=0; j<ejb.resources.size(); j++)
				{
					RESOURCE res = (RESOURCE) ejb.resources.elementAt(j);
					code.append(
					"       <resource-ref>" + lf +
					"           <res-ref-name>" + res.jndi + "</res-ref-name>" + lf +
					"           <res-type>" + res.getResourceType() + "</res-type>" + lf +
					"           <res-auth>Container</res-auth>" + lf +
					"       </resource-ref>" + lf
					);
				}
				code.append("     </entity>" + lf);
			}
			else if(ejb.type==EJB.POJO)
			{
				code.append(
					"    <session>" + lf +
					"      <ejb-name>" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "</ejb-name>" + lf +
					"      <home>" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home</home>" + lf +
					"      <remote>" + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote</remote>" + lf +
					"      <ejb-class>" + ejb.getEJBPackage() + "." + TextUtils.toSunClassName(ejb.name) + "EJB</ejb-class>" + lf +
					"      <session-type>Stateless</session-type>" + lf +
					"      <transaction-type>Container</transaction-type>" + lf
				);

				// Define resources:
				for(int j=0; j<ejb.resources.size(); j++)
				{
					RESOURCE res = (RESOURCE) ejb.resources.elementAt(j);
					code.append(
					"       <resource-ref>" + lf +
					"           <res-ref-name>" + res.jndi + "</res-ref-name>" + lf +
					"           <res-type>" + res.getResourceType() + "</res-type>" + lf +
					"           <res-auth>Container</res-auth>" + lf +
					"       </resource-ref>" + lf
					);
				}
				code.append("    </session>" + lf);
			}
		}

		for(int i=0; i<dacs.size(); i++)
		{
			dac = (DAC)dacs.elementAt(i);

			code.append(
				"    <session>" + lf +
				"      <ejb-name>" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "</ejb-name>" + lf +
				"      <home>" + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home</home>" + lf +
				"      <remote>" + dac.getRemotePackage() + "." + TextUtils.toSunClassName(dac.name) + "Remote</remote>" + lf +
				"      <ejb-class>" + dac.getDACPackage() + "." + TextUtils.toSunClassName(dac.name) + "DAC</ejb-class>" + lf +
				"      <session-type>Stateless</session-type>" + lf +
				"      <transaction-type>Container</transaction-type>" + lf
			);
			// Define resources:
			for(int j=0; j<dac.resources.size(); j++)
			{
				RESOURCE res = (RESOURCE) dac.resources.elementAt(j);
				code.append(
				"       <resource-ref>" + lf +
				"           <res-ref-name>" + res.jndi + "</res-ref-name>" + lf +
				"           <res-type>" + res.getResourceType() + "</res-type>" + lf +
				"           <res-auth>Container</res-auth>" + lf +
				"       </resource-ref>" + lf
				);
			}
			code.append("    </session>" + lf);
		}
		code.append(
			"  </enterprise-beans>" + lf +
			"  <assembly-descriptor>" + lf
		);
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);
			// I also need some way to handle the Transactions...
			code.append(
				"    <container-transaction>" + lf +
				"      <method>" + lf +
				"        <ejb-name>" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "</ejb-name>" + lf +
				"        <method-name>*</method-name>" + lf +
				"      </method>" + lf +
				"      <trans-attribute>" + ejb.transaction + "</trans-attribute>" + lf +
				"    </container-transaction>" + lf
			);
		}
		for(int i=0; i<dacs.size(); i++)
		{
			dac = (DAC)dacs.elementAt(i);

			code.append(
				"    <container-transaction>" + lf +
				"      <method>" + lf +
				"        <ejb-name>" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "</ejb-name>" + lf +
				"        <method-name>*</method-name>" + lf +
				"      </method>" + lf +
				"      <trans-attribute>" + dac.transaction + "</trans-attribute>" + lf +
				"    </container-transaction>" + lf
			);
		}
		code.append(
			"  </assembly-descriptor>" + lf +
			"</ejb-jar>" + lf
		);

		return code.toString();
	}

	// generates a default openejb.conf file
	private String openejb_conf_xml()
	{
		StringBuffer code = new StringBuffer();
		EJB ejb = null;
		RESOURCE res = null;

		// know how many beans we have for each kind
		// if the count is > 0 then define the container
		int cmp = 0;
		int bmp = 0;
		int statefull = 0;
		int stateless = 0;

		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);
			if(ejb.type == EJB.BMP) bmp++;
			if(ejb.type == EJB.CMP) cmp++;
			if(ejb.type == EJB.POJO) stateless++;
		}

		for(int i=0; i<dacs.size(); i++)
		{
			stateless++;
		}

		code.append(
			"<?xml version=\"1.0\"?>" + lf +
			"<openejb>" + lf
		);

		if(cmp > 0)
			code.append(
				"  <Container id=\"" + project.getName() + " CMP Container\" ctype=\"CMP_ENTITY\">" + lf +
				"    PoolSize 100" + lf +
				"    Global_TX_Database  " + config.openejb_home + "/conf/cmp_global_database.xml" + lf +
				"    Local_TX_Database   " + config.openejb_home + "/conf/cmp_local_database.xml" + lf +
				"  </Container>" + lf + lf
			);

		if(bmp > 0)
			code.append(
				"  <Container id=\"" + project.getName() + " BMP Container\" ctype=\"BMP_ENTITY\" />" + lf + lf
			);

		if(statefull > 0)
			code.append(
				"  <Container id=\"" + project.getName() + " STATEFULL Container\" ctype=\"STATEFUL\">" + lf +
				"    Passivator   org.openejb.core.stateful.SimplePassivater" + lf +
				"    # Passivator   org.openejb.core.stateful.RAFPassivater" + lf +
				"    TimeOut  20" + lf +
				"    PoolSize  100" + lf +
				"    BulkPassivate  50" + lf +
				"  </Container>" + lf + lf
			);

		if(stateless > 0)
			code.append(
				"  <Container id=\"" + project.getName() + " STATELESS Container\" ctype=\"STATELESS\">" + lf +
				"    StrictPooling  true" + lf +
				"  </Container>" + lf + lf
			);

		code.append(
			"  <Deployments jar=\"" + config.openejb_home + "/lib/" + project.unix_name + "_j2ee.jar\" />" + lf + lf
		);

		// define all connector we have
		for(int i=0; i<resources.size(); i++)
		{
			res = (RESOURCE)resources.elementAt(i);
			if(res.type == RESOURCE.DATASOURCE)
				code.append(
					"  <Connector id=\"" + project.getName() + " JDBC Database (" + res.name + ")\">" + lf +
					"    JdbcDriver # your JDBC driver should go here..." + lf +
					"    JdbcUrl # your JDBC url string goes here" + lf +
					"    UserName username" + lf +
					"    Password password" + lf +
					"  </Connector>" + lf + lf
				);
		}

		// found this in the sample config file. When loading is says that security
		// is deprecated but when i remove it doesn't start... weird
		code.append(
			"  <ProxyFactory id=\"Default JDK 1.3 ProxyFactory\" />" + lf +
			"  <SecurityService id=\"Default Security Service\" />" + lf +
			"  <TransactionService id=\"Default Transaction Manager\" />" + lf +
			"</openejb>" + lf
		);

		return code.toString();
	}

	// generates a default cmp_global_database.xml file
	private String cmp_global_database_xml()
	{
		StringBuffer code = new StringBuffer();
		EJB ejb = null;

		int cmp = 0;

		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);
			if(ejb.type == EJB.CMP) cmp++;
		}

		code.append(
			"<?xml version=\"1.0\"?>" + lf
		);

		if(cmp > 0)
		{
			RESOURCE res = null;
			// get the first DataSource resource
			for(int i=0; i<resources.size(); i++)
			{
				res = (RESOURCE)resources.elementAt(i);
				if(res.type == RESOURCE.DATASOURCE)
				{
					break;
				}
				if(i == resources.size()-1)
					System.err.println("DATASOURCE resource not found!");
			}

			code.append(
				"<database name=\"Global_TX_Database\" engine=\"" + res.engine + "\">" + lf +
				"  <jndi name=\"java:comp/env/" + res.jndi + "\" />" + lf +
				"  <mapping href=\"" + config.openejb_home + "/conf/cmp_or_mapping.xml\" />" + lf +
				"</database>" + lf
			);
		}

		return code.toString();
	}

	// generate the cmp_or_mapping.xml file
	private String cmp_or_mapping_xml()
	{
		StringBuffer code = new StringBuffer();
		EJB ejb = null;
		RESOURCE res = null;
		FIELD field = null;

		code.append("<?xml version=\"1.0\"?>" + lf);

		code.append("<mapping>" + lf);

		// get the first DataSource resource
		for(int i=0; i<resources.size(); i++)
		{
			res = (RESOURCE)resources.elementAt(i);
			if(res.type == RESOURCE.DATASOURCE)
			{
				break;
			}
			if(i == resources.size()-1)
				System.err.println("DATASOURCE resource not found!");
		}

		// now the fun! generate the mappings (only cmp)
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			if(ejb.type == EJB.CMP)
			{
				// define wich fields are the keys for a table
				String identity = null;
				for(int j=0; j<ejb.fields.size(); j++)
				{
					field = (FIELD)ejb.fields.elementAt(j);
					if(field.pk)
					{
						if(identity == null)
							identity = TextUtils.toSunMethodName(field.name + (field.fk?"_id":""));
						else
							identity += " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":""));
					}
				}

				if(identity == null)
				{
					// no pk defined so, assume all fk's compose the pk
					for(int j=0; j<ejb.fields.size(); j++)
					{
						field = (FIELD)ejb.fields.elementAt(j);
						if(field.fk)
						{
							if(identity == null)
								identity = TextUtils.toSunMethodName(field.name + (field.fk?"_id":""));
							else
								identity += " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":""));
						}
					}
				}

				if(identity == null) identity = "";

				// define the object and the related table
				code.append(
					"  <class name=\"" + ejb.getEJBPackage() + "." + TextUtils.toSunClassName(ejb.name) + "EJB\" identity=\"" + identity + "\"" +
						((identity.indexOf(" ")==-1)?(" key-generator=\"" + res.key_gen + "\""):("")) + ">" + lf +
					"    <description>" + TextUtils.toSunClassName(ejb.name) + " definition</description>" + lf +
					"    <map-to table=\"" + ejb.db_name + "\" />" + lf
				);

				// map the fields
				// i'm trying to make it the most castor compatible
				for(int j=0; j<ejb.fields.size(); j++)
				{
					field = (FIELD)ejb.fields.elementAt(j);
					code.append(
						"    <field name=\"" + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "\" type=\"" + field.type + "\" direct=\"false\">" + lf +
						"      <sql name=\"" + field.db_name + "\" type=\"" + field.db_type + "\"" + (field.pk?"":" dirty=\"check\"") + " />" + lf +
						"    </field>" + lf
					);
				}

				// collections have been removed, they will be handled by tyer 2
				code.append("  </class>" + lf + lf);
			}
		}
		code.append("</mapping>" + lf);
		return code.toString();
	}

	// generate cmp_local_database.xml file
	private String cmp_local_database_xml()
	{
		StringBuffer code = new StringBuffer();
		EJB ejb = null;
		RESOURCE res = null;

		int cmp = 0;

		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);
			if(ejb.type == EJB.CMP) cmp++;
		}

		code.append(
			"<?xml version=\"1.0\"?>" + lf
		);

		if(cmp > 0)
		{
			// get the first DataSource resource
			for(int i=0; i<resources.size(); i++)
			{
				res = (RESOURCE)resources.elementAt(i);
				if(res.type == RESOURCE.DATASOURCE)
				{
					break;
				}
				if(i == resources.size()-1)
					System.err.println("DATASOURCE resource not found!");
			}

			code.append(
				"<database name=\"Local_TX_Database\" engine=\"" + res.engine + "\">" + lf +
				"  <driver class-name=\" # your JDBC driver class goes here \" url=\" # your JDBC url goes here\">" + lf +
				"    <param name=\"user\" value=\"username\" />" + lf +
				"    <param name=\"password\" value=\"password\" />" + lf +
				"  </driver>" + lf +
				"  <mapping href=\"" + config.openejb_home + "/conf/cmp_or_mapping.xml\" />" + lf +
				"</database>" + lf
			);
		}

		return code.toString();
	}

	// this is a first approach to auto-deploy
	private String openejb_jar_xml()
	{
		StringBuffer code = new StringBuffer();
		EJB ejb = null;
		FIELD field = null;
		RESOURCE res = null;

		code.append(
			"<?xml version=\"1.0\" ?>" + lf +
			"<openejb-jar xmlns=\"http://www.openejb.org/openejb-jar/1.1\">" + lf
		);

		// for each bean generate the descriptor
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			code.append(
				"  <ejb-deployment ejb-name=\"" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\" deployment-id=\"" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\" container-id=\""
			);

			if(ejb.type == EJB.CMP)
				code.append(project.getName() + " CMP Container");
			else if(ejb.type == EJB.BMP)
				code.append(project.getName() + " BMP Container");
			else if(ejb.type == EJB.POJO)
				code.append(project.getName() + " STATELESS Container");

			code.append("\" >" + lf);

			for(int j=0; j<ejb.resources.size(); j++)
			{
				res = (RESOURCE) ejb.resources.elementAt(j);
				if(res.type == RESOURCE.DATASOURCE)
				{
					code.append(
						"    <resource-link res-ref-name=\"" + res.jndi + "\" res-id=\"" + project.getName() + " JDBC Database (" + res.name + ")\" />" + lf
					);
					break;
				}
				if(j == ejb.resources.size()-1)
					System.err.println("DATASOURCE resource not found!");
			}

			// findAll
			code.append(
				"    <query>" + lf +
				"      <query-method>" + lf +
				"        <method-name>findAll</method-name>" + lf +
				"        <method-params />" + lf +
				"      </query-method>" + lf +
				"      <object-ql>SELECT o FROM " + ejb.getEJBPackage() + "." + TextUtils.toSunClassName(ejb.name) + "EJB o</object-ql>" + lf +
				"    </query>" + lf
			);

			// collections
			// add 1 to Many RellationShips
			EJB ref_ejb = null;
			COLLECTION col = null;

			// search all other beans for references to this bean in theirs collections
			for(int j=0; j<ejbs.size(); j++)
			{
				ref_ejb = (EJB)ejbs.elementAt(j);
				for(int k=0; k<ref_ejb.collections.size(); k++)
				{
					col = (COLLECTION)ref_ejb.collections.elementAt(k);

					// found a 1 to Many RelationShip
					if(ejb.name.equals(col.obj_name))
					{
						boolean found = false;
						code.append(
							"    <query>" + lf +
							"      <query-method>" + lf
						);

						// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
//						if(ejb.name.equals(col.getAlias()))
//						{
							// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
							for(int a=0; a<ref_ejb.fields.size(); a++)
							{
								field = (FIELD)ref_ejb.fields.elementAt(a);
								if(field.fk &&
									(
										field.db_name.equals(ejb.getPkDbName()) || field.db_alias.equals(ejb.getPkDbName())
									)
								)
								{
									code.append("        <method-name>findBy" + TextUtils.toSunClassName(field.name) + "</method-name>" + lf);
									found = true;
									break;
								}
							}
							if(!found)
								code.append("        <method-name>findBy" + TextUtils.toSunClassName(ref_ejb.name) + "</method-name>" + lf);
//						}
//						else
//							code.append("        <method-name>findBy" + TextUtils.toSunClassName(col.getAlias()) + "</method-name>" + lf);

						code.append(
							"        <method-params>" + lf +
							"          <method-param xmlns=\"http://www.openejb.org/ejb-jar/1.1\">" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + "</method-param>" + lf +
							"        </method-params>" + lf +
							"      </query-method>" + lf +
							"      <object-ql>SELECT o FROM " + ejb.getEJBPackage() + "." + TextUtils.toSunClassName(ejb.name) + "EJB o WHERE o." + TextUtils.toSunMethodName(ref_ejb.name + "_id") + " = $1</object-ql>" + lf +
							"    </query>" + lf
						);
					}
				}
			}
			code.append("  </ejb-deployment>" + lf);
		}

		// for each bean generate the descriptor
		DAC dac = null;
		for(int i=0; i<dacs.size(); i++)
		{
			dac = (DAC)dacs.elementAt(i);

			code.append(
				"  <ejb-deployment ejb-name=\"" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "\" deployment-id=\"" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "\" container-id=\""
			);

			code.append(project.getName() + " STATELESS Container");
			code.append("\" >" + lf);

			for(int j=0; j<dac.resources.size(); j++)
			{
				res = (RESOURCE) dac.resources.elementAt(j);
				if(res.type == RESOURCE.DATASOURCE)
				{
					code.append(
						"    <resource-link res-ref-name=\"" + res.jndi + "\" res-id=\"" + project.getName() + " JDBC Database (" + res.name + ")\" />" + lf
					);
					break;
				}
				if(j == dac.resources.size()-1)
					System.err.println("DATASOURCE resource not found!");
			}
			code.append("  </ejb-deployment>" + lf);
		}

		code.append(
			"</openejb-jar>" + lf
		);

		return code.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Generates a simple SQL script for MySQL with no constrains and no Foreign
	 * Keys, only Identities are managed
	 */
	private String sql_model()
	{
		StringBuffer code = new StringBuffer();
		EJB ejb = null;
		FIELD field = null;

		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);
			code.append("CREATE TABLE " + ejb.db_name + " (" + lf);
			for(int j=0;j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				code.append("    " + field.db_name + " " + field.db_type);
				if(!field.nullable) code.append(" NOT NULL");
				if(field.pk) code.append(" AUTO_INCREMENT");
				if(j != ejb.fields.size()-1) code.append("," + lf);
			}
			if("null".equals(ejb.getPkDbName())) code.append(lf);
			else
			{
				code.append("," + lf);
				code.append("    PRIMARY KEY(" + ejb.getPkDbName() + ")" + lf);
			}
			code.append(");" + lf + lf);
		}

		return code.toString();
	}


	private String pk(EJB ejb)
	{
		StringBuffer code = new StringBuffer();
		FIELD field = null;
		// Home Interface
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getPkPackage() + ";" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getPkPackage() + "." + TextUtils.toSunClassName(ejb.name) + "PK" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + "PK implements java.io.Serializable {" + lf + lf);

		StringBuffer sb = new StringBuffer();
		int fks = 0;
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.ck)
			{
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name) + ";" + lf);
				sb.append(TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name) + ", ");
				fks++;
			}
		}

		if(sb.length()>0)
			sb.setLength(sb.length() - 2);

 		code.append(lf);
 		code.append("    public " + TextUtils.toSunClassName(ejb.name) + "PK(" + sb.toString() + ") {" + lf);
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.ck)
				code.append("        this." + TextUtils.toSunMethodName(field.name) + " = " + TextUtils.toSunMethodName(field.name) + ";" + lf);
		}
 		code.append("    }" + lf + lf);
 		code.append("    public " + TextUtils.toSunClassName(ejb.name) + "PK() {}" + lf + lf);

 		code.append("    public int hashCode() {" + lf);
 		code.append("        // return a hash code here - the one supplied" + lf);
 		code.append("        // below may not be optimal for this bean" + lf);
 		code.append("        return (int)(" + lf);

 		int tmp_fks = 0;
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.ck)
			{
				if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
 				{
 					if("int".equals(TextUtils.toJavaFieldType(field.type)))
 					{
 						tmp_fks++;
 						code.append("            (this." + TextUtils.toSunMethodName(field.name) + ") ");
 						if(tmp_fks == fks)
 							code.append(");" + lf);
 						else
 							code.append("*" + lf);
 					}
 					else
 					{
 						tmp_fks++;
 						code.append("            ((int) this." + TextUtils.toSunMethodName(field.name) + ") ");
 						if(tmp_fks == fks)
 							code.append(");" + lf);
 						else
 							code.append("*" + lf);
 					}
 				}
 				else
 				{
					tmp_fks++;
					code.append("            this." + TextUtils.toSunMethodName(field.name) + ".hashCode() ");
					if(tmp_fks == fks)
						code.append(");" + lf);
					else
						code.append("*" + lf);
 				}
 			}
 		}
 		code.append("    }" + lf + lf);

 		code.append("    public boolean equals(Object o) {" + lf);
 		code.append("        if(!(o instanceof " + TextUtils.toSunClassName(ejb.name) + "PK)) {" + lf);
 		code.append("            return false;" + lf);
 		code.append("        }" + lf);
 		code.append("        " + TextUtils.toSunClassName(ejb.name) + "PK o2 = (" + TextUtils.toSunClassName(ejb.name) + "PK) o;" + lf);
 		code.append("        return (" + lf);
 		tmp_fks = 0;
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.ck)
			{
				if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
 				{
					tmp_fks++;
					code.append("            (this." + TextUtils.toSunMethodName(field.name) + " == o2."  + TextUtils.toSunMethodName(field.name) + ")");
					if(tmp_fks == fks)
						code.append(");" + lf);
					else
						code.append(" && " + lf);
 				}
 				else
 				{
					tmp_fks++;
					code.append("            this." + TextUtils.toSunMethodName(field.name) + ".equals(o2."  + TextUtils.toSunMethodName(field.name) + ")");
					if(tmp_fks == fks)
						code.append(");" + lf);
					else
						code.append(" && " + lf);
 				}
 			}
 		}
 		code.append("    }" + lf + lf);

 		code.append("    public String toString() {" + lf);
 		code.append("        return (" + lf);
 		tmp_fks = 0;
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.ck)
			{
				if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
 				{
					tmp_fks++;
					code.append("            String.valueOf(this." + TextUtils.toSunMethodName(field.name) + ")");
					if(tmp_fks == fks)
						code.append(");" + lf);
					else
						code.append(" + \"/\" + " + lf);
 				}
 				else
 				{
					tmp_fks++;
					code.append("            this." + TextUtils.toSunMethodName(field.name) + ".toString()");
					if(tmp_fks == fks)
						code.append(");" + lf);
					else
						code.append(" + \"/\" + " + lf);
 				}
 			}
 		}
 		code.append("    }" + lf + lf);
 		code.append("}" + lf);

 		return code.toString();
	}

	// Generate the home interface for our EJB
	// Home interfaces are interfaces that let us access the ejb beans
	// usually they only define create methods and find methods
	private String home_interface(EJB ejb)
	{
		FIELD field = null;
		StringBuffer code = new StringBuffer();

		// Home Interface
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getHomePackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("import java.util.Collection;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.EJBHome;" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public interface " + TextUtils.toSunClassName(ejb.name) + "Home extends EJBHome {" + lf + lf);

		if(ejb.type != EJB.POJO)
		{
			// get all not null fields and force them into the create method
			StringBuffer create_params = new StringBuffer();

			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				if(!field.nullable && !field.pk)
					create_params.append( TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
			}

			// cut the extra ', ' chars
			if(create_params.length() > 0)
				create_params.setLength( create_params.length() - 2 );

			code.append("    /**" + lf);
			code.append("     * Creates a proxy object for " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			// define the javadoc
			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				if(!field.nullable && !field.pk)
					code.append("     * @param " + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " " + lf);
			}
			code.append("     * @return Remote interface for proxy " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
			code.append("     */" + lf);
			code.append("    public " + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote create(" + create_params.toString() +") throws CreateException, RemoteException;" + lf + lf);

			// ready made methods for all beans
			code.append("    /**" + lf);
			code.append("     * Finds an entry in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			code.append("     * @param key Primary Key that identifies the entry" + lf);
			code.append("     * @return Remote interface for proxy " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
			code.append("     */" + lf);
			code.append("    public " + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote findByPrimaryKey(" + TextUtils.toJavaObjectFieldType(ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);

			code.append("    /**" + lf);
			code.append("     * Finds all entries in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			code.append("     * @return Collection of interfaces for proxy " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
			code.append("     */" + lf);
			code.append("    public Collection findAll() throws FinderException, RemoteException;" + lf + lf);

			EJB ref_ejb = null;
			for(int i=0; i<ejbs.size(); i++)
			{
				COLLECTION col = null;
				// add 1 to Many RellationShips
				ref_ejb = (EJB)ejbs.elementAt(i);
				for(int j=0; j<ref_ejb.collections.size(); j++)
				{
					col = (COLLECTION)ref_ejb.collections.elementAt(j);
					// found a 1 to Many RelationShip
					if(ejb.name.equals(col.obj_name))
					{
						boolean found = false;
						code.append("    /**" + lf);
						code.append("     * Finds all entries in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
						code.append("     * Where " + TextUtils.toSunClassName(ref_ejb.name) + " is the parameter used in query" + lf);
						code.append("     * @param key parameter to use in Query" + lf);
						code.append("     * @return Collection of interfaces for proxy " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
						code.append("     */" + lf);
						// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
						if(ejb.name.equals(col.getAlias()))
						{
							for(int k=0; k<ref_ejb.fields.size(); k++)
							{
								field = (FIELD)ref_ejb.fields.elementAt(k);
								if(field.fk &&
									(
										field.db_name.equals(ejb.getPkDbName()) || field.db_alias.equals(ejb.getPkDbName())
									)
								)
								{
									code.append("// " + col.getAlias() + " - " + field.name + " - " + ref_ejb.name + lf);
									code.append("    public Collection findBy" + TextUtils.toSunClassName(field.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);
									found = true;
									break;
								}
							}
							if(!found)
							{
								code.append("// " + col.getAlias() + " - " + field.name + " - " + ref_ejb.name + lf);
								code.append("    public Collection findBy" + TextUtils.toSunClassName(ref_ejb.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);
							}
						}
						else
						{
							code.append("// " + col.getAlias() + " - " + field.name + " - " + ref_ejb.name + lf);
							code.append("    public Collection findBy" + TextUtils.toSunClassName(col.getAlias()) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);
						}
					}
				}
			}
		}
		else
		{
			code.append("    /**" + lf);
			code.append("     * Creates a proxy object for " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			code.append("     */" + lf);
			code.append("    public " + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name + "_remote") + " create() throws CreateException, RemoteException;" + lf);
		}

		code.append("}" + lf);

		return code.toString();
	}

	private String home_interface(DAC dac)
	{
		PARAM field = null;
		StringBuffer code = new StringBuffer();

		// Home Interface
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + dac.getHomePackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.EJBHome;" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home" + lf);
		code.append(" * " + lf);
		code.append(" * " + dac.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + dac.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public interface " + TextUtils.toSunClassName(dac.name) + "Home extends EJBHome {" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Creates a proxy object for " + TextUtils.toSunClassName(dac.name) + lf);
		code.append("     * " + lf);
		code.append("     * @return Remote interface for proxy " + TextUtils.toSunClassName(dac.name) + "Remote" + lf);
		code.append("     */" + lf);
		code.append("    public " + dac.getRemotePackage() + "." + TextUtils.toSunClassName(dac.name) + "Remote create() throws CreateException, RemoteException;" + lf + lf);
		code.append("}" + lf);

		return code.toString();
	}

	// Generate the remote interface
	// Remote interface lets us access the underlaying data for this bean
	private String remote_interface(EJB ejb)
	{
		FIELD field = null;
		StringBuffer code = new StringBuffer();

		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getRemotePackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.EJBObject;" + lf);
		if(ejb.type == EJB.POJO)
		{
			code.append(
				"import javax.ejb.CreateException;" + lf +
				"import javax.ejb.FinderException;" + lf +
				"import javax.ejb.RemoveException;" + lf +
				"import javax.ejb.EJBException;" + lf
			);
		}
		code.append("import ejb.exception.MandatoryFieldException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public interface " + TextUtils.toSunClassName(ejb.name) + "Remote extends EJBObject {" + lf + lf);

		// setter's
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.ro)
			{
				code.append("    /**" + lf);
				code.append("     * Setter for field " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + lf);
				code.append("     * @param " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " the new value for this field" + lf);
				code.append("     */" + lf);
				if(field.nullable)
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException;" + lf);
				else
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + ( TextUtils.isNative(TextUtils.toJavaFieldType(field.type))==true?"":", MandatoryFieldException" ) + ";" + lf);
				code.append(lf);
			}
		}

		code.append("    " + lf);

		// getter's
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			code.append("    /**" + lf);
			code.append("     * Getter for field " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + (field.ro?" (READ ONLY)":"") + lf);
			code.append("     * @return the value for the field" + lf);
			code.append("     */" + lf);
			if(field.nullable)
				code.append("    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException" + (ejb.type == EJB.POJO?", EJBException":"") + ";" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException" + (ejb.type == EJB.POJO?", EJBException":"") + ";" + lf);
			code.append(lf);
		}

		/* POJO also define here all crud operations */
		if(ejb.type == EJB.POJO)
		{
			// define the create method (INSERT)
			// get all not null fields and force them into the create method
			StringBuffer create_params = new StringBuffer();

			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				if(!field.nullable && !field.pk)
					create_params.append( TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
			}

			// cut the extra ', ' chars
			if(create_params.length() > 0)
				create_params.setLength( create_params.length() - 2 );

			code.append("    /**" + lf);
			code.append("     * Creates a row in the persistent storage system of type " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			// define the javadoc
			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				if(!field.nullable && !field.pk)
					code.append("     * @param " + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " " + lf);
			}
			code.append("     */" + lf);
			code.append("    public void insert(" + create_params.toString() +") throws CreateException, RemoteException;" + lf + lf);

			// ready made methods for all beans
			code.append("    /**" + lf);
			code.append("     * Finds an entry in the storage for your JDO " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			code.append("     * @param key Primary Key that identifies the entry" + lf);
			code.append("     */" + lf);
			code.append("    public void selectByPrimaryKey(" + TextUtils.toJavaFieldType(ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);

			code.append("    /**" + lf);
			code.append("     * Finds all entries in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			code.append("     */" + lf);
			code.append("    public void selectAll() throws FinderException, RemoteException;" + lf + lf);

			code.append(
				"    /**" + lf +
				"     * Updates the current row in the rowset" + lf +
				"     */" + lf +
				"    public void update() throws EJBException, RemoteException;" + lf + lf +

				"    /**" + lf +
				"     * Deletes the current row" + lf +
				"     */" + lf +
				"    public void delete() throws RemoveException, RemoteException;" + lf + lf +

				"    /**" + lf +
				"     * Moves the pointer to the next row" + lf +
				"     *" + lf +
				"     * @return is it possible to move to next row" + lf +
				"     */" + lf +
				"    public boolean next() throws EJBException, RemoteException;" + lf + lf +

				"    /**" + lf +
				"     * Returns the size of our rowset" + lf +
				"     *" + lf +
				"     * @return size of rowset" + lf +
				"     */" + lf +
				"    public int size() throws RemoteException;" + lf + lf
			);

			EJB ref_ejb = null;
			for(int i=0; i<ejbs.size(); i++)
			{
				COLLECTION col = null;
				// add 1 to Many RellationShips
				ref_ejb = (EJB)ejbs.elementAt(i);
				for(int j=0; j<ref_ejb.collections.size(); j++)
				{
					col = (COLLECTION)ref_ejb.collections.elementAt(j);
					// found a 1 to Many RelationShip
					if(ejb.name.equals(col.obj_name))
					{
						boolean found = false;
						code.append("    /**" + lf);
						code.append("     * Finds all entries in the storage for your JDO's " + TextUtils.toSunClassName(ejb.name) + lf);
						code.append("     * Where " + TextUtils.toSunClassName(ref_ejb.name) + " is the parameter used in query" + lf);
						code.append("     * @param key parameter to use in Query" + lf);
						code.append("     */" + lf);
						// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
						if(ejb.name.equals(col.getAlias()))
						{
							for(int k=0; k<ref_ejb.fields.size(); k++)
							{
								field = (FIELD)ref_ejb.fields.elementAt(k);
								if(field.fk &&
									(
										field.db_name.equals(ejb.getPkDbName()) || field.db_alias.equals(ejb.getPkDbName())
									)
								)
								{
									code.append("    public void selectBy" + TextUtils.toSunClassName(field.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);
									found = true;
									break;
								}
							}
							if(!found)
							{
								code.append("    public void selectBy" + TextUtils.toSunClassName(ref_ejb.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);
							}
						}
						else
						{
							code.append("    public void selectBy" + TextUtils.toSunClassName(col.getAlias()) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);
						}
					}
				}
			}
		}

		code.append("}" + lf);

		return code.toString();
	}

	// Generate the remote interface
	// Remote interface lets us access the underlaying data for this bean
	private String remote_interface(DAC dac)
	{
		PARAM column = null;
		PARAM param = null;
		StringBuffer code = new StringBuffer();

		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + dac.getRemotePackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.EJBObject;" + lf);
		code.append("import ejb.exception.DACException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + dac.getRemotePackage() + "." + TextUtils.toSunClassName(dac.name) + "Remote" + lf);
		code.append(" * " + lf);
		code.append(" * " + dac.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + dac.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public interface " + TextUtils.toSunClassName(dac.name) + "Remote extends EJBObject {" + lf + lf);

		// generate the param's call
		StringBuffer create_params = new StringBuffer();
		StringBuffer create_params_names = new StringBuffer();

		for(int i=0; i<dac.params.size(); i++)
		{
			param = (PARAM)dac.params.elementAt(i);
			if(param.nullable)
				create_params.append( TextUtils.toJavaObjectFieldType(param.type) + " " + TextUtils.toSunParameterName(param.name) + ", ");
			else
				create_params.append( TextUtils.toJavaFieldType(param.type) + " " + TextUtils.toSunParameterName(param.name) + ", ");

			create_params_names.append(TextUtils.toSunParameterName(param.name) + ", ");
		}

		// cut the extra ', ' chars
		if(create_params.length() != 0)
		{
			create_params.setLength( create_params.length() - 2 );
			create_params_names.setLength( create_params_names.length() - 2 );
		}
		code.append(
			"    /**" + lf +
			"     * Executes a Use Case using the defined QUERY" + lf +
			"     */" + lf +
			"    public void execute(" + create_params.toString() + ") throws DACException, RemoteException;" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Navigates across the rowset, return false when there's no more elements" + lf);
		code.append("     * " + lf);
		code.append("     * @return true when there is a next element" + lf);
		code.append("     */" + lf);
		code.append(
			"    public boolean next() throws DACException, RemoteException;" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Number of elements in rowset" + lf);
		code.append("     * " + lf);
		code.append("     * @return number of elements in rowset" + lf);
		code.append("     */" + lf);
		code.append(
			"    public int size() throws RemoteException;" + lf + lf
		);

		// getter's
		for(int j=0; j<dac.columns.size(); j++)
		{
			column = (PARAM)dac.columns.elementAt(j);
			code.append("    /**" + lf);
			code.append("     * Getter for column " + TextUtils.toSunParameterName(column.name) + lf);
			code.append("     * @return the value for the column" + lf);
			code.append("     */" + lf);
			if(column.nullable)
				code.append("    public " + TextUtils.toJavaObjectFieldType(column.type) + " " + TextUtils.toSunMethodName("get_" + column.name) + "() throws RemoteException, DACException;" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(column.type) + " " + TextUtils.toSunMethodName("get_" + column.name) + "() throws RemoteException, DACException;" + lf);
			code.append(lf);
		}

		code.append("}" + lf);

		return code.toString();
	}

	// Out bean that implements all the interfaces we've defined above
	private String entity_bean(EJB ejb)
	{
		FIELD field = null;
		RESOURCE res = null;
		StringBuffer code = new StringBuffer();

		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getEJBPackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.util.Collection;" + lf);
		code.append("import java.util.List;" + lf);
		code.append("import java.util.Vector;" + lf);
		code.append(((ejb.type != EJB.CMP)?"":"// ") + "import java.sql.Timestamp;" + lf);
		code.append(((ejb.type != EJB.CMP)?"":"// ") + "import java.sql.Connection;" + lf);
		code.append(((ejb.type != EJB.CMP)?"":"// ") + "import java.sql.PreparedStatement;" + lf);
		code.append(((ejb.type != EJB.CMP)?"":"// ") + "import java.sql.ResultSet;" + lf);
		code.append(((ejb.type != EJB.CMP)?"":"// ") + "import java.sql.SQLException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import ejb.exception.MandatoryFieldException;" + lf);
		if(ejb.type == EJB.POJO)
			code.append("import ejb.core.SessionBeanAdapter;" + lf);
		else
			code.append("import ejb.core.EntityBeanAdapter;" + lf);
		if(ejb.type == EJB.POJO || ejb.type == EJB.POJO)
			code.append("import ejb.core.DBUtil;" + lf);
		code.append("import ejb.exception.BeanHomeFactoryException;" + lf);
		code.append("import ejb.util.BeanHomeFactory;" + lf + lf);

		code.append("import javax.ejb.EJBException;" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.DuplicateKeyException;" + lf);
		code.append("import javax.ejb.ObjectNotFoundException;" + lf);
		code.append("import javax.ejb.RemoveException;" + lf);
		code.append(((ejb.type != EJB.CMP)?"":"// ") + "import javax.sql.DataSource;" + lf);
		if(ejb.type == EJB.POJO)
		{
			code.append("import javax.sql.rowset.CachedRowSet;" + lf);
			code.append("import com.sun.rowset.CachedRowSetImpl;" + lf);
		}
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getEJBPackage() + "." + TextUtils.toSunClassName(ejb.name) + "EJB" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + "EJB extends " + (ejb.type==EJB.POJO?"Session":"Entity") + "BeanAdapter {" + lf + lf);

		// Generate all the fields
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.nullable)
			{
				code.append(
					"    /**" + lf +
					"     * The internal java object corresponding to the non-native database field " + ejb.db_name + "." + field.db_name + "because it can be null" + lf +
					"     */" + lf +
					"    " + (ejb.type==EJB.CMP?"public":"private") + " " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = null;" + lf + lf
				);
			}
			else
			{
				code.append(
					"    /**" + lf +
					"     * The internal java object corresponding to the primitive database field " + ejb.db_name + "." + field.db_name + lf +
					"     */" + lf +
					"    " + (ejb.type==EJB.CMP?"public":"private") + " " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.getInitialValueFor(TextUtils.toJavaFieldType(field.type)) + ";" + lf + lf
				);
			}

			if(ejb.type == EJB.POJO)
			{
				code.append(
					"    /**" + lf +
					"     * Dirty flag for field " + ejb.db_name + "." + field.db_name + lf +
					"     */" + lf +
					"    private boolean " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty = false;" + lf + lf
				);
			}
		}

		if(ejb.type == EJB.BMP)
		{
			// when we use a dirty flag we can reduce the ejbStore() calls
			// we could use the flag also to reduce the ejbLoad() but we can be sure
			// if the underlaying data hasn't change since the were loaded for the
			// first time
			code.append("    /**" + lf);
			code.append("     * optimize the ejbStore()" + lf);
			code.append("     * it isn't stored in a serialized form" + lf);
			code.append("     */" + lf);
			code.append("    public transient boolean dirty = false;" + lf);
			code.append(lf);
		}

		code.append("    // DataSource used to get a Connection to a Database" + lf);
		if(ejb.type == ejb.CMP)
			code.append("    // Uncomment if you need to access the DataSource from your bean" + lf);
		code.append("    " + (ejb.type == ejb.CMP?"// ":"") + "private transient DataSource dataSource = null;" + lf);
		code.append(lf);

		if(ejb.type == EJB.POJO)
		{
			code.append("    // rowset that holds all information fetched in a select" + lf);
			code.append("    private transient CachedRowSet rowset = null;" + lf + lf);
		}

		code.append("    /**" + lf);
		code.append("     * @return  the connection from the dataSource" + lf);
		code.append("     * @exception EJBException  Thrown by the method if the dataSource is not found" + lf);
		code.append("     *                          in the naming." + lf);
		code.append("     * @exception SQLException may be thrown by dataSource.getConnection()" + lf);
		code.append("     */" + lf);

		if(ejb.type == ejb.CMP)
			code.append("    /* Uncomment if you need to access the DataSource from your bean" + lf);
		code.append("    private Connection getConnection() throws EJBException, SQLException {" + lf);
		code.append("        if (dataSource == null) {" + lf);
		code.append("            // Finds DataSource from JNDI" + lf);
		code.append("            try {" + lf);

		for(int i=0; i<ejb.resources.size(); i++)
		{
			res = (RESOURCE) ejb.resources.elementAt(i);
			if(res.type == RESOURCE.DATASOURCE)
			{
				break;
			}
			if(i == ejb.resources.size()-1)
				System.err.println("DATASOURCE resource not found!");
		}

		code.append("                dataSource = (DataSource)this.initContext.lookup(\"java:comp/env/" + res.jndi + "\");" + lf);
		code.append("            } catch (javax.naming.NamingException e) {" + lf);
		code.append("                throw new EJBException(e);" + lf);
		code.append("            }" + lf);
		code.append("        }" + lf);
		code.append("        return dataSource.getConnection();" + lf);
		code.append("    }" + lf);
		if(ejb.type == ejb.CMP)
			code.append("    */" + lf);
		code.append(lf);

		// setter's
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			code.append("    /**" + lf);
 			code.append("     * Setter for field " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + lf);
 			code.append("     * @param " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " the new value for this field" + lf);
			code.append("     */" + lf);
			if(field.nullable)
				code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") {" + lf);
			else
				code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") " + ( TextUtils.isNative(TextUtils.toJavaFieldType(field.type)) == true? "":"throws MandatoryFieldException " ) + "{" + lf);
			if(ejb.type == EJB.POJO)
			{
				if(!TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
				{
					if(!field.nullable)
					{
						code.append(
							"        if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == null)" + lf +
							"            throw new MandatoryFieldException(\"field '" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + "' can't be null\");" + lf
						);
					}
				}
			}
			code.append("        this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ";" + lf);
			if(ejb.type == ejb.BMP)
				code.append("        this.dirty = true;" + lf);
			if(ejb.type == EJB.POJO)
			{
				code.append("        this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty = true;" + lf);
			}
			code.append("    }" + lf);
			code.append(lf);
		}
		code.append(lf);

		// getter's
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			code.append("    /**" + lf);
			code.append("     * Getter for field " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + lf);
			code.append("     * @return the value for the field" + lf);
			code.append("     */" + lf);
			if(field.nullable)
				code.append("    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws EJBException {" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws EJBException {" + lf);

			if(ejb.type == EJB.POJO)
			{
				code.append(
					"        try {" + lf +
					"            if(!" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + "_dirty)" + lf
				);

				if(field.nullable)
					code.append("                return (" + TextUtils.toJavaObjectFieldType(field.type) + ") rowset.getObject(\"" + field.db_name + "\");" + lf);
				else
					code.append("                return rowset." + TextUtils.getJDBCGetter(field.db_type) + "(\"" + field.db_name + "\");" + lf);

				code.append(
					"            else" + lf +
					"                // not yet commited to persistent engine" + lf +
					"                return this." + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ";" + lf +
					"        } catch(SQLException sqle) {" + lf +
					"            throw new EJBException(sqle);" + lf +
					"        }" + lf
				);
			}
			else
				code.append("        return this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + ";" + lf);
			code.append("    }" + lf);
			code.append(lf);
		}
		code.append(lf);

		// get all not null and non pk fields and force them into the create method
		StringBuffer create_params = new StringBuffer();

		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(!field.nullable && !field.pk)
				create_params.append( TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
		}

		// cut the extra ', ' chars
		if(create_params.length() != 0)
			create_params.setLength( create_params.length() - 2 );

		// home interface wrapper's

		// Create the comment for javadoc
		code.append("    /**" + lf);
		code.append("     * create a new " + ejb.name + lf);
		code.append("     * " +lf);
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(!field.nullable && !field.pk)
				code.append("     * @param " + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " " + lf);
		}
		code.append("     * " + lf);
		if(ejb.type != EJB.POJO)
		{
			code.append("     * @return " + ejb.getPkName() + " primary key" + lf);
			code.append("     *" + lf);
		}
		code.append("     */" + lf);
		if(ejb.type == EJB.POJO)
			code.append("    public synchronized void insert(" + create_params.toString() +") throws CreateException {" + lf + lf);
		else
			code.append("    public " + TextUtils.toSQLType(ejb.getPkType()) + " ejbCreate(" + create_params.toString() +") throws CreateException {" + lf + lf);

		if(ejb.type != EJB.CMP)
		{
			if(ejb.type == EJB.BMP)
				code.append("        " + TextUtils.toSQLType(ejb.getPkType()) + " pk = null;" + lf);
			else
				code.append("        " + TextUtils.toJavaFieldType(ejb.getPkType()) + " pk = " + TextUtils.getInitialValueFor(TextUtils.toJavaFieldType(ejb.getPkType())) + ";" + lf);
			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement stmt = null;" + lf);
			code.append(lf);
		}

		int cnt = 0;
		int non_native = 0;
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.nullable && !field.pk)
			{
				cnt++;
				if(!TextUtils.isNative(TextUtils.toJavaFieldType(field.type))) non_native++;
			}
		}

		if(cnt != 0)
		{
			if(non_native != 0)
				code.append("        try {" + lf);
			// setter's
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.nullable && !field.pk)
					code.append((non_native != 0?"    ":"") + "        this." + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ");" + lf);
			}
			if(non_native != 0)
			{
				code.append("        } catch(MandatoryFieldException e) {" + lf);
				code.append("            // there's a null value in a mandatory field" + lf);
				code.append("            System.err.println(\"Error in assigning values\");" + lf);
				code.append("            throw new CreateException(\"" + TextUtils.toSunClassName(ejb.name) + " Failed to assign variable - \" + e.getMessage());" + lf);
				code.append("        }" + lf);
			}
			code.append(lf);
		}
		code.append("        // your extra code here" + lf);
		StringBuffer sql_full_params = new StringBuffer();

		if(ejb.type != EJB.CMP)
		{
			code.append("        String sql_cmd = null;" + lf);
			code.append("        try {" + lf);
			code.append("            // get a connection for this transaction context" + lf);
			code.append("            conn = getConnection();" + lf);
			code.append("            " + lf);
			code.append("            // store Object state in DB" + lf);
			code.append("            int arg = 1;" + lf);

			StringBuffer sql_params = new StringBuffer();
			StringBuffer sql_params2 = new StringBuffer();

			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.nullable && !field.pk)
				{
					sql_params.append( ejb.db_name + "." + field.db_name + ", ");
					sql_full_params.append( ejb.db_name + "." + field.db_name + ", ");
					sql_params2.append("?, ");
				}
				else
					sql_full_params.append( ejb.db_name + "." + field.db_name + ", ");
			}

			if(sql_params.length() != 0)
			{
				sql_params.setLength( sql_params.length() - 2);
				sql_params2.setLength( sql_params2.length() - 2);
			}

			if(sql_full_params.length() != 0)
				sql_full_params.setLength( sql_full_params.length() - 2);

			code.append("            sql_cmd = \"INSERT INTO " + ejb.db_name + "(" + sql_params.toString() + ") VALUES(" + sql_params2.toString() + ")\";" + lf);
			code.append("            stmt = conn.prepareStatement(sql_cmd);" + lf);

			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.nullable && !field.pk)
				{
					if("setTimestamp".equals(TextUtils.getJDBCSetter(field.db_type)))
						code.append("            stmt." + TextUtils.getJDBCSetter(field.db_type) + "(arg++, new Timestamp(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ".getTime()));" + lf);
					else
						code.append("            stmt." + TextUtils.getJDBCSetter(field.db_type) + "(arg++, " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ");" + lf);
				}
			}

			code.append("            // set this variable with the PK value" + lf);
			code.append("            stmt.close();" + lf);
			code.append("            sql_cmd = (new DBUtil(DBUtil.SQLSERVER)).genId(\"" + ejb.db_name + "\");" + lf);
			code.append("            stmt = conn.prepareStatement(sql_cmd);" + lf);
			code.append("            stmt = conn.prepareStatement(sql_cmd);" + lf);
			code.append("            ResultSet rs = stmt.executeQuery();" + lf);
			code.append("            if(rs == null) {" + lf);
			code.append("                throw new CreateException(\"" + TextUtils.toSunClassName(ejb.name) + " creation failed - \" + sql_cmd);" + lf);
			code.append("            } else {" + lf);
			code.append("                if(rs.next()) {" + lf);
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(field.pk)
				{
					code.append("                    pk = rs." + TextUtils.getJDBCGetter(field.db_type) + "(1);" + lf);
					break;
				}
			}
			code.append("                    rs.close();" + lf);
			code.append("                } else {" + lf);
			code.append("                    rs.close();" + lf);
			code.append("                    throw new CreateException(\"" + TextUtils.toSunClassName(ejb.name) + " Failed to get last inserted ID - \" + sql_cmd);" + lf);
			code.append("                }" + lf);
			code.append("            }" + lf);
			code.append("        } catch (SQLException ex) {" + lf);
			code.append("            throw new CreateException(\"" + TextUtils.toSunClassName(ejb.name) + " Failed to add to database - \" + sql_cmd + \" - \" + ex.getMessage());" + lf);
			code.append("        } finally {" + lf);
			code.append("            // Always make sure result sets and statements are closed," + lf);
			code.append("            // and the connection is returned to the pool" + lf);
			code.append("            if( stmt != null) {" + lf);
			code.append("                try { stmt.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                stmt = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( conn != null) {" + lf);
			code.append("                try { conn.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                conn = null;" + lf);
			code.append("            }" + lf);
			code.append("        }" + lf);

			if(ejb.type != EJB.POJO)
				code.append("        return pk;" + lf);
		}
		else
			code.append("        return null;" + lf);
		code.append("    }" + lf + lf);

		if(ejb.type != ejb.CMP)
		{
			if(ejb.type == ejb.BMP)
				code.append("    public void ejbLoad() throws EJBException {" + lf);
			else
				code.append("    public void selectByPrimaryKey(" + TextUtils.toJavaFieldType(ejb.getPkType()) + " pk) throws FinderException {" + lf + lf);

			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement stmt = null;" + lf);
			code.append("        ResultSet rs = null;" + lf);
			code.append(lf);
			code.append("        try {" + lf);
			code.append("            // get a connection for this transaction context" + lf);
			if(ejb.type == ejb.BMP)
				code.append("            " + TextUtils.toJavaFieldType(ejb.getPkType()) + " pk = (" + TextUtils.toJavaFieldType(ejb.getPkType()) + ") ejbContext.getPrimaryKey();" + lf);
			code.append("            conn = getConnection();" + lf);
			code.append("            // insert in DB" + lf);
			// compound keys suck
			if(ejb.needsPkObject())
			{
				boolean fst = true;
				code.append("            String sql_cmd = \"SELECT " + sql_full_params.toString() + " FROM " + ejb.db_name + " WHERE");
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						if(fst) code.append(" "); else code.append(" AND ");
						code.append(ejb.db_name + "." + field.db_name + " = ?");
						fst = false;
					}
				}

				code.append("\";" + lf);
			}
			else
				code.append("            String sql_cmd = \"SELECT " + sql_full_params.toString() + " FROM " + ejb.db_name + " WHERE " + ejb.db_name + "." + ejb.getPkDbName() + " = ?\";" + lf);
			code.append("            int arg = 1;" + lf);
			code.append("            stmt = conn.prepareStatement( sql_cmd );" + lf);

			if(ejb.needsPkObject())
			{
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						code.append("            stmt." + TextUtils.getJDBCSetter(field.db_type) + "(arg++, pk." + TextUtils.toSunMethodName(field.name) + ");" + lf);
					}
				}
			}
			else
				code.append("            stmt." + TextUtils.getJDBCSetter(ejb.getPkType()) + "(arg++, pk);" + lf);

			if(ejb.type == EJB.POJO)
			{
            	code.append("            rowset = new CachedRowSetImpl();" + lf);
            	code.append("            rowset.populate(stmt.executeQuery());" + lf);
            	code.append("            rowset.beforeFirst();" + lf);
			}
			else
			{
				code.append("            rs = stmt.executeQuery();" + lf);
			}
			if(ejb.type == EJB.POJO)
			{
				code.append("            if (this.next())" + lf);
				code.append("                throw new FinderException(\"Failed to load bean from database\");" + lf);
			}
			else
			{
				code.append("            if (!rs.next())" + lf);
				code.append("                throw new EJBException(\"Failed to load bean from database\");" + lf);
			}
			code.append("        } catch (SQLException e) {" + lf);
			code.append("            throw new EJBException(\"Failed to load bean from database\");" + lf);
			code.append("        } finally {" + lf);
			code.append("            // Always make sure result sets and statements are closed," + lf);
			code.append("            // and the connection is returned to the pool" + lf);
			if(ejb.type == EJB.BMP)
			{
				code.append("            if( rs != null) {" + lf);
				code.append("                try { rs.close(); } catch (SQLException e) { ; }" + lf);
				code.append("                rs = null;" + lf);
				code.append("            }" + lf);
			}
			code.append("            if( stmt != null) {" + lf);
			code.append("                try { stmt.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                stmt = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( conn != null) {" + lf);
			code.append("                try { conn.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                conn = null;" + lf);
			code.append("            }" + lf);
			code.append("        }" + lf);
			code.append("    }" + lf + lf);
		}

		if(ejb.type != EJB.POJO)
		{
			code.append("    /**" + lf);
			code.append("     * Called after the creation of a new " + ejb.name + lf);
			code.append("     * " +lf);
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.nullable && !field.pk)
					code.append("     * @param " + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " " + lf);
			}
			code.append("     * " + lf);
			code.append("     */" + lf);
			code.append("    public void ejbPostCreate(" + create_params.toString() +") throws CreateException {" + lf);
			code.append("        // add post create code for your bean" + lf);
			code.append("    }" + lf + lf);
		}

		if(ejb.type == EJB.BMP)
		{
			// let's generate the code for a simple pk finder
			code.append("    /**" + lf);
			code.append("     * find a specific object from the Database using a PK" + lf);
			code.append("     * " +lf);
			code.append("     * @throws ObjectNotFoundException when there's no object with that pk" + lf);
			code.append("     * @throws FinderException when there's an error finding some object" + lf);
			code.append("     * " +lf);
			code.append("     * @param key the primary key that identifies the object " + lf);
			code.append("     * " + lf);
			code.append("     * @return " + ejb.getPkName() + " primary key" + lf);
			code.append("     *" + lf);
			code.append("     */" + lf);
			code.append("    public " + TextUtils.toSQLType(ejb.getPkType()) + " ejbFindByPrimaryKey(" + TextUtils.toSQLType(ejb.getPkType()) + " key) throws ObjectNotFoundException, FinderException {" + lf);
			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement stmt = null;" + lf);
			code.append("        ResultSet rs = null;" + lf);
			code.append("        String sql_cmd = null;" + lf);
			code.append("        try {" + lf);
			code.append("            // get a connection for this transaction context" + lf);
			code.append("            conn = getConnection();" + lf);
			code.append("            " + lf);
			code.append("            // find Object state in DB" + lf);
			code.append("            sql_cmd = \"SELECT " + ejb.db_name + "." + ejb.getPkDbName() + " FROM " + ejb.db_name + " WHERE " + ejb.db_name + "." + ejb.getPkDbName() + " = ?\";" + lf);
			code.append("            stmt = conn.prepareStatement(sql_cmd);" + lf);
			code.append("            stmt.setObject(1, key);" + lf);
			code.append("            rs = stmt.executeQuery();" + lf);
			code.append("            if (!rs.next())" + lf);
			code.append("                throw new ObjectNotFoundException();" + lf);
			code.append("        } catch (SQLException e) {" + lf);
			code.append("            throw new FinderException(\"Failed to execute query \" + e);" + lf);
			code.append("        } finally {" + lf);
			code.append("            // Always make sure result sets and statements are closed," + lf);
			code.append("            // and the connection is returned to the pool" + lf);
			code.append("            if( rs != null) {" + lf);
			code.append("                try { rs.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                rs = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( stmt != null) {" + lf);
			code.append("                try { stmt.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                stmt = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( conn != null) {" + lf);
			code.append("                try { conn.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                conn = null;" + lf);
			code.append("            }" + lf);
			code.append("        }" + lf);
			code.append("        return key;" + lf);
			code.append("    }" + lf + lf);

			code.append("    /**" + lf);
			code.append("     * find all objects from the Database" + lf);
			code.append("     * " +lf);
			code.append("     * @throws FinderException when there's an error finding some object" + lf);
			code.append("     * " +lf);
			code.append("     * @return " + ejb.getPkName() + " primary key" + lf);
			code.append("     *" + lf);
			code.append("     */" + lf);
			code.append("    public Collection ejbFindAll() throws FinderException {" + lf);
			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement stmt = null;" + lf);
			code.append("        ResultSet rs = null;" + lf);
			code.append("        String sql_cmd = null;" + lf);
			code.append("        List l = new Vector();" + lf);
			code.append("        try {" + lf);
			code.append("            // get a connection for this transaction context" + lf);
			code.append("            conn = getConnection();" + lf);
			code.append("            " + lf);
			code.append("            // find Object state in DB" + lf);
			code.append("            sql_cmd = \"SELECT " + ejb.db_name + "." + ejb.getPkName() + " FROM " + ejb.db_name + "\";" + lf);
			code.append("            stmt = conn.prepareStatement(sql_cmd);" + lf);
			code.append("            rs = stmt.executeQuery();" + lf);
			code.append("            while (rs.next())" + lf);
			code.append("                l.add( rs.getObject(1) );" + lf);
			code.append("        } catch (SQLException e) {" + lf);
			code.append("            throw new FinderException(\"Failed to execute query \" + e);" + lf);
			code.append("        } finally {" + lf);
			code.append("            // Always make sure result sets and statements are closed," + lf);
			code.append("            // and the connection is returned to the pool" + lf);
			code.append("            if( rs != null) {" + lf);
			code.append("                try { rs.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                rs = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( stmt != null) {" + lf);
			code.append("                try { stmt.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                stmt = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( conn != null) {" + lf);
			code.append("                try { conn.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                conn = null;" + lf);
			code.append("            }" + lf);
			code.append("        }" + lf);
			code.append("        return l;" + lf);
			code.append("    }" + lf + lf);
		}

		if(ejb.type == EJB.POJO)
		{
			code.append("    /**" + lf);
			code.append("     * find all objects from the Database" + lf);
			code.append("     * " +lf);
			code.append("     * @throws FinderException when there's an error finding some object" + lf);
			code.append("     *" + lf);
			code.append("     */" + lf);
			code.append("    public void selectAll() throws FinderException {" + lf + lf);
			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement pstmt = null;" + lf);
			code.append("        String sql_cmd = null;" + lf);
			code.append("        try {" + lf);
			code.append("            // get a connection for this transaction context" + lf);
			code.append("            conn = getConnection();" + lf);
			code.append("            " + lf);
			code.append("            // find Object state in DB" + lf);
			code.append("            sql_cmd = \"SELECT " + sql_full_params.toString() + " FROM " + ejb.db_name + "\";" + lf);
			code.append("            pstmt = conn.prepareStatement(sql_cmd);" + lf);
			code.append("            rowset = new CachedRowSetImpl();" + lf);
			code.append("            rowset.populate(pstmt.executeQuery());" + lf);
			code.append("            rowset.beforeFirst();" + lf);
			code.append("        } catch (SQLException e) {" + lf);
			code.append("            throw new FinderException(\"Failed to execute query \" + e);" + lf);
			code.append("        } finally {" + lf);
			code.append("            // Always make sure result sets and statements are closed," + lf);
			code.append("            // and the connection is returned to the pool" + lf);
			code.append("            if( pstmt != null) {" + lf);
			code.append("                try { pstmt.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                pstmt = null;" + lf);
			code.append("            }" + lf);
			code.append("            if( conn != null) {" + lf);
			code.append("                try { conn.close(); } catch (SQLException e) { ; }" + lf);
			code.append("                conn = null;" + lf);
			code.append("            }" + lf);
			code.append("        }" + lf);
			code.append("    }" + lf + lf);

			// update
			code.append(
				"    /**" + lf +
				"     * Call with caution, this can be very expensive" + lf +
				"     */" + lf +
				"    public synchronized void update() throws EJBException {" + lf + lf +

				"        Connection conn = null;" + lf +
				"        PreparedStatement pstmt = null;" + lf +
				"        StringBuffer sql = new StringBuffer();" + lf +
				"        int updates = 0;" + lf +
				"        // verify if there's anything to update" + lf
			);

			code.append("        if(!(");
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.pk)
				{

					code.append(TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + "_dirty");
					if(j != ejb.fields.size()-1)
						code.append(" || ");
				}
			}
			code.append("))" + lf);
			code.append("            return;" + lf + lf);

			// generate the amazing query
			code.append(
				"        try {" + lf +
				"            // define table name" + lf +
				"            sql.append(\"UPDATE " + ejb.db_name + " SET \");" + lf +
				"            // for each column check for dirty data" + lf
			);

			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.pk)
				{
					code.append("            if(" + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + "_dirty) {" + lf);
					code.append("                if(updates > 0) sql.append(\", \"); else sql.append(\" \");" + lf);
					code.append("                sql.append(\"" + field.db_name + " = ?\");" + lf);
					code.append("                updates++;" + lf);
					code.append("            }" + lf + lf);
				}
			}

			if(ejb.needsPkObject())
			{
				boolean fst = true;
				code.append("            sql.append(\" WHERE");
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						if(fst) code.append(" "); else code.append(" AND ");
						code.append(field.db_name + " = ?");
						fst = false;
					}
				}
				code.append("\");" + lf);
			}
			else
				code.append("            sql.append(\" WHERE " + ejb.getPkDbName() + " = ?\");" + lf);
			code.append(
				"            // prepare the statement" + lf +
				"            conn = getConnection();" + lf +
				"            pstmt = conn.prepareStatement( sql.toString() );" + lf +
				"            // fill the values" + lf +
				"            updates = 1;" + lf + lf
			);

			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.pk)
				{
					code.append("            if(" + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + "_dirty) {" + lf);
					if(field.nullable)
					{
						code.append("                if(" + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " == null)" + lf);
						code.append("                    pstmt.setNull(updates++, java.sql.Types." + field.db_type.toUpperCase() + ");" + lf);
						code.append("                else" + lf);
						code.append("                    pstmt.setObject(updates++, this." + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + ");" + lf);
					}
					else
					{
						if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
						{
							code.append("                pstmt." + TextUtils.getJDBCSetter(field.db_type) + "(updates++, this." + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + ");" + lf);
						}
						else
						{
							code.append("                if(" + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " == null)" + lf);
							code.append("                    pstmt.setNull(updates++, java.sql.Types." + field.db_type.toUpperCase() + ");" + lf);
							code.append("                else" + lf);
							if("String".equals(TextUtils.toJavaFieldType(field.type)))
								code.append("                    pstmt.setString(updates++, this." + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + ");" + lf);
							else
								code.append("                    pstmt.setObject(updates++, this." + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + ");" + lf);
						}
					}
					code.append("            }" + lf + lf);
				}
			}

			// almost there...
			code.append(
				"            // finally set the row id" + lf
			);

			if(ejb.needsPkObject())
			{
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						code.append("            pstmt." + TextUtils.getJDBCSetter(field.db_type) + "(updates++, this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "());" + lf);
					}
				}
			}
			else
				code.append("            pstmt." + TextUtils.getJDBCSetter(ejb.getPkDbType()) + "(updates++, this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf);

			code.append(
				"            // execute the update statement" + lf +
				"            if(pstmt.executeUpdate() != 1)" + lf
			);


			if(ejb.needsPkObject())
			{
				boolean fst = true;
				code.append("                throw new EJBException(\"Impossible to update row with PK = \" + ");
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						if(!fst) code.append(" + \"/\" + ");
						code.append(TextUtils.toSunMethodName("get_" + field.name + "_id") + "()");
						fst = false;
					}
				}
				code.append(");" + lf + lf);
			}
			else
				code.append("                throw new EJBException(\"Impossible to update row with PK = \" + " + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf + lf);
			code.append(
				"        } catch(SQLException sqle) {" + lf +
				"            throw new EJBException(sqle);" + lf +
				"        } finally {" + lf +
				"            // Always make sure result sets and statements are closed," + lf +
				"            // and the connection is returned to the pool" + lf +
				"            if( pstmt != null) {" + lf +
				"                try { pstmt.close(); } catch (SQLException e) { ; }" + lf +
				"                pstmt = null;" + lf +
				"            }" + lf +
				"            if( conn != null) {" + lf +
				"                try { conn.close(); } catch (SQLException e) { ; }" + lf +
				"                conn = null;" + lf +
				"            }" + lf +
				"        }" + lf +
				"    }" + lf + lf
			);

			// delete
			code.append(
				"    /**" + lf +
				"     * Removes the current line from persistent engine" + lf +
				"     */" + lf +
				"    public synchronized void delete() throws RemoveException {" + lf + lf +

				"        Connection conn = null;" + lf +
				"        PreparedStatement pstmt = null;" + lf + lf +

				"        try {" + lf +
				"            // prepare the statement" + lf +
				"            conn = getConnection();" + lf +
				"            int arg = 1;" + lf
			);

			if(ejb.needsPkObject())
			{
				boolean fst = true;
				code.append("            pstmt = conn.prepareStatement(\"DELETE FROM " + ejb.db_name + " WHERE");
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						if(fst) code.append(" "); else code.append(" AND ");
						code.append(field.db_name + " = ?");
						fst = false;
					}
				}
				code.append("\");" + lf);
			}
			else
				code.append("            pstmt = conn.prepareStatement(\"DELETE FROM " + ejb.db_name + " WHERE " + ejb.getPkDbName() + " = ?\");" + lf);

			if(ejb.needsPkObject())
			{
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						code.append("            pstmt." + TextUtils.getJDBCSetter(field.db_type) + "(arg++, this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "());" + lf);
					}
				}
			}
			else
				code.append("            pstmt." + TextUtils.getJDBCSetter(ejb.getPkDbType()) + "(arg++, this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf);

			code.append(
				"            // execute the update statement" + lf +
				"            if(pstmt.executeUpdate() != 1)" + lf
			);

			if(ejb.needsPkObject())
			{
				boolean fst = true;
				code.append("                throw new EJBException(\"Impossible to delete row with PK = \" + ");
				for(int k=0; k<ejb.fields.size(); k++)
				{
					field = (FIELD)ejb.fields.elementAt(k);
					if(field.ck)
					{
						if(!fst) code.append(" + \"/\" + ");
						code.append(TextUtils.toSunMethodName("get_" + field.name + "_id") + "()");
						fst = false;
					}
				}
				code.append(");" + lf + lf);
			}
			else
				code.append("                throw new EJBException(\"Impossible to delete row with PK = \" + " + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf);
			code.append(
				"        } catch(SQLException sqle) {" + lf +
				"            throw new EJBException(sqle);" + lf +
				"        } finally {" + lf +
				"            // Always make sure result sets and statements are closed," + lf +
				"            // and the connection is returned to the pool" + lf +
				"            if( pstmt != null) {" + lf +
				"                try { pstmt.close(); } catch (SQLException e) { ; }" + lf +
				"                pstmt = null;" + lf +
				"            }" + lf +
				"            if( conn != null) {" + lf +
				"                try { conn.close(); } catch (SQLException e) { ; }" + lf +
				"                conn = null;" + lf +
				"            }" + lf +
				"        }" + lf +
				"    }" + lf + lf
			);

			code.append(
				"    /**" + lf +
				"     * Reset's dirty flags (called after a read from persistent engine operation" + lf +
				"     */" + lf +
				"    private void resetDirty() {" + lf
			);

			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.pk)
				{

					code.append("        " + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + "_dirty = false;" + lf);
				}
			}
			code.append("    }" + lf + lf);

			code.append(
				"    /**" + lf +
				"     * Navigates across the rowset, return false when there's no more elements" + lf +
				"     * @return true when there is a next element" + lf +
				"     */" + lf +
				"    public boolean next() throws EJBException {" + lf +
				"        try {" + lf +
				"            // hopefully this will be fast because no data is needed to be" + lf +
				"            // updated, if data is needed to be updated this can take a while" + lf +
				"            this.update();" + lf +
				"            // now clear dirty flags since out pointer is going to move" + lf +
				"            this.resetDirty();" + lf +
				"            return rowset.next();" + lf +
				"        } catch(SQLException sqle) {" + lf +
				"            throw new EJBException(sqle);" + lf +
				"        }" + lf +
				"    }" + lf + lf +

				"    /**" + lf +
				"     * Returns the number of rows available in the select method" + lf +
				"     * @return number of rows available" + lf +
				"     */" + lf +
				"    public int size() {" + lf +
				"        if(this.rowset == null)" + lf +
				"            return 0;" + lf +
				"        else" + lf +
				"            return rowset.size();" + lf +
				"    }" + lf + lf +

				"    /**" + lf +
				"     * When the client changes a value this JDO implementation" + lf +
				"     * won't update the data at that time, it will be scheduled for" + lf +
				"     * a latter bulk row update. When the client don't call the update" + lf +
				"     * method and the bean was changed, this method will invoke it" + lf +
				"     * preventing data loss." + lf +
				"     */" + lf +
				"    public void ejbRemove() {" + lf +
				"        try {" + lf +
				"            this.update();" + lf +
				"        } catch(EJBException ejbe) {" + lf +
				"            ejbe.printStackTrace();" + lf +
				"        }" + lf +
				"    }" + lf
			);

			// fk finders
			EJB ref_ejb = null;
			for(int i=0; i<ejbs.size(); i++)
			{
				COLLECTION col = null;
				// add 1 to Many RellationShips
				ref_ejb = (EJB)ejbs.elementAt(i);
				for(int j=0; j<ref_ejb.collections.size(); j++)
				{
					col = (COLLECTION)ref_ejb.collections.elementAt(j);
					// found a 1 to Many RelationShip
					if(ejb.name.equals(col.obj_name))
					{
						boolean found = false;
						code.append("    /**" + lf);
						code.append("     * Finds all entries in the storage for your JDO's " + TextUtils.toSunClassName(ejb.name) + lf);
						code.append("     * Where " + TextUtils.toSunClassName(ref_ejb.name) + " is the parameter used in query" + lf);
						code.append("     * @param key parameter to use in Query" + lf);
						code.append("     */" + lf);
						// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
						if(ejb.name.equals(col.getAlias()))
						{
							for(int k=0; k<ref_ejb.fields.size(); k++)
							{
								field = (FIELD)ref_ejb.fields.elementAt(k);
								if(field.fk &&
									(
										field.db_name.equals(ejb.getPkDbName()) || field.db_alias.equals(ejb.getPkDbName())
									)
								)
								{
									code.append("    public void selectBy" + TextUtils.toSunClassName(field.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException {" + lf + lf);
									found = true;
									break;
								}
							}
							if(!found)
							{
								code.append("    public void selectBy" + TextUtils.toSunClassName(ref_ejb.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException {" + lf + lf);
							}
						}
						else
						{
							code.append("    public void selectBy" + TextUtils.toSunClassName(col.getAlias()) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException {" + lf + lf);
						}

						// aqui código deste finder
						code.append("        Connection conn = null;" + lf);
						code.append("        PreparedStatement stmt = null;" + lf);
						code.append("        ResultSet rs = null;" + lf);
						code.append(lf);
						code.append("        try {" + lf);
						code.append("            // get a connection for this transaction context" + lf);
						code.append("            conn = getConnection();" + lf);
						code.append("            // insert in DB" + lf);
						code.append("            String sql_cmd = \"SELECT " + sql_full_params.toString() + " FROM " + ejb.db_name + " WHERE " + ejb.db_name + "." + ref_ejb.getPkDbName() + " = ?\";" + lf);
						code.append("            int arg = 1;" + lf);
						code.append("            stmt = conn.prepareStatement( sql_cmd );" + lf);

						code.append("            stmt." + TextUtils.getJDBCSetter(ref_ejb.getPkType()) + "(arg++, key);" + lf);

						code.append("            rs = stmt.executeQuery();" + lf);
						code.append("            if (!rs.next())" + lf);
						code.append("                throw new EJBException(\"Failed to load bean from database\");" + lf);
						code.append("        } catch (SQLException e) {" + lf);
						code.append("            throw new EJBException(\"Failed to load bean from database\");" + lf);
						code.append("        } finally {" + lf);
						code.append("            // Always make sure result sets and statements are closed," + lf);
						code.append("            // and the connection is returned to the pool" + lf);
						code.append("            if( rs != null) {" + lf);
						code.append("                try { rs.close(); } catch (SQLException e) { ; }" + lf);
						code.append("                rs = null;" + lf);
						code.append("            }" + lf);
						code.append("            if( stmt != null) {" + lf);
						code.append("                try { stmt.close(); } catch (SQLException e) { ; }" + lf);
						code.append("                stmt = null;" + lf);
						code.append("            }" + lf);
						code.append("            if( conn != null) {" + lf);
						code.append("                try { conn.close(); } catch (SQLException e) { ; }" + lf);
						code.append("                conn = null;" + lf);
						code.append("            }" + lf);
						code.append("        }" + lf);
						code.append("    }" + lf + lf);
					}
				}
			}

		}

		code.append("}" + lf);
		return code.toString();
	}

	private String data_access_bean(DAC dac)
	{
		PARAM param = null;
		RESOURCE res = null;
		StringBuffer code = new StringBuffer();

		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + dac.getDACPackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.sql.Connection;" + lf);
		code.append("import java.sql.PreparedStatement;" + lf);
		code.append("import java.sql.SQLException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import ejb.core.SessionBeanAdapter;" + lf);
		code.append("import ejb.exception.DACException;" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.EJBException;" + lf);
		code.append("import javax.sql.DataSource;" + lf);
		code.append("import javax.sql.rowset.CachedRowSet;" + lf);
		code.append("import com.sun.rowset.CachedRowSetImpl;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + dac.getDACPackage() + "." + TextUtils.toSunClassName(dac.name) + "DAC" + lf);
		code.append(" * " + lf);
		code.append(" * " + dac.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + dac.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(dac.name) + "DAC extends SessionBeanAdapter {" + lf + lf);

		code.append(
			"    private static final String statement = \"" + dac.query + "\";" + lf +
			"    private transient DataSource dataSource = null;" + lf +
			"    private transient CachedRowSet rowset = null;" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * @return  the connection from the dataSource" + lf);
		code.append("     * @exception EJBException  Thrown by the method if the dataSource is not found" + lf);
		code.append("     *                          in the naming." + lf);
		code.append("     * @exception SQLException may be thrown by dataSource.getConnection()" + lf);
		code.append("     */" + lf);
		code.append("    private Connection getConnection() throws EJBException, SQLException {" + lf);
		code.append("        if (dataSource == null) {" + lf);
		code.append("            // Finds DataSource from JNDI" + lf);
		code.append("            try {" + lf);

		for(int i=0; i<dac.resources.size(); i++)
		{
			res = (RESOURCE) dac.resources.elementAt(i);
			if(res.type == RESOURCE.DATASOURCE)
			{
				break;
			}
			if(i == dac.resources.size()-1)
				System.err.println("DATASOURCE resource not found!");
		}

		code.append("                dataSource = (DataSource)this.initContext.lookup(\"java:comp/env/" + res.jndi + "\");" + lf);
		code.append("            } catch (javax.naming.NamingException e) {" + lf);
		code.append("                throw new EJBException(e);" + lf);
		code.append("            }" + lf);
		code.append("        }" + lf);
		code.append("        return dataSource.getConnection();" + lf);
		code.append("    }" + lf);
		code.append(lf);

		// generate the param's call
		StringBuffer create_params = new StringBuffer();

		for(int i=0; i<dac.params.size(); i++)
		{
			param = (PARAM)dac.params.elementAt(i);
			if(param.nullable)
				create_params.append( TextUtils.toJavaObjectFieldType(param.type) + " " + TextUtils.toSunParameterName(param.name) + ", ");
			else
				create_params.append( TextUtils.toJavaFieldType(param.type) + " " + TextUtils.toSunParameterName(param.name) + ", ");
		}

		// cut the extra ', ' chars
		if(create_params.length() != 0)
			create_params.setLength( create_params.length() - 2 );

		code.append(
			"    /**" + lf +
			"     * Executes a Use Case using the defined QUERY" + lf +
			"     */" + lf +
			"    public void execute(" + create_params.toString() + ") throws DACException {" + lf +
			"        try {" + lf +
			"            Connection con = getConnection();" + lf +
			"            PreparedStatement pstmt = con.prepareStatement(statement);" + lf
		);

		// add param's here
		for(int i=0; i<dac.params.size(); i++)
		{
			param = (PARAM)dac.params.elementAt(i);
			if(param.nullable)
			{
				code.append("            if(" + TextUtils.toSunParameterName(param.name) + " == null)" + lf);
				code.append("                pstmt.setNull(" + param.number + ", java.sql.Types." + param.sql_type.toUpperCase() + ");" + lf);
				code.append("            else" + lf);
				if("String".equals(TextUtils.toJavaObjectFieldType(param.type)))
					code.append("                pstmt." + TextUtils.toSunMethodName("set_string") + "(" + param.number + ", " + TextUtils.toSunParameterName(param.name) + ");" + lf);
				else
					code.append("                pstmt." + TextUtils.toSunMethodName("set_object") + "(" + param.number + ", " + TextUtils.toSunParameterName(param.name) + ");" + lf);
			}
			else
			{
				code.append("            pstmt." + TextUtils.toSunMethodName("set_" + TextUtils.toJavaFieldType(param.type)) + "(" + param.number + ", " + TextUtils.toSunParameterName(param.name) + ");" + lf);
			}
		}

		code.append(
			"            rowset = new CachedRowSetImpl();" + lf +
			"            rowset.populate(pstmt.executeQuery());" + lf +
			"            rowset.beforeFirst();" + lf +
			"            if(pstmt != null) pstmt.close();" + lf +
			"            if(con != null) con.close();" + lf +
			"        } catch(EJBException ejbe) {" + lf +
			"            throw new DACException(ejbe);" + lf +
			"        } catch(SQLException sqle) {" + lf +
			"            throw new DACException(sqle);" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Navigates across the rowset, return false when there's no more elements" + lf);
		code.append("     * @return true when there is a next element" + lf);
		code.append("     */" + lf);
		code.append("    public boolean next() throws DACException {" + lf);
		code.append("        try {" + lf);
		code.append("            return rowset.next();" + lf);
		code.append("        } catch(SQLException sqle) {" + lf);
		code.append("            throw new DACException(sqle);" + lf);
		code.append("        }" + lf);
		code.append("    }" + lf);

		code.append("    /**" + lf);
		code.append("     * Number of elements in rowset" + lf);
		code.append("     * @return number os elements in rowset" + lf);
		code.append("     */" + lf);
		code.append("    public int size() {" + lf);
		code.append("        return (rowset==null?0:rowset.size());" + lf);
		code.append("    }" + lf);

		// getter's
		for(int i=0; i<dac.columns.size(); i++)
		{
			param = (PARAM)dac.columns.elementAt(i);
			code.append("    /**" + lf);
			code.append("     * Getter for field " + TextUtils.toSunParameterName(param.name) + lf);
			code.append("     * @return the value for the field" + lf);
			code.append("     */" + lf);
			if(param.nullable)
			{
				code.append("    public " + TextUtils.toJavaObjectFieldType(param.type) + " " + TextUtils.toSunMethodName("get_" + param.name) + "() throws DACException {" + lf);
				code.append("        try {" + lf);
				if("String".equals(TextUtils.toJavaObjectFieldType(param.type)))
					code.append("            return rowset." + TextUtils.toSunMethodName("get_string") + "(" + param.number + ");" + lf);
				else
					code.append("            return (" + TextUtils.toJavaObjectFieldType(param.type) + ")rowset." + TextUtils.toSunMethodName("get_object") + "(" + param.number + ");" + lf);
				code.append(
					"        } catch(SQLException sqle) {" + lf +
					"            throw new DACException(sqle);" + lf +
					"        }" + lf
				);
			}
			else
			{
				code.append("    public " + TextUtils.toJavaFieldType(param.type) + " " + TextUtils.toSunMethodName("get_" + param.name) + "() throws DACException {" + lf);
				code.append("        try {" + lf);
				code.append("            return rowset." + TextUtils.toSunMethodName("get_" + TextUtils.toJavaFieldType(param.type)) + "(" + param.number + ");" + lf);
				code.append(
					"        } catch(SQLException sqle) {" + lf +
					"            throw new DACException(sqle);" + lf +
					"        }" + lf
				);
			}
			code.append("    }" + lf);
			code.append(lf);
		}

		code.append("    public void ejbCreate() throws CreateException {}" + lf);

		code.append("}" + lf);
		return code.toString();
	}

	// folks that use jsp most probably came from asp or some other micro$osf platform
	// so thei're used to have a naive API's so i'll try to make it the simples possible
	private String manager_bean(EJB ejb)
	{
		FIELD field = null;
		RESOURCE res = null;

		StringBuffer code = new StringBuffer();
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getPackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.util.Collection;" + lf);
		code.append("import java.util.Iterator;" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("import ejb.util.Trace;" + lf);
		code.append("import ejb.util.BeanHomeFactory;" + lf);
		code.append("import ejb.exception.BeanHomeFactoryException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getPackage() + "." + TextUtils.toSunClassName(ejb.name) + "Manager" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + "Manager {" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Reference to a Home interface factory" + lf);
		code.append("     */" + lf);
		code.append("    private static BeanHomeFactory factory = null;" + lf + lf);

		// define the constructor
		code.append(
			"    /**" + lf +
			"     * Bean Manager Static initializer" + lf +
			"     */" + lf +
			"    private static void init() throws BeanHomeFactoryException {" + lf +
			"        try {" + lf +
			"            factory = BeanHomeFactory.getFactory();" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		// get all not null fields and force them into the create method
		StringBuffer native_create_params = new StringBuffer();
		StringBuffer ejb_create_params = new StringBuffer();

		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.nullable && !field.pk)
			{
				native_create_params.append(TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
				ejb_create_params.append(TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
			}
		}

		// remove the trailling ' ,'
		if(native_create_params.length() > 0)
		{
			native_create_params.setLength( native_create_params.length() - 2 );
			ejb_create_params.setLength( ejb_create_params.length() - 2 );
		}

		code.append("    /**" + lf);
		code.append("     * Creates a proxy object for " + TextUtils.toSunClassName(ejb.name) + lf);
		code.append("     * " + lf);
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.nullable && !field.pk)
				code.append("     * @param " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " " + lf);
		}
		code.append("     * @return Wrapper instance for a " + TextUtils.toSunClassName(ejb.name) + " proxy" + lf);
		code.append("     */" + lf);
		code.append(
			"    public static synchronized " + TextUtils.toSunClassName(ejb.name) + " create(" + native_create_params.toString() + ") throws BeanHomeFactoryException, CreateException, RemoteException {" + lf +
			"        try {" + lf +
			"            if(factory == null) init();" + lf +
			"            " + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\");" + lf +
			"            return new " + TextUtils.toSunClassName(ejb.name) + "(home.create(" + ejb_create_params.toString() + "));" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        } catch(CreateException ce) {" + lf +
			"            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw ce;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Finds an entry in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
		code.append("     * " + lf);
		code.append("     * @param key Primary Key that identifies the entry" + lf);
		code.append("     * @return Wrapper for " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
		code.append("     */" + lf);
		code.append(
			"    public static synchronized " + TextUtils.toSunClassName(ejb.name) + " findByPrimaryKey(" + TextUtils.toJavaFieldType(ejb.getPkType()) + " key) throws BeanHomeFactoryException, FinderException, RemoteException {" + lf +
			"        try {" + lf +
			"            if(factory == null) init();" + lf +
			"            " + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\");" + lf +
			"            return new " + TextUtils.toSunClassName(ejb.name) + "(home.findByPrimaryKey(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(ejb.getPkType()),"key") + "));" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        } catch(FinderException fe) {" + lf +
			"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw fe;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Finds all entries in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
		code.append("     * " + lf);
		code.append("     * @return Wrapper for " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
		code.append("     */" + lf);
		code.append(
			"    public static synchronized " + TextUtils.toSunClassName(ejb.name) + "[] findAll() throws BeanHomeFactoryException, FinderException, RemoteException {" + lf +
			"        try {" + lf +
			"            if(factory == null) init();" + lf +
			"            " + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\");" + lf +
			"            Collection elements = home.findAll();" + lf +
			"            if(elements == null)" + lf +
			"                return null;" + lf +
			"            else" + lf +
			"            {" + lf +
			"                " + TextUtils.toSunClassName(ejb.name) + "[] wrappers = new " + TextUtils.toSunClassName(ejb.name) + "[elements.size()];" + lf +
			"                Iterator it = elements.iterator();" + lf +
			"                int i = 0;" + lf +
			"                while(it.hasNext()) {" + lf +
			"                    wrappers[i++] = new " + TextUtils.toSunClassName(ejb.name) + "((" + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote)it.next());" + lf +
			"                    it.remove();" + lf +
			"                }" + lf +
			"                it = null;" + lf +
			"                elements = null;" + lf +
			"                return wrappers;" + lf +
			"            }" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        } catch(FinderException fe) {" + lf +
			"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw fe;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		COLLECTION col_tmp = null;
		FIELD field_tmp = null;
		EJB ejb_tmp = null;
		String pk_package = null;
		String pk_type = null;

		// find collections for other objects
		for(int i=0; i<ejb.collections.size(); i++)
		{
			pk_package = null;
			col_tmp = (COLLECTION)ejb.collections.elementAt(i);

			String home_intf_name = null;
			// navigate the oposite way as in home interface
			for(int j=0; j<ejbs.size(); j++)
			{
				ejb_tmp = (EJB)ejbs.elementAt(j);
				// found a 1 to Many RelationShip
				if(ejb_tmp.name.equals(col_tmp.obj_name))
				{
					boolean found = false;
					// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
//					if(ejb.name.equals(col_tmp.getAlias()))
//					{
						for(int k=0; k<ejb_tmp.fields.size(); k++)
						{
							field = (FIELD)ejb_tmp.fields.elementAt(k);
							if(field.fk &&
								(
									field.db_name.equals(ejb.getPkDbName()) || field.db_alias.equals(ejb.getPkDbName())
								)
							)
							{
								home_intf_name = field.name;
								found = true;
								break;
							}
						}
						if(!found)
							home_intf_name = ejb_tmp.name;
//					}
//					else
//						home_intf_name = col_tmp.getAlias();
				}
			}

			for(int k=0;k<ejbs.size(); k++)
			{
				ejb_tmp = (EJB)ejbs.elementAt(k);
				if(col_tmp.obj_name.equals(ejb_tmp.name))
				{
					pk_package = ejb_tmp.getPackage();
					pk_type = TextUtils.toJavaFieldType(ejb.getPkType());
					break;
				}
			}

			code.append("    /**" + lf);
			code.append("     * Finds all entries in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
			code.append("     * " + lf);
			code.append("     * @return Wrapper for " + TextUtils.toSunClassName(col_tmp.obj_name) + "Remote" + lf);
			code.append("     */" + lf);
			code.append(
				"    public static synchronized " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] " + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(TextUtils.toSunClassName(col_tmp.getAlias()))) + "(" + pk_type + " key) throws BeanHomeFactoryException, FinderException, RemoteException {" + lf +
				"        try {" + lf +
				"            if(factory == null) init();" + lf +
				"            " + EJB.getHomePackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Home home = (" + EJB.getHomePackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Home)factory.getHome(" + EJB.getHomePackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Home.class, \"java:comp/env/ejb/" + ejb_tmp.getJNDIPath() + TextUtils.toSunClassName(ejb_tmp.name) + "\");" + lf +
				"            Collection elements = home.findBy" + TextUtils.toSunClassName(home_intf_name) + "(key);" + lf +
				"            if(elements == null)" + lf +
				"                return null;" + lf +
				"            else" + lf +
				"            {" + lf +
				"                " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] wrappers = new " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[elements.size()];" + lf +
				"                Iterator it = elements.iterator();" + lf +
				"                int i = 0;" + lf +
				"                while(it.hasNext()) {" + lf +
				"                    wrappers[i++] = new " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "((" + EJB.getRemotePackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Remote)it.next());" + lf +
				"                    it.remove();" + lf +
				"                }" + lf +
				"                it = null;" + lf +
				"                elements = null;" + lf +
				"                return wrappers;" + lf +
				"            }" + lf +
				"        } catch(BeanHomeFactoryException bhfe) {" + lf +
				"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw bhfe;" + lf +
				"        } catch(FinderException fe) {" + lf +
				"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw fe;" + lf +
				"        } catch(RemoteException re) {" + lf +
				"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw re;" + lf +
				"        }" + lf +
				"    }" + lf + lf
			);

		}
		code.append("}" + lf);
		return code.toString();
	}

	private String query_bean(DAC dac)
	{
		PARAM param = null;

		StringBuffer code = new StringBuffer();
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + dac.getPackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.RemoveException;" + lf);
		code.append("import ejb.util.Trace;" + lf);
		code.append("import ejb.util.BeanHomeFactory;" + lf);
		code.append("import ejb.exception.BeanHomeFactoryException;" + lf);
		code.append("import ejb.exception.DACException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + dac.getPackage() + "." + TextUtils.toSunClassName(dac.name) + "Query" + lf);
		code.append(" * " + lf);
		code.append(" * " + dac.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + dac.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(dac.name) + "Query {" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Reference to a Home interface factory" + lf);
		code.append("     */" + lf);
		code.append("    private static BeanHomeFactory factory = null;" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Internal reference to Remote Object" + lf);
		code.append("     */" + lf);
		code.append("    private transient " + dac.getRemotePackage() + "." + TextUtils.toSunClassName(dac.name) + "Remote dac = null;" + lf + lf);

		// define the constructor
		code.append(
			"    /**" + lf +
			"     * Query Bean initializer" + lf +
			"     */" + lf +
			"    private void init() throws BeanHomeFactoryException, CreateException, RemoteException {" + lf +
			"        try {" + lf +
			"            if(factory == null) factory = BeanHomeFactory.getFactory();" + lf +
			"            " + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home home = (" + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home)factory.getHome(" + dac.getHomePackage() + "." + TextUtils.toSunClassName(dac.name) + "Home.class, \"java:comp/env/dac/" + dac.getJNDIPath() + TextUtils.toSunClassName(dac.name) + "\");" + lf +
			"            this.dac = home.create();" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        } catch(CreateException ce) {" + lf +
			"            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw ce;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		// generate the param's call
		StringBuffer create_params = new StringBuffer();
		StringBuffer create_params_names = new StringBuffer();

		for(int i=0; i<dac.params.size(); i++)
		{
			param = (PARAM)dac.params.elementAt(i);
			if(param.nullable)
				create_params.append( TextUtils.toJavaObjectFieldType(param.type) + " " + TextUtils.toSunParameterName(param.name) + ", ");
			else
				create_params.append( TextUtils.toJavaFieldType(param.type) + " " + TextUtils.toSunParameterName(param.name) + ", ");

			create_params_names.append(TextUtils.toSunParameterName(param.name) + ", ");
		}

		// cut the extra ', ' chars
		if(create_params.length() != 0)
		{
			create_params.setLength( create_params.length() - 2 );
			create_params_names.setLength( create_params_names.length() - 2 );
		}
		code.append(
			"    /**" + lf +
			"     * Executes a Use Case using the defined QUERY" + lf +
			"     */" + lf +
			"    public void execute(" + create_params.toString() + ") throws BeanHomeFactoryException, DACException, CreateException, RemoteException {" + lf +
			"        try {" + lf +
			"            if(dac == null) init();" + lf +
			"            dac.execute(" + create_params_names.toString() + ");" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        } catch(DACException dace) {" + lf +
			"            Trace.errln(\"DACException - \" + dace.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw dace;" + lf +
			"        } catch(CreateException ce) {" + lf +
			"            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw ce;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Navigates across the rowset, return false when there's no more elements" + lf);
		code.append("     * " + lf);
		code.append("     * @return true when there is a next element" + lf);
		code.append("     */" + lf);
		code.append(
			"    public boolean next() throws DACException, RemoteException {" + lf +
			"        if(dac == null) throw new DACException(\"Data Access Command Disconnected, must call execute(...) first!\");" + lf +
			"        try {" + lf +
			"            return dac.next();" + lf +
			"        } catch(DACException dace) {" + lf +
			"            Trace.errln(\"DACException - \" + dace.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw dace;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Number of elements in rowset" + lf);
		code.append("     * " + lf);
		code.append("     * @return number of elements in rowset" + lf);
		code.append("     */" + lf);
		code.append(
			"    public int size() throws DACException, RemoteException {" + lf +
			"        if(dac == null) throw new DACException(\"Data Access Command Disconnected, must call execute(...) first!\");" + lf +
			"        try {" + lf +
			"            return dac.size();" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		// getter's
		for(int i=0; i<dac.columns.size(); i++)
		{
			param = (PARAM)dac.columns.elementAt(i);
			code.append("    /**" + lf);
			code.append("     * Getter for field " + TextUtils.toSunParameterName(param.name) + lf);
			code.append("     * @return the value for the field" + lf);
			code.append("     */" + lf);
			if(param.nullable)
			{
				code.append("    public " + TextUtils.toJavaObjectFieldType(param.type) + " " + TextUtils.toSunMethodName("get_" + param.name) + "() throws RemoteException, DACException {" + lf);
			}
			else
			{
				code.append("    public " + TextUtils.toJavaFieldType(param.type) + " " + TextUtils.toSunMethodName("get_" + param.name) + "() throws RemoteException, DACException {" + lf);
			}
			code.append(
				"        try {" + lf +
				"            return dac." + TextUtils.toSunMethodName("get_" + param.name) + "();" + lf +
				"        } catch(DACException dace) {" + lf +
				"            Trace.errln(\"DACException - \" + dace.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw dace;" + lf +
				"        } catch(RemoteException re) {" + lf +
				"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw re;" + lf +
				"        }" + lf
			);
			code.append("    }" + lf);
			code.append(lf);
		}

		code.append("    /**" + lf);
		code.append("     * Closes the resources used by this Query" + lf);
		code.append("     */" + lf);
		code.append(
			"    public void close() {" + lf +
			"        try {" + lf +
			"            if(dac != null) dac.remove();" + lf +
			"            dac = null;" + lf +
			"        } catch(RemoveException rme) {" + lf +
			"            Trace.errln(\"RemoveException - \" + rme.getMessage(), Trace.SEVERE, true);" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append("    /**" + lf);
		code.append("     * Cleanup for this object" + lf);
		code.append("     */" + lf);
		code.append(
			"    protected void finalize() throws Throwable {" + lf +
			"        if(dac != null) dac.remove();" + lf +
			"        dac = null;" + lf +
			"        factory = null;" + lf +
			"    }" + lf + lf
		);

		code.append("}" + lf);
		return code.toString();
	}

	private String wraper_bean(EJB ejb)
	{
		FIELD field = null;
		FIELD field_tmp = null;

		StringBuffer code = new StringBuffer();
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getPackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("import ejb.util.Trace;" + lf);
		code.append("import ejb.exception.BeanHomeFactoryException;" + lf);
		code.append("import ejb.exception.MandatoryFieldException;" + lf);
		code.append("import ejb.exception.DisconnectedBeanException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getPackage() + "." + TextUtils.toSunClassName(ejb.name) + "" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + " {" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Internal reference to Remote Object" + lf);
		code.append("     */" + lf);
		code.append("    private " + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote ejb = null;" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Is this session bean phisically connected to the entity/command?" + lf);
		code.append("     */" + lf);
		code.append("    private boolean _connected = false;" + lf + lf);

		// if we want lazy loading for fields lets make a 'local' copy inside this JavaBean
		if(project.lazy.fields)
		{
			// Generate all the fields
			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				code.append(
					"    /**" + lf +
					"     * Local copy of remote field  field " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " for Lazy Loading." + lf +
					"     * This means that this bean will only load once this property until there's a update." + lf +
					"     */" + lf
				);

				if(field.nullable)
					code.append("    private " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = null;" + lf + lf);
				else
					code.append("    private " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.getInitialValueFor(TextUtils.toJavaFieldType(field.type)) + ";" + lf + lf);

				code.append(
					"    /**" + lf +
					"     * dirty flag to inform that is needed some entity update for this field" + lf +
					"     */" + lf +
					"    private boolean " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty = true;" + lf + lf
				);
			}
		}

		// if we want lazy FK
		if(project.lazy.fks)
		{
			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				if(field.fk)
				{
					String fk_tmp_name = null;
					String fk_tmp_package = null;
					EJB ejb_tmp = null;
					COLLECTION col_tmp = null;
					boolean found = false;

					// find where the reference to this fk
					for(int j=0; j<ejbs.size(); j++)
					{
						ejb_tmp = (EJB)ejbs.elementAt(j);
						for(int k=0; k<ejb_tmp.fields.size(); k++)
						{
							field_tmp = (FIELD)ejb_tmp.fields.elementAt(k);
							if(field_tmp.pk && field_tmp.db_name.equals(field.db_name))
							{
								fk_tmp_package = ejb_tmp.getPackage();
								fk_tmp_name = ejb_tmp.name;
								found = true;
							}
							if(found) break;
						}
						if(found) break;
					}
					if(fk_tmp_name == null)
					{
						if(!"".equals(field.db_alias))
						{
							found = false;
							// find where the reference to this fk
							for(int a=0; a<ejbs.size(); a++)
							{
								ejb_tmp = (EJB)ejbs.elementAt(a);
								for(int b=0; b<ejb_tmp.fields.size(); b++)
								{
									field_tmp = (FIELD)ejb_tmp.fields.elementAt(b);
									if(field_tmp.pk && field_tmp.db_name.equals(field.db_alias))
									{
										fk_tmp_package = ejb_tmp.getPackage();
										fk_tmp_name = ejb_tmp.name;
										found = true;
									}
									if(found) break;
								}
								if(found) break;
							}
							if(fk_tmp_name == null)
								System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
						}
						else
							System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
					}
					code.append(
						"    /**" + lf +
						"     * Local copy of remote field  field " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name) + " for Lazy Loading." + lf +
						"     * This means that this bean will only load once this property until there's a update." + lf +
						"     */" + lf +
						"    private " + EJB.getPackage(fk_tmp_package) + "." + TextUtils.toSunClassName(fk_tmp_name) + " " + TextUtils.toSunMethodName(field.name) + " = null;" + lf + lf +
						"    /**" + lf +
						"     * dirty flag to inform that is needed some entity update for this field" + lf +
						"     */" + lf +
						"    private boolean " + TextUtils.toSunMethodName(field.name) + "_dirty = true;" + lf + lf
					);
				}
			}
		}

		COLLECTION col_tmp = null;
		EJB ejb_tmp = null;
		String pk_package = null;

		if(project.lazy.collections)
		{
			// Generate all the collections
			for(int j=0; j<ejb.collections.size(); j++)
			{
				pk_package = null;
				col_tmp = (COLLECTION)ejb.collections.elementAt(j);

				for(int k=0;k<ejbs.size(); k++)
				{
					ejb_tmp = (EJB)ejbs.elementAt(k);
					if(col_tmp.obj_name.equals(ejb_tmp.name))
					{
						pk_package = ejb_tmp.getPackage();
						break;
					}
				}

				code.append(
					"    /**" + lf +
					"     * Local copy of remote Collection of " + TextUtils.toSunClassName(col_tmp.obj_name)  + "Remote for Lazy Loading." + lf +
					"     * This means that this bean will only load once this property until there's a update." + lf +
					"     */" + lf +
					"    private " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = null;" + lf + lf +
					"    /**" + lf +
					"     * dirty flag to inform that is needed some entity update for this field" + lf +
					"     */" + lf +
					"    private boolean " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "_dirty = true;" + lf + lf
				);
			}
		}

		// setter's
		// setter's aren't managed by lazy loading because we don't want to
		// make the database inconsistent
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.ro)
			{
				code.append("    /**" + lf);
	 			code.append("     * Setter for field " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + lf);
	 			code.append("     * @param " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " the new value for this field" + lf);
				code.append("     */" + lf);

				if(field.nullable)
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws DisconnectedBeanException, RemoteException" + (field.nullable==true?"":", MandatoryFieldException") + " {" + lf);
				else
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws DisconnectedBeanException, RemoteException" + (field.nullable==true?"":", MandatoryFieldException") + " {" + lf);

				code.append("        if(!this._connected) {" + lf);
				code.append("            Trace.errln(\"DisconnectedBeanException - " + TextUtils.toSunClassName(ejb.name) + " is disconnected\", Trace.SEVERE, true);" + lf);
				code.append("            throw new DisconnectedBeanException(\"" + TextUtils.toSunClassName(ejb.name) + " is disconnected!\");" + lf);
				code.append("        }" + lf);
				code.append("        try {" + lf);

				if(project.lazy.fields)
				{
					code.append("            if(this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty) {" + lf);
					code.append("                " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "();" + lf);
					code.append("            }" + lf);

					if(field.nullable)
		 			{
		 				if(!TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
		 				{
							code.append("            // both internal and new fields are null, leave it as it is..." + lf);
							code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == null && this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " == null) return;" + lf);
						}
						code.append("            // both internal and new fields are equal, leave it as it is..." + lf);
						if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
						{
							code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + ") return;" + lf);
						}
						else
						{
							code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " != null && " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ".equals(this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + ")) return;" + lf);
						}
						code.append("            this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ";" + lf);
					}
		 			else
					{
						if(!TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
						{
							code.append("            // since the db field can't be null we won't allow you to set it null..." + lf);
							code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == null) throw new MandatoryFieldException(\"'" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + "' can't be null.\");" + lf);
						}
						code.append("            // both internal and new fields are equal, leave it as it is..." + lf);
						if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
							code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + ") return;" + lf);
						else
							code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ".equals(this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + ")) return;" + lf);
						code.append("            this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ";" + lf);
		 			}
				}
				code.append("            ejb." + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ");" + lf);
				if(project.lazy.fields)
					code.append("            this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty = false;" + lf);
				code.append("        } catch(RemoteException re) {" + lf);
				code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
				code.append("            throw re;" + lf);
				code.append("        }" + lf);
				code.append("    }" + lf);
				code.append(lf);
			}
		}
		code.append(lf);

		// getter's
		// if we want lazy loading for fields we will only load when the field is dirty
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			code.append("    /**" + lf);
			code.append("     * Getter for field " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + lf);
			code.append("     * @return the value for the field" + lf);
			code.append("     */" + lf);

			if(field.nullable)
				code.append("    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException {" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException {" + lf);
			code.append("        try {" + lf);
			if(project.lazy.fields)
			{
				code.append("            if(this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty) {" + lf);
				code.append("                this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = ejb." + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "();" + lf);
				code.append("                this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + "_dirty = false;" + lf);
				code.append("            }" + lf);
				code.append("            return this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + ";" + lf);
			}
			else
			{
				code.append("            return ejb." + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "()" + TextUtils.toJavaFieldType(field.type) + ";" + lf);
			}
			code.append("        } catch(RemoteException re) {" + lf);
			code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
			code.append("            throw re;" + lf);
			code.append("        }" + lf);
			code.append("    }" + lf);
			code.append(lf);
		}
		code.append(lf);

		code.append("    /**" + lf);
		code.append("     * This method will phisically remove the Bean from the persistent storage" + lf);
		code.append("     *" + lf);
		code.append("     */" + lf);
		code.append("    public void remove() throws RemoteException {" + lf);
		code.append("        try {" + lf);
		code.append("            this.ejb.remove();" + lf);
		code.append("            // invalidate this bean" + lf);
		code.append("            finalize();" + lf);
		code.append("        } catch(RemoteException re) {" + lf);
		code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("            throw re;" + lf);
		code.append("        } catch(Throwable t) {" + lf);
		code.append("            Trace.errln(\"Throwable - \" + t.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("        }" + lf);
		code.append("    }" + lf + lf);

		// add virtual fk references
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(field.fk)
			{
				String fk_tmp_name = null;
				String fk_tmp_package = null;
				String fk_tmp_pk = null;
				String fk_tmp_type = null;
				boolean found = false;

				// find where the reference to this fk
				for(int k=0; k<ejbs.size(); k++)
				{
					ejb_tmp = (EJB)ejbs.elementAt(k);
					for(int a=0; a<ejb_tmp.fields.size(); a++)
					{
						field_tmp = (FIELD)ejb_tmp.fields.elementAt(a);
						if(field_tmp.pk)
						{
							fk_tmp_pk = field_tmp.name;
							fk_tmp_type = field_tmp.type;
						}
						if(field_tmp.pk && field_tmp.db_name.equals(field.db_name))
						{
							fk_tmp_package = ejb_tmp.getPackage();
							fk_tmp_name = ejb_tmp.name;
							found = true;
						}
						if(found) break;
					}
					if(found) break;
				}
				if(fk_tmp_name == null)
				{
					if(!"".equals(field.db_alias))
					{
						found = false;
						// find where the reference to this fk
						for(int k=0; k<ejbs.size(); k++)
						{
							ejb_tmp = (EJB)ejbs.elementAt(k);
							for(int a=0; a<ejb_tmp.fields.size(); a++)
							{
								field_tmp = (FIELD)ejb_tmp.fields.elementAt(a);
								if(field_tmp.pk && field_tmp.db_name.equals(field.db_alias))
								{
									fk_tmp_package = ejb_tmp.getPackage();
									fk_tmp_name = ejb_tmp.name;
									found = true;
								}
								if(found) break;
							}
							if(found) break;
						}
						if(fk_tmp_name == null)
							System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
					}
					else
						System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
				}
				code.append(
					"    /**" + lf +
					"     * Returns the foreign key for " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name) + lf +
					"     * @return A wrapper object for the foreign key" + lf +
					"     */" + lf +
					"    public " + EJB.getPackage(fk_tmp_package) + "." + TextUtils.toSunClassName(fk_tmp_name) + " " + TextUtils.toSunMethodName("get_" + field.name) + "() throws BeanHomeFactoryException, RemoteException, FinderException {" + lf +
					"        try {" + lf
				);
				if(project.lazy.fks)
				{
					code.append("            if(this." + TextUtils.toSunMethodName(field.name) + "_dirty) {" + lf);
					if(field.nullable)
					{
						code.append("                if(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "() == null) {" + lf);
						code.append("                    this." + TextUtils.toSunMethodName(field.name) + " = null;" + lf);
						code.append("                } else {" + lf);
						code.append("                    this." + TextUtils.toSunMethodName(field.name) + " = " + TextUtils.toSunClassName(fk_tmp_name) + "Manager.findByPrimaryKey(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "()" + TextUtils.getJavaNativeFromObject(TextUtils.toJavaObjectFieldType(fk_tmp_type)) + ");" + lf);
						code.append("                }" + lf);
					}
					else
						code.append("                this." + TextUtils.toSunMethodName(field.name) + " = " + TextUtils.toSunClassName(fk_tmp_name) + "Manager.findByPrimaryKey(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "());" + lf);

					code.append(
						"                this." + TextUtils.toSunMethodName(field.name) + "_dirty = false;" + lf +
						"            }" + lf +
						"            return this." + TextUtils.toSunMethodName(field.name) + ";" + lf
					);
				}
				else
				{
					code.append(
						"            return " + TextUtils.toSunClassName(fk_tmp_name) + "Manager.findByPrimaryKey(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "());" + lf
					);
				}
				code.append(
					"        } catch(BeanHomeFactoryException bhfe) {" + lf +
					"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw bhfe;" + lf +
					"        } catch(RemoteException re) {" + lf +
					"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw re;" + lf +
					"        } catch(FinderException fe) {" + lf +
					"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw fe;" + lf +
					"        }" + lf +
					"    }" + lf + lf
				);

				code.append(
					"    /**" + lf +
					"     * Set's the internal field value with the FK Wrapper id" + lf +
					"     * @param wrapper A wrapper object to be the new foreign key" + lf +
					"     */" + lf +
					"    public void " + TextUtils.toSunMethodName("set_" + field.name) + "(" + EJB.getPackage(fk_tmp_package) + "." + TextUtils.toSunClassName(fk_tmp_name) + " wrapper) throws DisconnectedBeanException, " + (field.nullable==true?"":"MandatoryFieldException, ") + "RemoteException {" + lf +
					"        try {" + lf
				);
				code.append("        if(!this._connected) {" + lf);
				code.append("            Trace.errln(\"DisconnectedBeanException - " + TextUtils.toSunClassName(ejb.name) + " is disconnected\", Trace.SEVERE, true);" + lf);
				code.append("            throw new DisconnectedBeanException(\"" + TextUtils.toSunClassName(ejb.name) + " is disconnected!\");" + lf);
				code.append("        }" + lf);
				if(field.nullable)
				{
					code.append("            if(wrapper == null) this." + TextUtils.toSunMethodName("set_" + field.name + "_id") + "(null);" + lf);
					code.append("            else this." + TextUtils.toSunMethodName("set_" + field.name + "_id") + "(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(fk_tmp_type), "wrapper." + TextUtils.toSunMethodName("get_" + fk_tmp_pk) + "()") + ");" + lf);
				}
				else
					code.append("            this." + TextUtils.toSunMethodName("set_" + field.name + "_id") + "(wrapper." + TextUtils.toSunMethodName("get_" + fk_tmp_pk) + "());" + lf);

				if(!field.nullable)
				{
					code.append(
						"        } catch(MandatoryFieldException mfe) {" + lf +
						"            Trace.errln(\"MandatoryFieldException - \" + mfe.getMessage(), Trace.SEVERE, true);" + lf +
						"            throw mfe;" + lf
					);
				}
				code.append(
					"        } catch(RemoteException re) {" + lf +
					"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw re;" + lf +
					"        }" + lf +
					"    }" + lf + lf
				);
			}
		}

		// Generate all the collections
		for(int j=0; j<ejb.collections.size(); j++)
		{
			pk_package = null;
			col_tmp = (COLLECTION)ejb.collections.elementAt(j);
			for(int k=0;k<ejbs.size(); k++)
			{
				ejb_tmp = (EJB)ejbs.elementAt(k);
				if(col_tmp.obj_name.equals(ejb_tmp.name))
				{
					pk_package = ejb_tmp.getPackage();
					break;
				}
			}

			code.append(
				"    /**" + lf +
				"     * Local copy of remote Collection of " + TextUtils.toSunClassName(col_tmp.obj_name)  + "Remote for Lazy Loading." + lf +
				"     * This means that this bean will only load once this property until there's a update." + lf +
				"     */" + lf +
				"    public " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] " + TextUtils.toSunMethodName("get_" + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias()))) + "() throws BeanHomeFactoryException, RemoteException, FinderException {" + lf +
				"        try {" + lf
			);
			if(project.lazy.collections)
			{
				code.append(
					"            if(this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "_dirty) {" + lf
				);

				if(!TextUtils.isNative(TextUtils.toJavaFieldType(ejb.getPkType())))
				{
					code.append("                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = " + TextUtils.toSunClassName(ejb.name) + "Manager." + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(col_tmp.getAlias())) + "(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(TextUtils.toJavaFieldType(ejb.getPkType())), " this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "()") + ");" + lf);
				}
				else
				{
					code.append("                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = " + TextUtils.toSunClassName(ejb.name) + "Manager." + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(col_tmp.getAlias())) + "(this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf);
				}
				code.append(
					"                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "_dirty = false;" + lf +
					"            }" + lf +
					"            return this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + ";" + lf
				);
			}
			else
			{
				if(!TextUtils.isNative(TextUtils.toJavaFieldType(ejb.getPkType())))
				{
					code.append("                return " + TextUtils.toSunClassName(ejb.name) + "Manager." + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(col_tmp.getAlias())) + "(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(ejb.getPkType()), " this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "()") + ");" + lf);
				}
				else
				{
					code.append("                return " + TextUtils.toSunClassName(ejb.name) + "Manager." + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(col_tmp.getAlias())) + "(this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf);
				}
			}
			code.append(
				"        } catch(BeanHomeFactoryException bhfe) {" + lf +
				"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw bhfe;" + lf +
				"        } catch(RemoteException re) {" + lf +
				"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw re;" + lf +
				"        } catch(FinderException fe) {" + lf +
				"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw fe;" + lf +
				"        }" + lf +
				"    }" + lf + lf
			);
		}

		code.append("    /**" + lf);
		code.append("     * Front an ejb with this wrapper" + lf);
		code.append("     * this constructor will be called by the Bean Manager" + lf);
		code.append("     *" + lf);
		code.append("     */" + lf);
		code.append("    protected " + TextUtils.toSunClassName(ejb.name) + "(" + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote ejb) {" + lf);
		code.append("        this.ejb = ejb;" + lf);
		code.append("        this._connected = true;" + lf);
		code.append("    }" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Disconnects this session bean from the entity/command" + lf);
		code.append("     * this should be used when you know that you'll never" + lf);
		code.append("     * call any setter on the bean. When you disconnect this" + lf);
		code.append("     * bean, you're releasing the openejb server resources to" + lf);
		code.append("     * other requests." + lf);
		code.append("     */" + lf);
		code.append("    public void disconnect() {" + lf);
		code.append("        this._connected = false;" + lf);
		code.append("        this.ejb = null;" + lf);
		code.append("    }" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Cleanup for this object" + lf);
		code.append("     */" + lf);
		code.append("    protected void finalize() throws Throwable {" + lf);
		code.append("        // TODO: Your extra clean up code here..." + lf);
		code.append("        this._connected = false;" + lf);
		code.append("        this.ejb = null;" + lf);
		if(project.lazy.fields)
		{
			code.append("        // Cleanup Fields references" + lf);
			// Cleanup all the fields
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(field.nullable)
					code.append("        this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = null;" + lf);
				else
					code.append("        this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.getInitialValueFor(TextUtils.toJavaFieldType(field.type)) + ";" + lf);
			}
		}

		if(project.lazy.fks)
		{
			code.append("        // Cleanup FK's references" + lf);
			// Cleanup all the FKs
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(field.fk)
				{
					code.append(
						"        this." + TextUtils.toSunMethodName(field.name) + " = null;" + lf
					);
				}
			}
		}

		if(project.lazy.collections)
		{
			// cleanup all the collections
			for(int j=0; j<ejb.collections.size(); j++)
			{
				pk_package = null;
				col_tmp = (COLLECTION)ejb.collections.elementAt(j);
				for(int k=0;k<ejbs.size(); k++)
				{
					ejb_tmp = (EJB)ejbs.elementAt(k);
					if(col_tmp.obj_name.equals(ejb_tmp.name))
					{
						pk_package = ejb_tmp.getPackage();
						break;
					}
				}

				code.append(
				    "        // ensure that there's no reference in memory so garbage collector reduces memory usage" + lf +
					"        if(" + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " != null) {" + lf +
					"            for(int i=0; i<" + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + ".length; i++) {" + lf +
					"                " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "[i] = null;" + lf +
					"            }" + lf +
					"        }" + lf +
					"        " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = null;" + lf
				);
			}
		}

		code.append("    }" + lf + lf);
		code.append("}");

		return code.toString();
	}

	private String jdo_wraper_bean(EJB ejb)
	{
		FIELD field = null;
		FIELD field_tmp = null;

		StringBuffer code = new StringBuffer();
		code.append(getHeader());
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.getPackage() + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.RemoveException;" + lf);
		code.append("import javax.ejb.EJBException;" + lf);
		code.append("import ejb.util.Trace;" + lf);
		code.append("import ejb.util.BeanHomeFactory;" + lf);
		code.append("import ejb.exception.BeanHomeFactoryException;" + lf);
		code.append("import ejb.exception.MandatoryFieldException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.getPackage() + "." + TextUtils.toSunClassName(ejb.name) + "" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * @version " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + " {" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Internal reference to Remote Object" + lf);
		code.append("     */" + lf);
		code.append("    private " + ejb.getRemotePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Remote ejb = null;" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Reference to a Home interface factory" + lf);
		code.append("     */" + lf);
		code.append("    private static BeanHomeFactory factory = null;" + lf + lf);

		// if we want lazy FK
		if(project.lazy.fks)
		{
			for(int i=0; i<ejb.fields.size(); i++)
			{
				field = (FIELD)ejb.fields.elementAt(i);
				if(field.fk)
				{
					String fk_tmp_name = null;
					String fk_tmp_package = null;
					EJB ejb_tmp = null;
					COLLECTION col_tmp = null;
					boolean found = false;

					// find where the reference to this fk
					for(int j=0; j<ejbs.size(); j++)
					{
						ejb_tmp = (EJB)ejbs.elementAt(j);
						for(int k=0; k<ejb_tmp.fields.size(); k++)
						{
							field_tmp = (FIELD)ejb_tmp.fields.elementAt(k);
							if(field_tmp.pk && field_tmp.db_name.equals(field.db_name))
							{
								fk_tmp_package = ejb_tmp.getPackage();
								fk_tmp_name = ejb_tmp.name;
								found = true;
							}
							if(found) break;
						}
						if(found) break;
					}
					if(fk_tmp_name == null)
					{
						if(!"".equals(field.db_alias))
						{
							found = false;
							// find where the reference to this fk
							for(int a=0; a<ejbs.size(); a++)
							{
								ejb_tmp = (EJB)ejbs.elementAt(a);
								for(int b=0; b<ejb_tmp.fields.size(); b++)
								{
									field_tmp = (FIELD)ejb_tmp.fields.elementAt(b);
									if(field_tmp.pk && field_tmp.db_name.equals(field.db_alias))
									{
										fk_tmp_package = ejb_tmp.getPackage();
										fk_tmp_name = ejb_tmp.name;
										found = true;
									}
									if(found) break;
								}
								if(found) break;
							}
							if(fk_tmp_name == null)
								System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
						}
						else
							System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
					}
					code.append(
						"    /**" + lf +
						"     * Local copy of remote field " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name) + " for Lazy Loading." + lf +
						"     * This means that this bean will only load once this property until there's a update." + lf +
						"     */" + lf +
						"    private " + EJB.getPackage(fk_tmp_package) + "." + TextUtils.toSunClassName(fk_tmp_name) + " " + TextUtils.toSunMethodName(field.name) + " = null;" + lf + lf +

						"    /**" + lf +
						"     * Dirty flag for field " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name) + " for Lazy Loading." + lf +
						"     */" + lf +
						"    private boolean " + TextUtils.toSunMethodName(field.name) + "_dirty = true;" + lf + lf
					);
				}
			}
		}

		COLLECTION col_tmp = null;
		EJB ejb_tmp = null;
		String pk_package = null;

		if(project.lazy.collections)
		{
			// Generate all the collections
			for(int j=0; j<ejb.collections.size(); j++)
			{
				pk_package = null;
				col_tmp = (COLLECTION)ejb.collections.elementAt(j);

				for(int k=0;k<ejbs.size(); k++)
				{
					ejb_tmp = (EJB)ejbs.elementAt(k);
					if(col_tmp.obj_name.equals(ejb_tmp.name))
					{
						pk_package = ejb_tmp.getPackage();
						break;
					}
				}

				code.append(
					"    /**" + lf +
					"     * Local copy of remote Collection of " + TextUtils.toSunClassName(col_tmp.obj_name)  + "Remote for Lazy Loading." + lf +
					"     * This means that this bean will only load once this property until there's a update." + lf +
					"     */" + lf +
					"    private " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + " " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = null;" + lf + lf +

					"    /**" + lf +
					"     * Dirty flag for collection " + TextUtils.toSunClassName(col_tmp.obj_name) + lf +
					"     */" + lf +
					"    private boolean " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "_dirty = true;" + lf + lf
				);
			}
		}

		// setter's
		// setter's aren't managed by lazy loading because we don't want to
		// make the database inconsistent
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.ro)
			{
				code.append("    /**" + lf);
	 			code.append("     * Setter for field " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + lf);
	 			code.append("     * @param " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " the new value for this field" + lf);
				code.append("     */" + lf);

				if(field.nullable)
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + " {" + lf);
				else
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + ((TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))?"":", MandatoryFieldException") + " {" + lf);

				code.append("        try {" + lf);

				if(field.nullable)
		 		{
		 			if(!TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
		 			{
						code.append("            // both internal and new fields are null, leave it as it is..." + lf);
						code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == null && " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() == null) return;" + lf);
					}
					code.append("            // both internal and new fields are equal, leave it as it is..." + lf);
					if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
					{
						code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "()) return;" + lf);
					}
					else
					{
						code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " != null && " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ".equals(" + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "())) return;" + lf);
					}
				}
		 		else
				{
					if(!TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
					{
						code.append("            // since the db field can't be null we won't allow you to set it null..." + lf);
						code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == null) throw new MandatoryFieldException(\"'" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + "' can't be null.\");" + lf);
					}
					code.append("            // both internal and new fields are equal, leave it as it is..." + lf);
					if(TextUtils.isNative(TextUtils.toJavaFieldType(field.type)))
						code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + " == " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "()) return;" + lf);
					else
						code.append("            if(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ".equals(" + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "())) return;" + lf);
		 		}
				code.append("            ejb." + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ");" + lf);
				code.append("        } catch(RemoteException re) {" + lf);
				code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
				code.append("            throw re;" + lf);
				code.append("        }" + lf);
				code.append("    }" + lf);
				code.append(lf);
			}
		}
		code.append(lf);

		// getter's
		// if we want lazy loading for fields we will only load when the field is dirty
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			code.append("    /**" + lf);
			code.append("     * Getter for field " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + lf);
			code.append("     * @return the value for the field" + lf);
			code.append("     */" + lf);

			if(field.nullable)
				code.append("    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException {" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException {" + lf);
			code.append("        try {" + lf);
			code.append("            return ejb." + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "()" + ";" + lf);
			code.append("        } catch(RemoteException re) {" + lf);
			code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
			code.append("            throw re;" + lf);
			code.append("        }" + lf);
			code.append("    }" + lf);
			code.append(lf);
		}
		code.append(lf);

		code.append("    /**" + lf);
		code.append("     * This method will phisically remove the Bean from the persistent storage" + lf);
		code.append("     *" + lf);
		code.append("     */" + lf);
		code.append("    public void remove() throws RemoteException {" + lf);
		code.append("        try {" + lf);
		code.append("            this.ejb.delete();" + lf);
		code.append("        } catch(RemoteException re) {" + lf);
		code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("            throw re;" + lf);
		code.append("        } catch(Throwable t) {" + lf);
		code.append("            Trace.errln(\"Throwable - \" + t.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("        }" + lf);
		code.append("    }" + lf + lf);

		code.append(
			"    /**" + lf +
			"     * This method will update the Bean into the persistent storage." + lf +
			"     * Use this ONLY if you need to flush the update to persistent storage," + lf +
			"     * the EJB bean under this layer handles all the updates transparently however" + lf +
			"     * it only changes the values when the bean is discarded or the rowset pointer" + lf +
			"     * changes." + lf +
			"     * This operation is quite expensive in matter of time and processing." + lf +
			"     */" + lf +
			"    public void update() throws RemoteException, EJBException {" + lf +
			"        try {" + lf +
			"            ejb.update();" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        } catch(EJBException ejbe) {" + lf +
			"            Trace.errln(\"EJBException - \" + ejbe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw ejbe;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append(
			"    /**" + lf +
			"     * Moves the pointer to the next row" + lf +
			"     *" + lf +
			"     * @return is it possible to move to next row" + lf +
			"     */" + lf +
			"    public boolean next() throws RemoteException {" + lf +
			"    	try {" + lf +
			"	    	return ejb.next();" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append(
			"    /**" + lf +
			"     * Returns the size of our rowset" + lf +
			"     *" + lf +
			"     * @return size of rowset" + lf +
			"     */" + lf +
			"    public int size() throws RemoteException {" + lf +
			"    	try {" + lf +
			"    		return ejb.size();" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		// add virtual fk references
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(field.fk)
			{
				String fk_tmp_name = null;
				String fk_tmp_package = null;
				String fk_tmp_pk = null;
				String fk_tmp_type = null;
				boolean found = false;

				// find where the reference to this fk
				for(int k=0; k<ejbs.size(); k++)
				{
					ejb_tmp = (EJB)ejbs.elementAt(k);
					for(int a=0; a<ejb_tmp.fields.size(); a++)
					{
						field_tmp = (FIELD)ejb_tmp.fields.elementAt(a);
						if(field_tmp.pk)
						{
							fk_tmp_pk = field_tmp.name;
							fk_tmp_type = field_tmp.type;
						}
						if(field_tmp.pk && field_tmp.db_name.equals(field.db_name))
						{
							fk_tmp_package = ejb_tmp.getPackage();
							fk_tmp_name = ejb_tmp.name;
							found = true;
						}
						if(found) break;
					}
					if(found) break;
				}
				if(fk_tmp_name == null)
				{
					if(!"".equals(field.db_alias))
					{
						found = false;
						// find where the reference to this fk
						for(int k=0; k<ejbs.size(); k++)
						{
							ejb_tmp = (EJB)ejbs.elementAt(k);
							for(int a=0; a<ejb_tmp.fields.size(); a++)
							{
								field_tmp = (FIELD)ejb_tmp.fields.elementAt(a);
								if(field_tmp.pk && field_tmp.db_name.equals(field.db_alias))
								{
									fk_tmp_package = ejb_tmp.getPackage();
									fk_tmp_name = ejb_tmp.name;
									found = true;
								}
								if(found) break;
							}
							if(found) break;
						}
						if(fk_tmp_name == null)
							System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
					}
					else
						System.err.println("FK reference (" + ejb.db_name + "." + field.db_name + ") not found...");
				}
				code.append(
					"    /**" + lf +
					"     * Returns the foreign key for " + TextUtils.toSunClassName(ejb.name) + "Remote." + TextUtils.toSunMethodName(field.name) + lf +
					"     * @return A wrapper object for the foreign key" + lf +
					"     */" + lf +
					"    public " + EJB.getPackage(fk_tmp_package) + "." + TextUtils.toSunClassName(fk_tmp_name) + " " + TextUtils.toSunMethodName("get_" + field.name) + "() throws BeanHomeFactoryException, RemoteException, CreateException, FinderException {" + lf +
					"        try {" + lf
				);
				if(project.lazy.fks)
				{
					code.append("            if(this." + TextUtils.toSunMethodName(field.name) + "_dirty) {" + lf);
					if(field.nullable)
					{
						code.append("                if(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "() == null) {" + lf);
						code.append("                    this." + TextUtils.toSunMethodName(field.name) + " = null;" + lf);
						code.append("                } else {" + lf);
						code.append("                    this." + TextUtils.toSunMethodName(field.name) + " = new " + TextUtils.toSunClassName(fk_tmp_name) + "();" + lf);
						code.append("                    this." + TextUtils.toSunMethodName(field.name) + ".findByPrimaryKey(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "()" + TextUtils.getJavaNativeFromObject(TextUtils.toJavaObjectFieldType(fk_tmp_type)) + ");" + lf);
						code.append("                }" + lf);
					}
					else
					{
						code.append("                this." + TextUtils.toSunMethodName(field.name) + " = new " + TextUtils.toSunClassName(fk_tmp_name) + "();" + lf);
						code.append("                this." + TextUtils.toSunMethodName(field.name) + ".findByPrimaryKey(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "());" + lf);
					}
					code.append(
						"                this." + TextUtils.toSunMethodName(field.name) + "_dirty = false;" + lf +
						"            }" + lf +
						"            return this." + TextUtils.toSunMethodName(field.name) + ";" + lf
					);
				}
				else
				{
					code.append(
						"            return " + TextUtils.toSunClassName(fk_tmp_name) + "Manager.findByPrimaryKey(this." + TextUtils.toSunMethodName("get_" + field.name + "_id") + "());" + lf
					);
				}
				code.append(
					"        } catch(BeanHomeFactoryException bhfe) {" + lf +
					"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw bhfe;" + lf +
					"        } catch(RemoteException re) {" + lf +
					"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw re;" + lf +
					"        } catch(FinderException fe) {" + lf +
					"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw fe;" + lf +
					"        } catch(CreateException ce) {" + lf +
					"            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw ce;" + lf +
					"        }" + lf +
					"    }" + lf + lf
				);

				code.append(
					"    /**" + lf +
					"     * Set's the internal field value with the FK Wrapper id" + lf +
					"     * @param wrapper A wrapper object to be the new foreign key" + lf +
					"     */" + lf +
					"    public void " + TextUtils.toSunMethodName("set_" + field.name) + "(" + EJB.getPackage(fk_tmp_package) + "." + TextUtils.toSunClassName(fk_tmp_name) + " wrapper) throws " + (field.nullable==true?"":"MandatoryFieldException, ") + "RemoteException {" + lf +
					"        try {" + lf
				);
				if(field.nullable)
				{
					code.append("            if(wrapper == null) this." + TextUtils.toSunMethodName("set_" + field.name + "_id") + "(null);" + lf);
					code.append("            else this." + TextUtils.toSunMethodName("set_" + field.name + "_id") + "(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(fk_tmp_type), "wrapper." + TextUtils.toSunMethodName("get_" + fk_tmp_pk) + "()") + ");" + lf);
				}
				else
				{
					code.append("            if(wrapper == null) throw new MandatoryFieldException(\"'" + TextUtils.toSunMethodName(field.name) + "' can't be null.\");" + lf);
					code.append("            this." + TextUtils.toSunMethodName("set_" + field.name + "_id") + "(wrapper." + TextUtils.toSunMethodName("get_" + fk_tmp_pk) + "());" + lf);
				}

				if(!field.nullable)
				{
					code.append(
						"        } catch(MandatoryFieldException mfe) {" + lf +
						"            Trace.errln(\"MandatoryFieldException - \" + mfe.getMessage(), Trace.SEVERE, true);" + lf +
						"            throw mfe;" + lf
					);
				}
				code.append(
					"        } catch(RemoteException re) {" + lf +
					"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
					"            throw re;" + lf +
					"        }" + lf +
					"    }" + lf + lf
				);
			}
		}

		String method = null;
		// Generate all the collections
		for(int j=0; j<ejb.collections.size(); j++)
		{
			pk_package = null;
			col_tmp = (COLLECTION)ejb.collections.elementAt(j);
			for(int k=0;k<ejbs.size(); k++)
			{
				ejb_tmp = (EJB)ejbs.elementAt(k);
				if(col_tmp.obj_name.equals(ejb_tmp.name))
				{
					pk_package = ejb_tmp.getPackage();
					// find the relation fk
					for(int a=0; a<ejb_tmp.fields.size(); a++)
					{
						field_tmp = (FIELD)ejb_tmp.fields.elementAt(a);
						if(field_tmp.fk)
						{
							if(field_tmp.db_alias.equals(ejb.getPkDbName()) || field_tmp.db_name.equals(ejb.getPkDbName()))
							{
								method = field_tmp.name;
								break;
							}
						}
					}
					break;
				}
			}

			code.append(
				"    /**" + lf +
				"     * Local copy of remote Collection of " + TextUtils.toSunClassName(col_tmp.obj_name)  + "Remote for Lazy Loading." + lf +
				"     * This means that this bean will only load once this property until there's a update." + lf +
				"     */" + lf +
				"    public " + EJB.getPackage(pk_package) + "." + TextUtils.toSunClassName(col_tmp.obj_name) + " " + TextUtils.toSunMethodName("get_" + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias()))) + "() throws BeanHomeFactoryException, RemoteException, FinderException, CreateException {" + lf +
				"        try {" + lf
			);
			if(project.lazy.collections)
			{
				code.append(
					"            if(this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "_dirty) {" + lf
				);

				if(!TextUtils.isNative(TextUtils.toJavaFieldType(ejb.getPkType())))
				{
					code.append("                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = new " + TextUtils.toSunClassName(ejb.name) + "();" + lf);
					code.append("                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + ".findBy" + TextUtils.toSunClassName(method) + "(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(TextUtils.toJavaFieldType(ejb.getPkType())), " this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "()") + ");" + lf);
				}
				else
				{
					code.append(
						"                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = new " + TextUtils.toSunClassName(col_tmp.obj_name) + "();" + lf +
						"                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + ".findBy" + TextUtils.toSunClassName(method) + "(this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf
					);
				}
				code.append(
					"                this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + "_dirty = false;" + lf +
					"            }" + lf +
					"            return this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + ";" + lf
				);
			}
			else
			{
				if(!TextUtils.isNative(TextUtils.toJavaFieldType(ejb.getPkType())))
				{
					code.append("                return " + TextUtils.toSunClassName(ejb.name) + "Manager." + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(col_tmp.getAlias())) + "(" + TextUtils.getJavaObjectConstructorForNative(TextUtils.toJavaFieldType(ejb.getPkType()), " this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "()") + ");" + lf);
				}
				else
				{
					code.append("                return " + TextUtils.toSunClassName(ejb.name) + "Manager." + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(col_tmp.getAlias())) + "(this." + TextUtils.toSunMethodName("get_" + ejb.getPkName()) + "());" + lf);
				}
			}
			code.append(
				"        } catch(BeanHomeFactoryException bhfe) {" + lf +
				"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw bhfe;" + lf +
				"        } catch(RemoteException re) {" + lf +
				"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw re;" + lf +
				"        } catch(FinderException fe) {" + lf +
				"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw fe;" + lf +
				"        } catch(CreateException ce) {" + lf +
				"            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf +
				"            throw ce;" + lf +
				"        }" + lf +
				"    }" + lf + lf
			);
		}

		code.append("    /**" + lf);
		code.append("     * Java Bean to handle all the low level EJB operations for " + TextUtils.toSunClassName(ejb.name) + "EJB entity" + lf);
		code.append("     *" + lf);
		code.append("     */" + lf);
		code.append("    public " + TextUtils.toSunClassName(ejb.name) + "() throws BeanHomeFactoryException, CreateException, RemoteException {" + lf);
		code.append("        try {" + lf);
		code.append("            if(factory == null) init();" + lf);
		code.append("            " + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.getHomePackage() + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) +"\");" + lf);
		code.append("            ejb = home.create();" + lf);
		code.append("        } catch(BeanHomeFactoryException bhfe) {" + lf);
		code.append("            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("            throw bhfe;" + lf);
		code.append("        } catch(CreateException ce) {" + lf);
		code.append("            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("            throw ce;" + lf);
		code.append("        } catch(RemoteException re) {" + lf);
		code.append("            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf);
		code.append("            throw re;" + lf);
		code.append("        }" + lf);
		code.append("    }" + lf + lf);

		code.append(
			"    /**" + lf +
			"     * Bean Manager Static initializer" + lf +
			"     */" + lf +
			"    private static void init() throws BeanHomeFactoryException {" + lf +
			"        try {" + lf +
			"            factory = BeanHomeFactory.getFactory();" + lf +
			"        } catch(BeanHomeFactoryException bhfe) {" + lf +
			"            Trace.errln(\"BeanHomeFactoryException - \" + bhfe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw bhfe;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		code.append(
			"    /**" + lf +
			"     * Finds all entries in the storage for your " + TextUtils.toSunClassName(ejb.name) + " entities" + lf +
			"     */" + lf +
			"	 public void findAll() throws FinderException, RemoteException {" + lf +
			"        try {" + lf +
			"	         ejb.selectAll();" + lf +
			"        } catch(FinderException fe) {" + lf +
			"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw fe;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf + lf
		);

		StringBuffer create_params = new StringBuffer();
		StringBuffer create_simple_params = new StringBuffer();
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(!field.nullable && !field.pk)
			{
				create_params.append( TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
				create_simple_params.append( TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ", ");
			}
		}
		// cut the extra ', ' chars
		if(create_params.length() > 0)
		{
			create_params.setLength( create_params.length() - 2 );
			create_simple_params.setLength( create_simple_params.length() - 2 );
		}

		code.append("    /**" + lf);
		code.append("     * Creates a row in the persistent storage system of type " + TextUtils.toSunClassName(ejb.name) + lf);
		code.append("     * " + lf);
		// define the javadoc
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(!field.nullable && !field.pk)
				code.append("     * @param " + TextUtils.toSunParameterName(field.name) + (field.fk?"Id":"") + " " + lf);
		}
		code.append("     */" + lf);
		code.append("    public void create(" + create_params.toString() +") throws CreateException, RemoteException {" + lf);
		code.append(
			"        try {" + lf +
			"    	     ejb.insert(" + create_simple_params.toString() +");" + lf +
			"        } catch(CreateException ce) {" + lf +
			"            Trace.errln(\"CreateException - \" + ce.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw ce;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf +lf
		);

		// ready made methods for all beans
		code.append("    /**" + lf);
		code.append("     * Finds an entry in the storage for your JDO " + TextUtils.toSunClassName(ejb.name) + lf);
		code.append("     * " + lf);
		code.append("     * @param key Primary Key that identifies the entry" + lf);
		code.append("     */" + lf);
		code.append(
			"    public void findByPrimaryKey(" + TextUtils.toJavaFieldType(ejb.getPkType()) + " key) throws FinderException, RemoteException {" + lf +
			"        try {" + lf +
			"    	     ejb.selectByPrimaryKey(key);" + lf +
			"        } catch(FinderException fe) {" + lf +
			"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw fe;" + lf +
			"        } catch(RemoteException re) {" + lf +
			"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
			"            throw re;" + lf +
			"        }" + lf +
			"    }" + lf +lf
		);

		EJB ref_ejb = null;
		for(int i=0; i<ejbs.size(); i++)
		{
			COLLECTION col = null;
			// add 1 to Many RellationShips
			ref_ejb = (EJB)ejbs.elementAt(i);
			for(int j=0; j<ref_ejb.collections.size(); j++)
			{
				col = (COLLECTION)ref_ejb.collections.elementAt(j);
				// found a 1 to Many RelationShip
				if(ejb.name.equals(col.obj_name))
				{
					boolean found = false;
					code.append("    /**" + lf);
					code.append("     * Finds all entries in the storage for your JDO's " + TextUtils.toSunClassName(ejb.name) + lf);
					code.append("     * Where " + TextUtils.toSunClassName(ref_ejb.name) + " is the parameter used in query" + lf);
					code.append("     * @param key parameter to use in Query" + lf);
					code.append("     */" + lf);
					// we need a fk to generate the name fo the finder, if no fk is found object name will be used instead
					if(ejb.name.equals(col.getAlias()))
					{
						for(int k=0; k<ref_ejb.fields.size(); k++)
						{
							field = (FIELD)ref_ejb.fields.elementAt(k);
							if(field.fk &&
								(
									field.db_name.equals(ejb.getPkDbName()) || field.db_alias.equals(ejb.getPkDbName())
								)
							)
							{
								code.append("    public void findBy" + TextUtils.toSunClassName(field.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException {" + lf);
								code.append(
									"        try {" + lf +
									"    	     ejb.selectBy" + TextUtils.toSunClassName(field.name) + "(key);" + lf +
									"        } catch(FinderException fe) {" + lf +
									"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
									"            throw fe;" + lf +
									"        } catch(RemoteException re) {" + lf +
									"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
									"            throw re;" + lf +
									"        }" + lf +
									"    }" + lf +lf
								);
								found = true;
								break;
							}
						}
						if(!found)
						{
							code.append("    public void findBy" + TextUtils.toSunClassName(ref_ejb.name) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException {" + lf);
							code.append(
								"        try {" + lf +
								"    	     ejb.selectBy" + TextUtils.toSunClassName(ref_ejb.name) + "(key);" + lf +
								"        } catch(FinderException fe) {" + lf +
								"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
								"            throw fe;" + lf +
								"        } catch(RemoteException re) {" + lf +
								"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
								"            throw re;" + lf +
								"        }" + lf +
								"    }" + lf +lf
							);
						}
					}
					else
					{
						code.append("    public void findBy" + TextUtils.toSunClassName(col.getAlias()) + "(" + TextUtils.toJavaFieldType(ref_ejb.getPkType()) + " key) throws FinderException, RemoteException {" + lf);
						code.append(
							"        try {" + lf +
							"    	     ejb.selectBy" + TextUtils.toSunClassName(col.getAlias()) + "(key);" + lf +
							"        } catch(FinderException fe) {" + lf +
							"            Trace.errln(\"FinderException - \" + fe.getMessage(), Trace.SEVERE, true);" + lf +
							"            throw fe;" + lf +
							"        } catch(RemoteException re) {" + lf +
							"            Trace.errln(\"RemoteException - \" + re.getMessage(), Trace.SEVERE, true);" + lf +
							"            throw re;" + lf +
							"        }" + lf +
							"    }" + lf +lf
						);
					}
				}
			}
		}

		code.append("    /**" + lf);
		code.append("     * Cleanup for this object" + lf);
		code.append("     */" + lf);
		code.append("    protected void finalize() throws Throwable {" + lf);
		code.append("        // TODO: Your extra clean up code here..." + lf);
		code.append("        if(this.ejb != null) this.ejb.remove();" + lf);
		code.append("        this.ejb = null;" + lf);

		if(project.lazy.fks)
		{
			code.append("        // Cleanup FK's references" + lf);
			// Cleanup all the FKs
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(field.fk)
				{
					code.append(
						"        if(this." + TextUtils.toSunMethodName(field.name) + " != null) this." + TextUtils.toSunMethodName(field.name) + ".finalize();" + lf +
						"        this." + TextUtils.toSunMethodName(field.name) + " = null;" + lf
					);
				}
			}
		}

		if(project.lazy.collections)
		{
			// cleanup all the collections
			for(int j=0; j<ejb.collections.size(); j++)
			{
				pk_package = null;
				col_tmp = (COLLECTION)ejb.collections.elementAt(j);
				for(int k=0;k<ejbs.size(); k++)
				{
					ejb_tmp = (EJB)ejbs.elementAt(k);
					if(col_tmp.obj_name.equals(ejb_tmp.name))
					{
						pk_package = ejb_tmp.getPackage();
						break;
					}
				}

				code.append(
				    "        // ensure that there's no reference in memory so garbage collector reduces memory usage" + lf +
					"        if(this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " != null) this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + ".finalize();" + lf +
					"        this." + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = null;" + lf
				);
			}
		}

		code.append("    }" + lf + lf);
		code.append("}");

		return code.toString();
	}

	private String test_jsp(EJB ejb)
	{
		StringBuffer code = new StringBuffer();

		code.append("<%@ page import=\"" + ejb.getPackage() + "." + TextUtils.toSunClassName(ejb.name) + "\"%>" + lf + lf);
		code.append("<jsp:useBean id=\"" + TextUtils.toSunParameterName(ejb.name + "_manager") + "\" scope=\"application\" class=\"" + ejb.getPackage() + "." + TextUtils.toSunClassName(ejb.name) + "Manager\" />" + lf + lf);
		code.append("<html>" + lf);
		code.append("<head>" + lf);
		code.append("	<title>openEJB BeanGen Test JSP for " + TextUtils.toSunClassName(ejb.name) + "</title>" + lf);
		code.append("</head>" + lf);
		code.append("<body>" + lf);
		code.append("<%" + lf);
		code.append("    " + TextUtils.toSunClassName(ejb.name) + " bean = " + TextUtils.toSunParameterName(ejb.name + "_manager") + ".findByPrimaryKey(1);" + lf);
		code.append("%>" + lf);
		code.append("</body>" + lf);
		code.append("</html>" + lf);

		return code.toString();
	}

	// The final method that will return the generated code in a jar stream.
	public void generate(OutputStream out) throws IOException
	{
		JarOutputStream jout = new JarOutputStream( out );
		JarEntry jentry = null;

		jentry = new JarEntry(project.unix_name + "/build.xml");
		jout.putNextEntry(jentry);
		jout.write(this.ant_makefile().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/entities.sql");
		jout.putNextEntry(jentry);
		jout.write(this.sql_model().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/tomcat/server.xml");
		jout.putNextEntry(jentry);
		jout.write(this.tomcat_server_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/tomcat/web.xml");
		jout.putNextEntry(jentry);
		jout.write(this.tomcat_webapp_web_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/openejb/openejb.conf");
		jout.putNextEntry(jentry);
		jout.write(this.openejb_conf_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/openejb/cmp_global_database.xml");
		jout.putNextEntry(jentry);
		jout.write(this.cmp_global_database_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/openejb/cmp_or_mapping.xml");
		jout.putNextEntry(jentry);
		jout.write(this.cmp_or_mapping_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/openejb/cmp_local_database.xml");
		jout.putNextEntry(jentry);
		jout.write(this.cmp_local_database_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/" + project.unix_name + "/ejb-jar.xml");
		jout.putNextEntry(jentry);
		jout.write(this.ejb_jar_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/conf/" + project.unix_name + "/openejb-jar.xml");
		jout.putNextEntry(jentry);
		jout.write(this.openejb_jar_xml().getBytes());

		EJB ejb = null;

		// boring code...
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			if(ejb.needsPkObject())
			{
				// pk object
				jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getPkPackage()) + TextUtils.toSunClassName(ejb.name) + "PK.java" );
				jout.putNextEntry(jentry);
				jout.write(this.pk( ejb ).getBytes());
			}

			// home interface
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getHomePackage()) + TextUtils.toSunClassName(ejb.name) + "Home.java" );
			jout.putNextEntry(jentry);
			jout.write(this.home_interface( ejb ).getBytes());

			// remote interface
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getRemotePackage()) + TextUtils.toSunClassName(ejb.name) + "Remote.java" );
			jout.putNextEntry(jentry);
			jout.write(this.remote_interface( ejb ).getBytes());

			// EJB
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getEJBPackage()) + TextUtils.toSunClassName(ejb.name) + "EJB.java" );
			jout.putNextEntry(jentry);
			jout.write(this.entity_bean( ejb ).getBytes());

			if(ejb.type == EJB.BMP || ejb.type == EJB.CMP)
			{
				// EJB Wrapper
				jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getPackage()) + TextUtils.toSunClassName(ejb.name) + ".java" );
				jout.putNextEntry(jentry);
				jout.write(this.wraper_bean( ejb ).getBytes());

				// Wrapper Manager
				jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getPackage()) + TextUtils.toSunClassName(ejb.name) + "Manager.java" );
				jout.putNextEntry(jentry);
				jout.write(this.manager_bean( ejb ).getBytes());
			}

			if(ejb.type == EJB.POJO)
			{
				// JDO Wrapper
				jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(ejb.getPackage()) + TextUtils.toSunClassName(ejb.name) + ".java" );
				jout.putNextEntry(jentry);
				jout.write(this.jdo_wraper_bean( ejb ).getBytes());
			}

			// JSP Sample
			jentry = new JarEntry(project.unix_name + "/src/web/" + TextUtils.toSunMethodName(ejb.name) + "_test.jsp");
			jout.putNextEntry(jentry);
			jout.write(this.test_jsp( ejb ).getBytes());
		}

		DAC dac = null;

		for(int i=0; i<dacs.size(); i++)
		{
			dac = (DAC)dacs.elementAt(i);

			// DAC Home
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(dac.getHomePackage()) + TextUtils.toSunClassName(dac.name) + "Home.java" );
			jout.putNextEntry(jentry);
			jout.write(this.home_interface( dac ).getBytes());

			// DAC Remote
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(dac.getRemotePackage()) + TextUtils.toSunClassName(dac.name) + "Remote.java" );
			jout.putNextEntry(jentry);
			jout.write(this.remote_interface( dac ).getBytes());

			// DAC
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(dac.getDACPackage()) + TextUtils.toSunClassName(dac.name) + "DAC.java" );
			jout.putNextEntry(jentry);
			jout.write(this.data_access_bean( dac ).getBytes());

			// DAC Wrapper
			jentry = new JarEntry(project.unix_name + "/src/java/" + TextUtils.toJarPath(dac.getPackage()) + TextUtils.toSunClassName(dac.name) + "Query.java" );
			jout.putNextEntry(jentry);
			jout.write(this.query_bean( dac ).getBytes());
		}

		// Logger
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/util/Trace.java");
		jout.putNextEntry(jentry);
		jout.write(this.commons_trace().getBytes());

		// EJB utilities (Home Factory)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/util/BeanHomeFactory.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_beanhome_factory().getBytes());

		// EJB utilities (Home Factory)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/exception/BeanHomeFactoryException.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_beanhome_factory_exception().getBytes());

		// EJB utilities (Entity Bean Adapter)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/core/EntityBeanAdapter.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_entity_bean_adapter().getBytes());

		// EJB utilities (Session Bean Adapter)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/core/SessionBeanAdapter.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_session_bean_adapter().getBytes());

		// EJB utilities (DBUtil)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/core/DBUtil.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_dbutil().getBytes());

		// EJB utilities (MandatoryField Exception)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/exception/MandatoryFieldException.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_mandatoryfield_exception().getBytes());

		// EJB utilities (DisconnectedBean Exception)
		jentry = new JarEntry(project.unix_name + "/src/java/ejb/exception/DisconnectedBeanException.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_disconnectedbean_exception().getBytes());

		jentry = new JarEntry(project.unix_name + "/src/java/ejb/exception/DACException.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_dacbean_exception().getBytes());
		jout.close();
	}

	// this is my file format parser
	protected void parse(String file) throws FileNotFoundException, IOException
	{
		// runtime temporary parser objectecs
		FIELD field = null;
		EJB ejb = null;
		RESOURCE res = null;
		DAC dac = null;
		PARAM param = null;

		BufferedReader in = null;
		String sz = null;
		StringTokenizer st = null;

		in = new BufferedReader ( new InputStreamReader(
			new FileInputStream( file )
		));

		while((sz = in.readLine()) != null)
		{
			if(sz != null)
			{
				int size = sz.length();
				while( (sz = sz.trim()).length() != size)
					size = sz.length();

				if(sz.length() != 0)
				{
					if(sz.charAt(0) != '#')
					{
						st = new StringTokenizer( sz, ":");
						// 1st token, command
						if(st.hasMoreTokens())
						{
							sz = st.nextToken();
							//System.out.println("Parsing " + sz + "...");
							if(sz.equalsIgnoreCase("BEAN"))
							{
								dac = null;
								ejb = null;
								ejb = new EJB();
								if(st.hasMoreTokens())
								{
									String tmp = st.nextToken();
									if(tmp.equalsIgnoreCase("BMP"))
										ejb.type = EJB.BMP;
									else if(tmp.equalsIgnoreCase("CMP"))
										ejb.type = EJB.CMP;
									else if(tmp.equalsIgnoreCase("POJO"))
										ejb.type = EJB.POJO;
								}
								if(st.hasMoreTokens()) ejb.db_name = st.nextToken();
								if(st.hasMoreTokens()) ejb.name = st.nextToken();
								if(st.hasMoreTokens()) ejb.app_package = st.nextToken();
								ejbs.addElement( ejb );
							}
							else if(sz.equalsIgnoreCase("FIELD"))
							{
								field = new FIELD();
								if(st.hasMoreTokens()) field.db_type = st.nextToken();
								if(st.hasMoreTokens()) field.db_name = st.nextToken();
								if(st.hasMoreTokens()) field.type = st.nextToken();
								if(st.hasMoreTokens()) field.name = st.nextToken();
								if(st.hasMoreTokens()) field.nullable = st.nextToken().equalsIgnoreCase("NULL");
								if(st.hasMoreTokens()) field.ro = st.nextToken().equalsIgnoreCase("RO");
								if(st.hasMoreTokens())
								{
									String tmp = st.nextToken();
									field.pk = tmp.equalsIgnoreCase("PK");
									field.fk = tmp.equalsIgnoreCase("FK") || tmp.equalsIgnoreCase("CK");
									field.ck = tmp.equalsIgnoreCase("CK");
								}
								ejb.fields.addElement( field );
							}
							else if(sz.equalsIgnoreCase("DESCRIPTION"))
							{
								if(ejb != null)
								{
									if("".equals(ejb.description))
										ejb.description = st.nextToken();
									else
										ejb.description += (lf + st.nextToken());
								}
								else if(dac != null)
								{
									if("".equals(dac.description))
										dac.description = st.nextToken();
									else
										dac.description += (lf + st.nextToken());
								}
							}
							else if(sz.equalsIgnoreCase("AUTHOR"))
							{
								if(ejb != null)
									ejb.author = st.nextToken();
								if(dac != null)
									dac.author = st.nextToken();
							}
							else if(sz.equalsIgnoreCase("RESOURCE"))
							{
								res = new RESOURCE();
								if(st.hasMoreTokens())
								{
									String type = st.nextToken();
									if("DATASOURCE".equalsIgnoreCase(type))
										res.type = RESOURCE.DATASOURCE;
								}
								if(st.hasMoreTokens()) res.name = st.nextToken();
								if(st.hasMoreTokens()) res.jndi = st.nextToken();
								if(st.hasMoreTokens()) res.engine = st.nextToken();
								if(st.hasMoreTokens()) res.key_gen = st.nextToken();
								this.resources.addElement( res );
							}
							else if(sz.equalsIgnoreCase("RESOURCE-LINK"))
							{
								if(st.hasMoreTokens())
								{
									String name = st.nextToken();
									res = null;
									boolean found = false;

									for(int i=0; i<resources.size(); i++)
									{
										res = (RESOURCE) resources.elementAt(i);
										if(name != null)
										{
											if(name.equals(res.name))
											{
												if(ejb != null)
													ejb.resources.addElement( res );
												if(dac != null)
													dac.resources.addElement( res );
												found = true;
												break;
											}
										}
									}

									if(!found)
										System.out.println("Ignoring undefined resource: " + name);
								}
							}
							else if(sz.equalsIgnoreCase("JNDI-BASEPATH"))
							{
								if(ejb != null)
									if(st.hasMoreTokens()) ejb.jndi_basepath = st.nextToken();
								if(dac != null)
									if(st.hasMoreTokens()) dac.jndi_basepath = st.nextToken();
							}
							else if(sz.equalsIgnoreCase("COLLECTION"))
							{
								COLLECTION col = new COLLECTION();
								if(st.hasMoreTokens())
								{
									String tmp = st.nextToken();
									if(tmp.equals("1-*")) col.type = COLLECTION.ONE_TO_MANY;
									else if(tmp.equals("*-*")) col.type = COLLECTION.MANY_TO_MANY;
									else System.err.println("Unknown relationship: " + tmp);
								}
								if(st.hasMoreTokens()) col.obj_name = st.nextToken();
								if(st.hasMoreTokens()) col.alias = st.nextToken();
								ejb.collections.addElement(col);
							}
							else if(sz.equalsIgnoreCase("PROJECT"))
							{
								if(st.hasMoreTokens()) project.unix_name = st.nextToken();
								if(st.hasMoreTokens()) project.name = st.nextToken();
							}
							else if(sz.equalsIgnoreCase("DB_ALIAS"))
							{
								if(st.hasMoreTokens()) field.db_alias = st.nextToken();
							}
							else if(sz.equalsIgnoreCase("CACHE"))
							{
								String tmp = null;
								while(st.hasMoreTokens())
								{
									tmp = st.nextToken();
									if(tmp.equalsIgnoreCase("COLLECTIONS")) project.lazy.collections = true;
									if(tmp.equalsIgnoreCase("FIELDS")) project.lazy.fields = true;
									if(tmp.equalsIgnoreCase("FKS")) project.lazy.fks = true;
								}
							}
							else if(sz.equalsIgnoreCase("CONFIG"))
							{
								String tmp = st.nextToken();
								if(tmp.equalsIgnoreCase("CATALINA"))
								{
									config.catalina_home = st.nextToken();
									while(st.hasMoreTokens())
										config.catalina_home += ":" + st.nextToken();
								}
								else if(tmp.equalsIgnoreCase("OPENEJB"))
								{
									config.openejb_home = st.nextToken();
									while(st.hasMoreTokens())
										config.openejb_home += ":" + st.nextToken();
								}
							}
							else if(sz.equalsIgnoreCase("DAC"))
							{
								dac = null;
								ejb = null;
								dac = new DAC();
								dac.name = st.nextToken();
								dac.app_package = st.nextToken();

								dacs.addElement(dac);
							}
							else if(sz.equalsIgnoreCase("QUERY"))
							{
								dac.query = st.nextToken();
								while(st.hasMoreTokens())
									dac.query += ":" + st.nextToken();
							}
							else if(sz.equalsIgnoreCase("PARAM"))
							{
								param = new PARAM();
								param.number = Integer.parseInt(st.nextToken());
								param.type = st.nextToken();
								param.name = st.nextToken();
								param.nullable = "null".equals(st.nextToken());
								if(st.hasMoreTokens()) param.sql_type = st.nextToken();
								dac.params.addElement( param );
							}
							else if(sz.equalsIgnoreCase("COLUMN"))
							{
								param = new PARAM();
								param.number = Integer.parseInt(st.nextToken());
								param.type = st.nextToken();
								param.name = st.nextToken();
								param.nullable = "null".equals(st.nextToken());
								dac.columns.addElement( param );
							}
							else
							{
								System.out.println("Unknown tag: " + sz);
							}
						}
					}
				}
			}
		}
		in.close();
	}


	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	// Utility method to load some "standard" classes
	private String loadResource(InputStream in) throws IOException
	{
		StringBuffer code = new StringBuffer();
		int c = -1;
		while((c = in.read()) != -1)
			code.append((char)c);
		in.close();
		return code.toString();
	}

	// ready made classes
	private String commons_trace() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/Trace.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_beanhome_factory() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/BeanHomeFactory.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_dbutil() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/DBUtil.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_dacbean_exception() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/DACException.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_beanhome_factory_exception() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/BeanHomeFactoryException.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_entity_bean_adapter() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/EntityBeanAdapter.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_session_bean_adapter() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/SessionBeanAdapter.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_mandatoryfield_exception() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/MandatoryFieldException.java");
		return loadResource(in);
	}

	// ready made classes
	private String commons_disconnectedbean_exception() throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/beangen/templates/DisconnectedBeanException.java");
		return loadResource(in);
	}

	// ready made makefile
	private String ant_makefile() throws IOException
	{
		StringBuffer code = new StringBuffer();

		code.append(
			"<project name=\"" + project.name + "\" default=\"compile\" basedir=\".\" >" + lf +
			"  <property name=\"openejb.home\" value=\"" + config.openejb_home + "\" />" + lf +
			"  <property name=\"catalina.home\" value=\"" + config.catalina_home + "\" />" + lf +
			"  <property name=\"rowset.home\" value=\"C:/devtools/jdbc_rowset1.0\" />" + lf +
		//	"  <property name=\"junit.home\" value=\"C:/devtools/junit3.8.1\" />" + lf +
		//	"  <property name=\"junit.doclet.home\" value=\"C:/devtools/JUnitDoclet.1.0.2\" />" + lf +
			"  <property name=\"webapp\" value=\"" + project.unix_name + "\" />" + lf +
			"  <property name=\"beangen.version\" value=\"" + VERSION + "\" />" + lf +
			"  <target name=\"init\" description=\"Prepares the sandbox directory structure\">" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <echo message=\"*** BeanGen ${beangen.version}\" />" + lf +
			"    <echo message=\"*** http://beangen.sourceforge.net\" />" + lf +
			"    <echo message=\"***\" />" + lf +
			"    <echo message=\"*** This Software is provided 'AS IS'.  All express\" />" + lf +
			"    <echo message=\"*** warranties, including any implied warranty of\" />" + lf +
			"    <echo message=\"*** merchantability, satisfactory quality, fitness for a\" />" + lf +
			"    <echo message=\"*** particular purpose, or non-infringement, are disclaimed,\" />" + lf +
			"    <echo message=\"*** except to the extent that such disclaimers are held to\" />" + lf +
			"    <echo message=\"*** be legally invalid.\" />" + lf +
			"    <echo message=\"*** Permission to use, copy, modify, and distribute this\" />" + lf +
			"    <echo message=\"*** Software and its documentation for NON-COMMERCIAL or\" />" + lf +
			"    <echo message=\"*** COMMERCIAL purposes and without fee is hereby granted.\" />" + lf +
			"    <echo message=\"***\" />" + lf +
			"    <echo message=\"*** In order to have more finders you need to create then\" />" + lf +
			"    <echo message=\"*** in the remote interfaces and define the OQL query in\" />" + lf +
			"    <echo message=\"*** descriptors/openejb-jar.xml file.\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <tstamp/>" + lf +
			"    <mkdir dir=\"build\" />" + lf +
			"    <mkdir dir=\"build/classes\" />" + lf +
			"    <mkdir dir=\"build/archives\" />" + lf +
			"    <mkdir dir=\"doc\" />" + lf +
			"    <mkdir dir=\"src/test\" />" + lf +
			"    <mkdir dir=\"src/test/java\" />" + lf +
			"  </target>" + lf + lf +

			"  <path id=\"classpath.compilation\" >" + lf +
			"    <pathelement path=\"${basedir}/build/classes\" />" + lf +
			"    <pathelement path=\"${openejb.home}/lib/ejb-2.0.jar\" />" + lf +
			"    <pathelement path=\"${rowset.home}/rowset.jar\" />" + lf +
			"  </path>" + lf + lf +

			"  <target name=\"compile\" depends=\"init\" description=\"Compiles only modified source files\">" + lf +
			"    <javac classpathref=\"classpath.compilation\"" + lf +
			"      srcdir=\"${basedir}/src/java\"" + lf +
			"      destdir=\"${basedir}/build/classes\" />" + lf +
			"    <antcall target=\"compile.resources\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"compile.resources\">" + lf +
			"    <mkdir dir=\"${basedir}/build/classes/META-INF\" />" + lf +
			"    <copy todir=\"${basedir}/build/classes/META-INF\">" + lf +
			"      <fileset dir=\"${basedir}/conf/${webapp}\" />" + lf +
			"    </copy>" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"package\" depends=\"compile\" description=\"Packages the application for deployment\">" + lf +
			"    <antcall target=\"package.ejb.jar\" />" + lf +
			"    <antcall target=\"package.web.war\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"package.ejb.jar\">" + lf +
			"    <jar jarfile=\"${basedir}/build/archives/${webapp}_j2ee.jar\"" + lf +
			"      basedir=\"${basedir}/build/classes\"" + lf +
			"      includes=\"**/j2ee/entity/*," + lf +
			"	            **/j2ee/jdo/*," + lf +
			"	            **/j2ee/dac/*," + lf +
			"	            **/j2ee/home/*," + lf +
			"				**/j2ee/remote/*," + lf +
			"				**/pk/*," + lf +
			"				ejb/core/*," + lf +
			"				ejb/exception/*," + lf +
			"				**/META-INF/*\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"package.web.war\">" + lf +
			"    <mkdir dir=\"${basedir}/build/archives/${webapp}\" />" + lf +
			"    <mkdir dir=\"${basedir}/build/archives/${webapp}/WEB-INF\" />" + lf +
			"    <mkdir dir=\"${basedir}/build/archives/${webapp}/WEB-INF/lib\" />" + lf +
			"    <mkdir dir=\"${basedir}/build/archives/${webapp}/api\" />" + lf +
			"    <copy todir=\"${basedir}/build/archives/${webapp}\">" + lf +
			"      <fileset dir=\"${basedir}/src/web\" />" + lf +
			"    </copy>" + lf +
			"    <copy todir=\"${basedir}/build/archives/${webapp}/WEB-INF\">" + lf +
			"      <fileset dir=\"${basedir}/conf/tomcat\">" + lf +
			"         <filename name=\"**/*.xml\" />" + lf +
			"      </fileset>" + lf +
			"    </copy>" + lf +
			"    <jar jarfile=\"${basedir}/build/archives/${webapp}/WEB-INF/lib/${webapp}-lib.jar\"" + lf +
			"      basedir=\"${basedir}/build/classes\"" + lf +
			"      excludes=\"**/j2ee/entity/*," + lf +
			"	            **/j2ee/jdo/*," + lf +
			"	            **/j2ee/dac/*," + lf +
			"	            **/j2ee/home/*," + lf +
			"				**/j2ee/remote/*," + lf +
			"	            **/ejb/core/*," + lf +
			"				**/META-INF/*\" />" + lf +
			"    <javadoc destdir=\"${basedir}/build/archives/${webapp}/api\" defaultexcludes=\"yes\" classpathref=\"classpath.compilation\" author=\"true\" version=\"true\" windowtitle=\"${webapp} API\">" + lf +
			"      <fileset dir=\"${basedir}/src/java\" defaultexcludes=\"no\">" + lf +
			"        <exclude name=\"**/j2ee/*.java\" />" + lf +
			"        <exclude name=\"**/j2ee/entity/*.java\" />" + lf +
			"        <exclude name=\"**/j2ee/jdo/*.java\" />" + lf +
			"        <exclude name=\"**/j2ee/dac/*.java\" />" + lf +
			"        <exclude name=\"**/j2ee/home/*.java\" />" + lf +
			"        <exclude name=\"**/j2ee/remote/*.java\" />" + lf +
			"        <exclude name=\"**/ejb/core/*.java\" />" + lf +
			"        <exclude name=\"**/webapp/*\" />" + lf +
			"        <exclude name=\"**/test/*\" />" + lf +
			"      </fileset>" + lf +
			"      <bottom><![CDATA[<i>Powered by <a href=\"http://beangen.sourceforge.net\">BeanGen ${beangen.version}</a>. All Rights Reserved.</i>]]></bottom>" + lf +
			"    </javadoc>" + lf +
			"    <jar jarfile=\"${basedir}/build/archives/${webapp}.war\" basedir=\"${basedir}/build/archives/${webapp}\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"documentation\" depends=\"init\" description=\"Generates the full javadoc\">" + lf +
			"    <javadoc destdir=\"${basedir}/doc\" defaultexcludes=\"yes\" classpathref=\"classpath.compilation\" author=\"true\" version=\"true\" windowtitle=\"${webapp} API\">" + lf +
			"      <fileset dir=\"${basedir}/src/java\" defaultexcludes=\"yes\">" + lf +
			"        <include name=\"**/*.java\" />" + lf +
			"      </fileset>" + lf +
			"      <bottom><![CDATA[<i>Powered by <a href=\"http://beangen.sourceforge.net\">BeanGen ${beangen.version}</a>. All Rights Reserved.</i>]]></bottom>" + lf +
			"    </javadoc>" + lf +
			"    <jar jarfile=\"${basedir}/build/archives/${webapp}_docs.jar\" basedir=\"${basedir}/doc\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"deploy\" depends=\"package\" description=\"Installs packages and config files into OpenEJB and Tomcat servers\">" + lf +
			"    <copy todir=\"${openejb.home}/conf\">" + lf +
			"      <fileset dir=\"${basedir}/conf/openejb\" />" + lf +
			"    </copy>" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <echo message=\"*** Don't Forget to configure the JDBC Connection in OpenEJB\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <copy file=\"${basedir}/build/archives/${webapp}_j2ee.jar\" todir=\"${openejb.home}/lib\" />" + lf +
			"    <copy file=\"${basedir}/build/archives/${webapp}.war\" todir=\"${catalina.home}/webapps\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <echo message=\"*** Don't Forget to merge server.xml from webapp into\" />" + lf +
			"    <echo message=\"*** Catalina's server.xml\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"clean\" description=\"Removes all ant generated files\">" + lf +
			"    <delete dir=\"${basedir}/build\" />" + lf +
			"    <delete dir=\"${basedir}/doc\" />" + lf +
			"    <delete dir=\"${basedir}/src/test\" />" + lf +
			"  </target>" + lf + lf +
/*
  <path id="project.test.class.path" >
    <pathelement path="${openejb.home}/lib/ejb-2.0.jar" />
    <pathelement path="${junit.home}/junit.jar" />
    <pathelement path="${junit.doclet.home}/JUnitDoclet.jar" />
    <pathelement path="${build}/java" />
  </path>

  <target name="junitdoclet" depends="banner,prepare">
    <javadoc destdir="${src}/test" sourcepath="${src}" defaultexcludes="yes" doclet="com.objectfab.tools.junitdoclet.JUnitDoclet" docletpathref="project.test.class.path" additionalparam="-buildall">
      <classpath refid="project.test.class.path" />
      <fileset dir="${src}" defaultexcludes="yes">
        <include name="** /*EJB.java" />
        <include name="** /ejb/impl/core/*" />
        <include name="** /*PK.java" />
      </fileset>
    </javadoc>
  </target>

  <target name="junitcompile" depends="banner,compile,junitdoclet">
 	<javac srcdir="${src}/test" destdir="${build}/test" debug="on">
      <classpath refid="project.test.class.path" />
    </javac>
  </target>
*/

			"  </project>" + lf
		);
		return code.toString();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		System.out.println("BeanGen" + VERSION);

		if(args.length < 2)
		{
			System.out.println("  Usage: BeanGen <input file> <destination file(.jar)>");
			return;
		}
		else
		{
			BeanGen main = new BeanGen();
			main.parse(args[0]);
			main.generate(new FileOutputStream(args[1]));
		}

		System.out.println("Done.");
	}
}