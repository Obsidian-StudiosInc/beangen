package ejb.beangen.util;

// Standard packages
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

// Extension packages
import javax.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import ejb.beangen.exception.BeanHomeFactoryException;

/**
 *	Design pattern for creating and caching the Home objects for easy access.
 *	This is referred to in various pattern guides as EJB Home Factory, or
 *	Service Locator. Service Locator provides access to not just the home, but
 *	also remote, and local objects, but does not cache. This class caters for
 *	Home and caches.
 *
 *	@author		Paulo Lopes
 */
public class BeanHomeFactory
{
	/** Map of Homes. */
	private Map	homeMap = null;

	/** Singleton of this class. */
	private static BeanHomeFactory me = null;

	/** Initial Context. */
	private	Context	context = null;

	/** Private constructor to enforce singleton pattern.
	 * @throws	NamingException	Thrown when unable to obtain an InitialContext. */
	private BeanHomeFactory()
	throws NamingException
	{
		try
		{
			context = new InitialContext();
			this.homeMap = Collections.synchronizedMap(new HashMap());
		}
		catch(Exception e)
		{
			throw new NamingException(e.getMessage());
		}
	}

	/** Accessor for the singleton instance.
	 * @return	The BeanHomeFactory.
	 * @throws	BeanHomeFactoryException	Thrown if an error is encountered
	 * finding the factory. */
	public static BeanHomeFactory getFactory()
	throws BeanHomeFactoryException
	{
		try
		{
			if (BeanHomeFactory.me == null)
			{
				BeanHomeFactory.me = new BeanHomeFactory();
			}
		}
		catch (NamingException e)
		{
			throw new BeanHomeFactoryException(e);
		}

		return BeanHomeFactory.me;
	}

	/** Accessor for the EJBHome of the specified bean. Relies on ejb-ref tags
	 * in the deployment descriptor being the same as the class_name.
	 * @param	home_class	The class of the Home interface.
	 * @return	The EJBHome.
	 * @throws	BeanHomeFactoryException	Thrown if there is an error in
	 * finding the Home. */
	public EJBHome	getHome(Class home_class)
	throws BeanHomeFactoryException
	{
		return getHome(home_class,home_class.getName());
	}

	/** Accessor for the EJBHome of the specified bean. Allows specification of
	 * the JNDI name, so catering for deployments where the ejb-ref name is
	 * different to the class_name (like the majority of cases).
	 * @param	home_class	The class of the Home interface.
	 * @param	jndi_name	The JNDI lookup name of the class.
	 * @return	The EJBHome.
	 * @throws	BeanHomeFactoryException	Thrown if there is an error in
	 * finding the Home. */
	public EJBHome	getHome(Class home_class,String jndi_name)
	throws BeanHomeFactoryException
	{
		EJBHome	home=(EJBHome)this.homeMap.get(home_class);

		try
		{
			if (home == null)
			{
				home = (EJBHome)PortableRemoteObject.narrow(context.lookup(jndi_name),home_class);
				this.homeMap.put(home_class,home);
			}
		}
		catch (ClassCastException e)
		{
			throw new BeanHomeFactoryException(e);
		}
		catch (NamingException e)
		{
			throw new BeanHomeFactoryException(e);
		}

		return home;
	}
}
