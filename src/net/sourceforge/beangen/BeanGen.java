package net.sourceforge.beangen;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * EJB Bean Generator Tool for OpenEJB
 * @author Paulo Lopes <pmml@netvita.pt>
 */
public class BeanGen
{
	// the collection of ejbs to generate
	private Vector ejbs = new Vector();
	private Vector resources = new Vector();
	private PROJECT project = new PROJECT();
	private CONFIG config = new CONFIG();

	// line feed
	private static String lf = System.getProperty("line.separator");
	private static final String DOLLAR = "$";
	// version
	public static final String VERSION = "0.8 - Oxygen";

	// generates a simple header for all classes
	// it's a simple copyright message compatible with open source
	private String getHeader(EJB ejb)
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
	protected String tomcat_webapp_web_xml()
	{
		FIELD field = null;
		EJB ejb = null;

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
				"    <home>" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home</home>" + lf +
				"    <remote>" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "</remote>" + lf +
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

		StringBuffer code = new StringBuffer();

		// for each ejb we need to create an entry Ejb with the type and Home/Remote
		// Interfaces and the factory information
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			// server.xml
			code.append(
				"<Ejb name=\"ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\" type=\"" + ejb.getType() + "\" home=\"" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home\" remote=\"" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "\" />" + lf +
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
					"       <home>" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home</home>" + lf +
					"       <remote>" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Remote</remote>" + lf +
					"       <ejb-class>" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "EJB</ejb-class>" + lf +
					"       <persistence-type>" + ejb.getManagementType() + "</persistence-type>" + lf
				);
				// sql types are managed from jdbc driver so it's a better choice than FieldType
				if(ejb.needsPkObject())
					code.append("       <prim-key-class>" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name + "_p_k") + "</prim-key-class>" + lf);
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
		}
		code.append(
			"  </enterprise-beans>" + lf +
			"  <assembly-descriptor>" + lf
		);
		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);

			if(ejb.type==EJB.BMP || ejb.type==EJB.CMP)
			{
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
		int message = 0;

		for(int i=0; i<ejbs.size(); i++)
		{
			ejb = (EJB)ejbs.elementAt(i);
			if(ejb.type == EJB.BMP) bmp++;
			if(ejb.type == EJB.CMP) cmp++;
			if(ejb.type == EJB.STATEFULL) statefull++;
			if(ejb.type == EJB.STATELESS) stateless++;
			if(ejb.type == EJB.MESSAGE) message++;
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

		if(message > 0)
			System.err.println("Message EJB's not yet Supported");

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

				// define the object and the related table
				code.append(
					"  <class name=\"" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "EJB\" identity=\"" + identity + "\"" +
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
			else if(ejb.type == EJB.STATEFULL)
				code.append(project.getName() + " STATEFULL Container");
			else if(ejb.type == EJB.STATELESS)
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
				"      <object-ql>SELECT o FROM " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "EJB o</object-ql>" + lf +
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
							"      <object-ql>SELECT o FROM " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "EJB o WHERE o." + TextUtils.toSunMethodName(ref_ejb.name + "_id") + " = $1</object-ql>" + lf +
							"    </query>" + lf
						);
					}
				}
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

	private String pk(EJB ejb)
	{
		StringBuffer code = new StringBuffer();
		FIELD field = null;
		// Home Interface
		code.append(getHeader(ejb) + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.app_package + ";" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "PK" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" * " + lf);
 		code.append(" * @since " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + "PK implements java.io.Serializable {" + lf + lf);

		StringBuffer sb = new StringBuffer();
		int fks = 0;
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.fk)
			{
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name) + ";" + lf);
				sb.append(TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name) + ", ");
				fks++;
			}
		}

		if(sb.length()>0)
			sb.setLength(sb.length() - 2);

 		code.append(lf);
 		code.append("    public " + TextUtils.toSunClassName(ejb.name) + "PK(" + sb.toString() + ") {");
		for(int i=0; i<ejb.fields.size(); i++)
		{
			field = (FIELD)ejb.fields.elementAt(i);
			if(field.fk)
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
			if(field.fk)
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
			if(field.fk)
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
			if(field.fk)
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
		code.append(getHeader(ejb) + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.app_package + ";" + lf);
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
		code.append(" * Implementation of " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" * " + lf);
 		code.append(" * @since " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public interface " + TextUtils.toSunClassName(ejb.name) + "Home extends EJBHome {" + lf + lf);

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
		code.append("    public " + TextUtils.toSunClassName(ejb.name) + "Remote create(" + create_params.toString() +") throws CreateException, RemoteException;" + lf + lf);

		// ready made methods for all beans
		code.append("    /**" + lf);
		code.append("     * Finds an entry in the storage for your entities " + TextUtils.toSunClassName(ejb.name) + lf);
		code.append("     * " + lf);
		code.append("     * @param key Primary Key that identifies the entry" + lf);
		code.append("     * @return Remote interface for proxy " + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
		code.append("     */" + lf);
		code.append("    public " + TextUtils.toSunClassName(ejb.name) + "Remote findByPrimaryKey(" + TextUtils.toJavaObjectFieldType(ejb.getPkType()) + " key) throws FinderException, RemoteException;" + lf + lf);

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

		code.append("}" + lf);

		return code.toString();
	}

	// Generate the remote interface
	// Remote interface lets us access the underlaying data for this bean
	private String remote_interface(EJB ejb)
	{
		FIELD field = null;
		StringBuffer code = new StringBuffer();

		code.append(getHeader(ejb) + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.app_package + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.EJBObject;" + lf);
		code.append("import ejb.impl.core.MandatoryFieldException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Remote" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" * " + lf);
 		code.append(" * @since " + DOLLAR + "Id:" + DOLLAR + lf);
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
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + (field.nullable?"":", MandatoryFieldException") + ";" + lf);
				else
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + (field.nullable?"":", MandatoryFieldException") + ";" + lf);
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
				code.append("    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException;" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() throws RemoteException;" + lf);
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

		code.append(getHeader(ejb) + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.app_package + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.util.Collection;" + lf);
		code.append("import java.util.List;" + lf);
		code.append("import java.util.Vector;" + lf);
		code.append(((ejb.type == EJB.BMP)?"":"// ") + "import java.sql.Connection;" + lf);
		code.append(((ejb.type == EJB.BMP)?"":"// ") + "import java.sql.PreparedStatement;" + lf);
		code.append(((ejb.type == EJB.BMP)?"":"// ") + "import java.sql.ResultSet;" + lf);
		code.append(((ejb.type == EJB.BMP)?"":"// ") + "import java.sql.SQLException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import ejb.impl.core.MandatoryFieldException;" + lf);
		code.append("import ejb.impl.core.EntityBeanAdapter;" + lf);
		code.append("import ejb.impl.util.BeanHomeFactoryException;" + lf);
		code.append("import ejb.impl.util.BeanHomeFactory;" + lf + lf);

		code.append("import javax.ejb.EJBException;" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("import javax.ejb.CreateException;" + lf);
		code.append("import javax.ejb.DuplicateKeyException;" + lf);
		code.append("import javax.ejb.ObjectNotFoundException;" + lf);
		code.append(((ejb.type == EJB.BMP)?"":"// ") + "import javax.sql.DataSource;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "EJB" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" * " + lf);
 		code.append(" * @since " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + "EJB extends EntityBeanAdapter {" + lf + lf);

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
					"    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = null;" + lf + lf
				);
			}
			else
			{
				code.append(
					"    /**" + lf +
					"     * The internal java object corresponding to the primitive database field " + ejb.db_name + "." + field.db_name + lf +
					"     */" + lf +
					"    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.getInitialValueFor(TextUtils.toJavaFieldType(field.type)) + ";" + lf + lf
				);
			}
		}

		if(ejb.type == ejb.BMP)
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
		code.append("            } catch (Exception e) {" + lf);
		code.append("                System.out.println(\"Cannot lookup dataSource\" + e);" + lf);
		code.append("                throw new EJBException(\"Cannot lookup dataSource\");" + lf);
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
				code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") " + (field.nullable?"":"throws MandatoryFieldException ") + "{" + lf);
			else
				code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") " + (field.nullable?"":"throws MandatoryFieldException ") + "{" + lf);
			code.append("        this." + TextUtils.toSunMethodName(field.name + (field.fk?"_id":"")) + " = " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ";" + lf);
			if(ejb.type == ejb.BMP)
				code.append("        this.dirty = true;" + lf);
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
				code.append("    public " + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() {" + lf);
			else
				code.append("    public " + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunMethodName("get_" + field.name + (field.fk?"_id":"")) + "() {" + lf);
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

		code.append("     * @return " + ejb.getPkName() + " primary key" + lf);
		code.append("     *" + lf);
		code.append("     */" + lf);
		code.append("    public " + TextUtils.toSQLType(ejb.getPkType()) + " ejbCreate(" + create_params.toString() +") throws CreateException {" + lf + lf);

		if(ejb.type == ejb.BMP)
		{
			code.append("        " + TextUtils.toSQLType(ejb.getPkType()) + " pk = null;" + lf);
			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement stmt = null;" + lf);
			code.append(lf);
		}

		int cnt = 0;
		for(int j=0; j<ejb.fields.size(); j++)
		{
			field = (FIELD)ejb.fields.elementAt(j);
			if(!field.nullable && !field.pk)
				cnt++;
		}

		if(cnt != 0)
		{
			code.append("        try {" + lf);
			// setter's
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				if(!field.nullable && !field.pk)
					code.append("            this." + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ");" + lf);
			}
			code.append("        } catch(MandatoryFieldException e) {" + lf);
			code.append("            // there's a null value in a mandatory field" + lf);
			code.append("            System.err.println(\"Error in assigning values\");" + lf);
			code.append("            throw new CreateException(\"" + TextUtils.toSunClassName(ejb.name) + " Failed to assign variable - \" + e.getMessage());" + lf);
			code.append("        }" + lf);

			code.append(lf);
		}
		code.append("        // your extra code here" + lf);
		StringBuffer sql_full_params = new StringBuffer();

		if(ejb.type == EJB.BMP)
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
				if(!field.nullable)
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
				if(!field.nullable)
				{
					code.append("            stmt.setObject(arg++, " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ");" + lf);
				}
			}

			code.append("            // TODO: set this variable with the PK value" + lf);
			code.append("            // pk = new " + TextUtils.toJavaFieldType(ejb.getPkType()) + "(/* value here */);" + lf);
			code.append("            if ( stmt.executeUpdate() != 1 ) {" + lf);
			code.append("                throw new CreateException(\"" + TextUtils.toSunClassName(ejb.name) + " creation failed - \" + sql_cmd);" + lf);
			code.append("            }" + lf);
			code.append("            // Throw DuplicateKeyException (if you can detect it)" + lf);
			code.append("            //        } catch (DuplicateKeyException dex) {" + lf);
			code.append("            //                throw dex;" + lf);
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
			code.append("        return pk;" + lf);
		}
		else
			code.append("        return null;" + lf);
		code.append("    }" + lf + lf);

		if(ejb.type == ejb.BMP)
		{
			code.append("    public void ejbLoad() throws EJBException {" + lf);
			code.append("        Connection conn = null;" + lf);
			code.append("        PreparedStatement stmt = null;" + lf);
			code.append("        ResultSet rs = null;" + lf);
			code.append(lf);
			code.append("        try {" + lf);
			code.append("            // get a connection for this transaction context" + lf);
			code.append("            " + TextUtils.toJavaFieldType(ejb.getPkType()) + " pk = (" + TextUtils.toJavaFieldType(ejb.getPkType()) + ") ejbContext.getPrimaryKey();" + lf);
			code.append("            conn = getConnection();" + lf);
			code.append("            // find account in DB" + lf);
			code.append("            String sql_cmd = \"SELECT " + sql_full_params.toString() + " FROM " + ejb.db_name + " WHERE " + ejb.db_name + "." + ejb.getPkDbName() + " = ?\";" + lf);
			code.append("            int arg = 1;" + lf);
			code.append("            stmt = conn.prepareStatement( sql_cmd );" + lf);
			code.append("            stmt.setObject(1, pk);" + lf);
			code.append("            rs = stmt.executeQuery();" + lf);
			code.append("            if (!rs.next())" + lf);
			code.append("                throw new EJBException(\"Failed to load bean from database\");" + lf);

			code.append("            try {" + lf);
			// setter's
			for(int j=0; j<ejb.fields.size(); j++)
			{
				field = (FIELD)ejb.fields.elementAt(j);
				code.append("                this." + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "( (" + TextUtils.toJavaFieldType(field.type) + ") rs.getObject(arg++) );" + lf);
			}

			code.append("            } catch (MandatoryFieldException e) {" + lf);
			code.append("                throw new EJBException(\"Failed to load bean from database (Data Model doesn't apply to Bean Model)\");" + lf);
			code.append("            }" + lf);
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

		if(ejb.type == EJB.BMP)
		{
			code.append("    /*============================ ejbFind methods ===========================*/" + lf + lf);

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
		code.append(getHeader(ejb) + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.app_package + ";" + lf);
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
		code.append("import ejb.impl.util.Trace;" + lf);
		code.append("import ejb.impl.util.BeanHomeFactory;" + lf);
		code.append("import ejb.impl.util.BeanHomeFactoryException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Manager" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" * " + lf);
 		code.append(" * @since " + DOLLAR + "Id:" + DOLLAR + lf);
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
			"            " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\");" + lf +
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
			"            " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\");" + lf +
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
			"            " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home home = (" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home)factory.getHome(" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Home.class, \"java:comp/env/ejb/" + ejb.getJNDIPath() + TextUtils.toSunClassName(ejb.name) + "\");" + lf +
			"            Collection elements = home.findAll();" + lf +
			"            if(elements == null)" + lf +
			"                return null;" + lf +
			"            else" + lf +
			"            {" + lf +
			"                " + TextUtils.toSunClassName(ejb.name) + "[] wrappers = new " + TextUtils.toSunClassName(ejb.name) + "[elements.size()];" + lf +
			"                Iterator it = elements.iterator();" + lf +
			"                int i = 0;" + lf +
			"                while(it.hasNext()) {" + lf +
			"                    wrappers[i++] = new " + TextUtils.toSunClassName(ejb.name) + "((" + TextUtils.toSunClassName(ejb.name) + "Remote)it.next());" + lf +
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
					pk_package = ejb_tmp.app_package;
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
				"    public static synchronized " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] " + TextUtils.toSunMethodName("get_" + TextUtils.toPlural(TextUtils.toSunClassName(col_tmp.getAlias()))) + "(" + pk_type + " key) throws BeanHomeFactoryException, FinderException, RemoteException {" + lf +
				"        try {" + lf +
				"            if(factory == null) init();" + lf +
				"            " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Home home = (" + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Home)factory.getHome(" + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Home.class, \"java:comp/env/ejb/" + ejb_tmp.getJNDIPath() + TextUtils.toSunClassName(ejb_tmp.name) + "\");" + lf +
				"            Collection elements = home.findBy" + TextUtils.toSunClassName(home_intf_name) + "(key);" + lf +
				"            if(elements == null)" + lf +
				"                return null;" + lf +
				"            else" + lf +
				"            {" + lf +
				"                " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] wrappers = new " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[elements.size()];" + lf +
				"                Iterator it = elements.iterator();" + lf +
				"                int i = 0;" + lf +
				"                while(it.hasNext()) {" + lf +
				"                    wrappers[i++] = new " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "((" + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "Remote)it.next());" + lf +
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

	private String wraper_bean(EJB ejb)
	{
		FIELD field = null;
		FIELD field_tmp = null;

		StringBuffer code = new StringBuffer();
		code.append(getHeader(ejb) + lf);
 		code.append(" */" + lf);
		code.append("package " + ejb.app_package + ";" + lf);
		code.append(lf);
		code.append("// Standard packages" + lf);
		code.append("import java.rmi.RemoteException;" + lf);
		code.append("  //add your needed standard packages here" + lf);
		code.append(lf);
		code.append("// Extension packages" + lf);
		code.append("import javax.ejb.FinderException;" + lf);
		code.append("import ejb.impl.util.Trace;" + lf);
		code.append("import ejb.impl.util.BeanHomeFactoryException;" + lf);
		code.append("import ejb.impl.core.MandatoryFieldException;" + lf);
		code.append("  //add your own packages here" + lf);
		code.append(lf);
 		code.append("/**" + lf);
		code.append(" * Implementation of " + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "" + lf);
		code.append(" * " + lf);
		code.append(" * " + ejb.description.replaceAll(lf, lf + " * ") + lf);
 		code.append(" * " + lf);
 		code.append(" * Generated by BeanGen " + this.VERSION + lf);
 		code.append(" * " + lf);
 		code.append(" * @since " + DOLLAR + "Id:" + DOLLAR + lf);
		code.append(" * @author " + ejb.author + lf);
 		code.append(" */" + lf);
 		code.append(lf);
		code.append("public class " + TextUtils.toSunClassName(ejb.name) + " {" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Internal reference to Remote Object" + lf);
		code.append("     */" + lf);
		code.append("    private " + TextUtils.toSunClassName(ejb.name) + "Remote ejb = null;" + lf + lf);

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
								fk_tmp_package = ejb_tmp.app_package;
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
										fk_tmp_package = ejb_tmp.app_package;
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
						"    private " + fk_tmp_package + "." + TextUtils.toSunClassName(fk_tmp_name) + " " + TextUtils.toSunMethodName(field.name) + " = null;" + lf + lf +
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
						pk_package = ejb_tmp.app_package;
						break;
					}
				}

				code.append(
					"    /**" + lf +
					"     * Local copy of remote Collection of " + TextUtils.toSunClassName(col_tmp.obj_name)  + "Remote for Lazy Loading." + lf +
					"     * This means that this bean will only load once this property until there's a update." + lf +
					"     */" + lf +
					"    private " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] " + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias())) + " = null;" + lf + lf +
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
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaObjectFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + (field.nullable==true?"":", MandatoryFieldException") + " {" + lf);
				else
					code.append("    public void " + TextUtils.toSunMethodName("set_" + field.name + (field.fk?"_id":"")) + "(" + TextUtils.toJavaFieldType(field.type) + " " + TextUtils.toSunParameterName(field.name + (field.fk?"_id":"")) + ") throws RemoteException" + (field.nullable==true?"":", MandatoryFieldException") + " {" + lf);

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
							fk_tmp_package = ejb_tmp.app_package;
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
									fk_tmp_package = ejb_tmp.app_package;
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
					"    public " + fk_tmp_package + "." + TextUtils.toSunClassName(fk_tmp_name) + " " + TextUtils.toSunMethodName("get_" + field.name) + "() throws BeanHomeFactoryException, RemoteException, FinderException {" + lf +
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
					"    public void " + TextUtils.toSunMethodName("set_" + field.name) + "(" + fk_tmp_package + "." + TextUtils.toSunClassName(fk_tmp_name) + " wrapper) throws " + (field.nullable==true?"":"MandatoryFieldException, ") + "RemoteException {" + lf +
					"        try {" + lf
				);
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
					pk_package = ejb_tmp.app_package;
					break;
				}
			}

			code.append(
				"    /**" + lf +
				"     * Local copy of remote Collection of " + TextUtils.toSunClassName(col_tmp.obj_name)  + "Remote for Lazy Loading." + lf +
				"     * This means that this bean will only load once this property until there's a update." + lf +
				"     */" + lf +
				"    public " + pk_package + "." + TextUtils.toSunClassName(col_tmp.obj_name) + "[] " + TextUtils.toSunMethodName("get_" + TextUtils.toSunMethodName(TextUtils.toPlural(col_tmp.getAlias()))) + "() throws BeanHomeFactoryException, RemoteException, FinderException {" + lf +
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
		code.append("    protected " + TextUtils.toSunClassName(ejb.name) + "(" + TextUtils.toSunClassName(ejb.name) + "Remote ejb) {" + lf);
		code.append("        this.ejb = ejb;" + lf);
		code.append("    }" + lf + lf);

		code.append("    /**" + lf);
		code.append("     * Cleanup for this object" + lf);
		code.append("     */" + lf);
		code.append("    protected void finalize() throws Throwable {" + lf);
		code.append("        // TODO: Your extra clean up code here..." + lf);

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
						pk_package = ejb_tmp.app_package;
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

	private String test_jsp(EJB ejb)
	{
		StringBuffer code = new StringBuffer();

		code.append("<%@ page import=\"" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "\"%>" + lf + lf);
		code.append("<jsp:useBean id=\"" + TextUtils.toSunParameterName(ejb.name + "_manager") + "\" scope=\"application\" class=\"" + ejb.app_package + "." + TextUtils.toSunClassName(ejb.name) + "Manager\" />" + lf + lf);
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

		jentry = new JarEntry(project.unix_name + "/" + "build.xml");
		jout.putNextEntry(jentry);
		jout.write(this.ant_makefile().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/server.xml");
		jout.putNextEntry(jentry);
		jout.write(this.tomcat_server_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/web.xml");
		jout.putNextEntry(jentry);
		jout.write(this.tomcat_webapp_web_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/openejb.conf");
		jout.putNextEntry(jentry);
		jout.write(this.openejb_conf_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/cmp_global_database.xml");
		jout.putNextEntry(jentry);
		jout.write(this.cmp_global_database_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/cmp_or_mapping.xml");
		jout.putNextEntry(jentry);
		jout.write(this.cmp_or_mapping_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/cmp_local_database.xml");
		jout.putNextEntry(jentry);
		jout.write(this.cmp_local_database_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/ejb-jar.xml");
		jout.putNextEntry(jentry);
		jout.write(this.ejb_jar_xml().getBytes());

		jentry = new JarEntry(project.unix_name + "/" + "descriptors/openejb-jar.xml");
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
				jentry = new JarEntry(project.unix_name + "/src/" + TextUtils.toJarPath(ejb.app_package) + TextUtils.toSunClassName(ejb.name) + "PK.java" );
				jout.putNextEntry(jentry);
				jout.write(this.pk( ejb ).getBytes());
			}

			// home interface
			jentry = new JarEntry(project.unix_name + "/src/" + TextUtils.toJarPath(ejb.app_package) + TextUtils.toSunClassName(ejb.name) + "Home.java" );
			jout.putNextEntry(jentry);
			jout.write(this.home_interface( ejb ).getBytes());

			// remote interface
			jentry = new JarEntry(project.unix_name + "/src/" + TextUtils.toJarPath(ejb.app_package) + TextUtils.toSunClassName(ejb.name) + "Remote.java" );
			jout.putNextEntry(jentry);
			jout.write(this.remote_interface( ejb ).getBytes());

			// EJB
			jentry = new JarEntry(project.unix_name + "/src/" + TextUtils.toJarPath(ejb.app_package) + TextUtils.toSunClassName(ejb.name) + "EJB.java" );
			jout.putNextEntry(jentry);
			if(ejb.type==EJB.BMP || ejb.type==EJB.CMP)
				jout.write(this.entity_bean( ejb ).getBytes());

			// EJB Wrapper
			jentry = new JarEntry(project.unix_name + "/src/" + TextUtils.toJarPath(ejb.app_package) + TextUtils.toSunClassName(ejb.name) + ".java" );
			jout.putNextEntry(jentry);
			if(ejb.type==EJB.BMP || ejb.type==EJB.CMP)
				jout.write(this.wraper_bean( ejb ).getBytes());

			// Wrapper Manager
			jentry = new JarEntry(project.unix_name + "/src/" + TextUtils.toJarPath(ejb.app_package) + TextUtils.toSunClassName(ejb.name) + "Manager.java" );
			jout.putNextEntry(jentry);
			if(ejb.type==EJB.BMP || ejb.type==EJB.CMP)
				jout.write(this.manager_bean( ejb ).getBytes());

			// JSP Sample
			jentry = new JarEntry(project.unix_name + "/jsp/" + TextUtils.toSunMethodName(ejb.name) + "_test.jsp");
			jout.putNextEntry(jentry);
			jout.write(this.test_jsp( ejb ).getBytes());

		}
		// Logger
		jentry = new JarEntry(project.unix_name + "/src/ejb/impl/util/Trace.java");
		jout.putNextEntry(jentry);
		jout.write(this.commons_trace().getBytes());

		// EJB utilities (Home Factory)
		jentry = new JarEntry(project.unix_name + "/src/ejb/impl/util/BeanHomeFactory.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_beanhome_factory().getBytes());

		// EJB utilities (Home Factory)
		jentry = new JarEntry(project.unix_name + "/src/ejb/impl/util/BeanHomeFactoryException.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_beanhome_factory_exception().getBytes());

		// EJB utilities (Entity Bean Adapter)
		jentry = new JarEntry(project.unix_name + "/src/ejb/impl/core/EntityBeanAdapter.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_entity_bean_adapter().getBytes());

		// EJB utilities (Session Bean Adapter)
		jentry = new JarEntry(project.unix_name + "/src/ejb/impl/core/SessionBeanAdapter.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_session_bean_adapter().getBytes());

		// EJB utilities (MandatoryField Exception)
		jentry = new JarEntry(project.unix_name + "/src/ejb/impl/core/MandatoryFieldException.java" );
		jout.putNextEntry(jentry);
		jout.write(this.commons_mandatoryfield_exception().getBytes());
		jout.close();
	}

	// this is my file format parser
	protected void parse(String file) throws FileNotFoundException, IOException
	{
		// runtime temporary parser objectecs
		FIELD field = null;
		EJB ejb = null;
		RESOURCE res = null;

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
							if(sz.equalsIgnoreCase("ENTITY"))
							{
								ejb = new EJB();
								if(st.hasMoreTokens())
								{
									String tmp = st.nextToken();
									if(tmp.equalsIgnoreCase("BMP"))
										ejb.type = EJB.BMP;
									else if(tmp.equalsIgnoreCase("CMP"))
										ejb.type = EJB.CMP;
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
									field.fk = tmp.equalsIgnoreCase("FK");
								}
								ejb.fields.addElement( field );
							}
							else if(sz.equalsIgnoreCase("DESCRIPTION"))
							{
								if("".equals(ejb.description))
									ejb.description = st.nextToken();
								else
									ejb.description += (lf + st.nextToken());
							}
							else if(sz.equalsIgnoreCase("AUTHOR"))
							{
								ejb.author = st.nextToken();
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
												ejb.resources.addElement( res );
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
								if(st.hasMoreTokens()) ejb.jndi_basepath = st.nextToken();
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
								} else if(tmp.equalsIgnoreCase("OPENEJB"))
								{
									config.openejb_home = st.nextToken();
									while(st.hasMoreTokens())
										config.openejb_home += ":" + st.nextToken();
								}
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

	// ready made makefile
	private String ant_makefile() throws IOException
	{
		StringBuffer code = new StringBuffer();

		code.append(
			"<project name=\"" + project.name + "\" default=\"war\" basedir=\".\" >" + lf +
			"  <!-- set global properties for this build - may be overridden -->" + lf +
			"  <property name=\"openejb.home\" value=\"" + config.openejb_home + "\" />" + lf +
			"  <property name=\"catalina.home\" value=\"" + config.catalina_home + "\" />" + lf +
			"  <property name=\"webapp\" value=\"" + project.unix_name + "\" />" + lf +
			"  <property name=\"beangen.version\" value=\"" + VERSION + "\" />" + lf +
			"  <property name=\"src\" value=\"src\" />" + lf +
			"  <property name=\"build\" value=\"build\" />" + lf +
			"  <property name=\"dist\" value=\"dist\" />" + lf +
			"  <property name=\"doc\" value=\"doc\" />" + lf +
			"  <property name=\"deploy-dir\" value=\".\" />" + lf + lf +

			"  <path id=\"project.class.path\" >" + lf +
			"    <pathelement path=\"${openejb.home}/lib/ejb-2.0.jar\" />" + lf +
			"  </path>" + lf + lf +

			"  <target name=\"banner\" >" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <echo message=\"*** BeanGen ${beangen.version}\" />" + lf +
			"    <echo message=\"*** http://beangen.sourceforge.net\" />" + lf +
			"    <echo message=\"***\" />" + lf +
			"    <echo message=\"*** This Software is provided \'AS IS\'.  All express\" />" + lf +
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
			"  </target>" + lf + lf +

			"  <target name=\"init\" >" + lf +
			"    <!-- Create the time stamp -->" + lf +
			"    <tstamp/>" + lf +
			"    <!-- Create the build directory structure used by compile -->" + lf +
			"    <mkdir dir=\"${build}\" />" + lf +
			"    <mkdir dir=\"${dist}\" />" + lf +
			"    <mkdir dir=\"${doc}\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"compile\" depends=\"init\" >" + lf +
			"    <!-- Compile the java code from ${src} into ${build} -->" + lf +
			"    <javac srcdir=\"${src}\" destdir=\"${build}\" classpathref=\"project.class.path\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"j2ee\" depends=\"compile\" >" + lf +
			"    <mkdir dir=\"${build}/META-INF\" />" + lf +
			"    <copy file=\"descriptors/ejb-jar.xml\" todir=\"${build}/META-INF\" />" + lf +
			"    <copy file=\"descriptors/openejb-jar.xml\" todir=\"${build}/META-INF\" />" + lf +
			"    <jar jarfile=\"${dist}/${webapp}_j2ee.jar\" basedir=\"${build}\" includes=\"**/*EJB.class,**/*Home.class,**/*Remote.class,**/*PK.class,ejb/impl/core/*,ejb/impl/util/*,**/META-INF/*\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"war\" depends=\"banner,init,compile,j2ee,documentation\" >" + lf +
			"    <mkdir dir=\"${dist}/webapps/${webapp}\" />" + lf +
			"    <mkdir dir=\"${dist}/webapps/${webapp}/WEB-INF\" />" + lf +
			"    <mkdir dir=\"${dist}/webapps/${webapp}/WEB-INF/lib\" />" + lf +
			"    <!-- Package Java Beans -->" + lf +
			"    <jar jarfile=\"${dist}/webapps/${webapp}/WEB-INF/lib/webapp_beans.jar\" basedir=\"${build}\" excludes=\"**/*EJB.class,**/*Home.class,**/*Remote.class,**/META-INF/*\" />" + lf +
			"    <!-- Add sample generated WEB.xml -->" + lf +
			"    <copy file=\"descriptors/web.xml\" todir=\"${dist}/webapps/${webapp}/WEB-INF\" />" + lf +
			"    <copy file=\"descriptors/server.xml\" todir=\"${dist}/webapps/${webapp}/WEB-INF\" />" + lf +
			"    <!-- Add sample generated JSP's -->" + lf +
			"    <copy todir=\"${dist}/webapps/${webapp}\" >" + lf +
			"      <fileset dir=\"jsp\" />" + lf +
			"    </copy>" + lf +
			"    <!-- Add API -->" + lf +
			"    <copy todir=\"${dist}/webapps/${webapp}/api\" >" + lf +
			"      <fileset dir=\"${doc}/api\" />" + lf +
			"    </copy>" + lf +
			"    <!-- Pack everything into a WAR file -->" + lf +
			"    <jar jarfile=\"${dist}/${webapp}.war\" basedir=\"${dist}/webapps/${webapp}\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"install\" depends=\"war\" >" + lf +
			"    <!-- Copy Configuration Files over OpenEJB one's -->" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <echo message=\"*** Don't Forget to configure the JDBC Connection in OpenEJB\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <copy file=\"descriptors/openejb.conf\" todir=\"${openejb.home}/conf\" />" + lf +
			"    <copy file=\"descriptors/cmp_global_database.xml\" todir=\"${openejb.home}/conf\" />" + lf +
			"    <copy file=\"descriptors/cmp_local_database.xml\" todir=\"${openejb.home}/conf\" />" + lf +
			"    <copy file=\"descriptors/cmp_or_mapping.xml\" todir=\"${openejb.home}/conf\" />" + lf +
			"    <copy file=\"${dist}/${webapp}_j2ee.jar\" todir=\"${openejb.home}/lib\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <echo message=\"*** Don't Forget to merge server.xml from webapp into\" />" + lf +
			"    <echo message=\"*** Catalina's server.xml\" />" + lf +
			"    <echo message=\"************************************************************\" />" + lf +
			"    <copy file=\"${dist}/${webapp}.war\" todir=\"${catalina.home}/webapps\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"clean\" >" + lf +
			"    <!-- Delete the ${build} directory trees -->" + lf +
			"    <delete dir=\"${build}\" />" + lf +
			"    <delete dir=\"${dist}\" />" + lf +
			"  </target>" + lf + lf +

			"  <target name=\"documentation\" depends=\"j2ee\">" + lf +
			"    <javadoc destdir=\"${doc}/api\" defaultexcludes=\"yes\" classpathref=\"project.class.path\" author=\"true\" version=\"true\" windowtitle=\"${webapp} API\">" + lf +
			"      <fileset dir=\"${src}\" defaultexcludes=\"yes\">" + lf +
			"        <include name=\"**/*.java\" />" + lf +
			"      </fileset>" + lf +
			"      <bottom><![CDATA[<i>Powered by <a href=\"http://beangen.sourceforge.net\">BeanGen ${beangen.version}</a>. All Rights Reserved.</i>]]></bottom>" + lf +
			"    </javadoc>" + lf +
			"  </target>" + lf + lf +

			"</project>"
		);
		return code.toString();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		System.out.println("BeanGen" + VERSION);

		if(args.length < 2)
			System.out.println("  Usage: BeanGen <input file> <destination file(.jar)>");

		BeanGen main = new BeanGen();
		main.parse(args[0]);
		main.generate(new FileOutputStream(args[1]));

		System.out.println("Done.");
	}
}