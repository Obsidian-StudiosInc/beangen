package ejb.impl.core;

// Standard packages

// Extension packages
import	javax.naming.Context;
import	javax.naming.InitialContext;
import	javax.naming.NamingException;
import	javax.ejb.EJBException;
import	javax.ejb.MessageDrivenBean;
import	javax.ejb.MessageDrivenContext;

/**
 *	Adapter class to provide basic implementations of the main EJB callback
 *	methods for a Message driven bean.
 *	<B>Distributable under the GPL - see terms at gnu.org</B>
 *
 *	@author		Andy Jefferson
 *	@version	$Revision$
 **/

public class MessageBeanAdapter implements MessageDrivenBean
{
	/** Entity Context for bean. */
	protected MessageDrivenContext	context=null;

	/** JNDI Context. */
	protected Context				jndiContext=null;

	/** Create method for bean. */
	public void ejbCreate()
	{
	}

	/** Remove method for bean. */
	public void	ejbRemove()
	{
		try
		{
			jndiContext.close();
			context = null;
		}
		catch (NamingException ne)
		{
			// Do nothing
		}
	}

	/** Mutator for Entity Context for bean - set.
	 * @param	context	The message context. */
	public void	setMessageDrivenContext(MessageDrivenContext context)
	{
		this.context = context;
		try
		{
			jndiContext = new InitialContext();
		}
		catch (NamingException ne)
		{
			throw new EJBException(ne);
		}
	}
}
