package ejb.impl.core;

// Standard packages

// Extension packages
import	javax.ejb.EntityBean;
import	javax.ejb.EntityContext;
import	javax.ejb.EJBException;
import	javax.naming.InitialContext;
import	javax.naming.NamingException;

/**
 *	Adapter class to provide basic implementations of the main EJB callback
 *	methods. Extracted from the OReilly book by Richard Monson-Haefel.
 *	<B>Distributable under the GPL - see terms at gnu.org</B>
 *
 */

public class EntityBeanAdapter implements EntityBean
{
	/** Entity Context for bean. */
    protected transient EntityContext ejbContext = null;
    protected transient InitialContext initContext = null;

	/** Activate method for bean. */
	public void	ejbActivate()
	{
	}

	/** Deactivate method for bean. */
	public void	ejbPassivate()
	{
	}

	/** Load method for bean. */
	public void	ejbLoad()
	{
	}

	/** Store method for bean. */
	public void	ejbStore()
	{
	}

	/** Remove method for bean. */
	public void	ejbRemove()
	{
	}

	/** Mutator for Entity Context for bean - set.
	 * @param	context	The entity context. */
	public void	setEntityContext(EntityContext context)
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

	/** Mutator for Entity Context for bean - unset. */
	public void unsetEntityContext()
	{
		this.ejbContext = null;
	}

	/** Accessor for Entity Context for bean.
	 * @return	The EJB context. */
	public EntityContext getEJBContext()
	{
		return ejbContext;
	}
}
