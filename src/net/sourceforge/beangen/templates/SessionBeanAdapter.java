package ejb.impl.core;

// Standard packages

// Extension packages
import	javax.ejb.SessionBean;
import	javax.ejb.SessionContext;
import	javax.ejb.EJBException;
import	javax.naming.InitialContext;
import	javax.naming.NamingException;


/**
 *	Adapter class to provide basic implementations of the main EJB callback
 *	methods. Extracted from the OReilly book by Richard Monson-Haefel.
 *	<B>Distributable under the GPL - see terms at gnu.org</B>
 */

public class SessionBeanAdapter implements SessionBean
{
	/** Session Context for bean. */
	protected transient SessionContext ejbContext = null;
    protected transient InitialContext initContext = null;

	/** Activate method for bean. */
	public void	ejbActivate()
	{
	}

	/** Deactivate method for bean. */
	public void	ejbPassivate()
	{
	}

	/** Remove method for bean. */
	public void	ejbRemove()
	{
	}

	/** Mutator for Session Context for bean - set.
	 * @param	context	The entity context. */
	public void	setSessionContext(SessionContext context)
	{
		this.ejbContext = context;
		try
		{
	    	this.initContext = new InitialContext();
	    }
	    catch (NamingException e)
		{
			throw new EJBException(e);
		}
	}

	/** Mutator for Session Context for bean - unset. */
	public void unsetSessionContext()
	{
		this.ejbContext = null;
	}

	/** Accessor for Session Context for bean.
	 * @return	The EJB context. */
	public SessionContext getEJBContext()
	{
		return ejbContext;
	}
}
