package ejb.impl.util;

// Standard packages

// Extension packages

/**
 *	Exception thrown by BeanHomeFactory design pattern.
 * 	<B>Distributable under the GPL - see terms at gnu.org</B>
 *
 *	@author		Andy Jefferson
 *	@version	$Revision$
 */
public class BeanHomeFactoryException extends Exception
{
	/** Default constructor. */
	public BeanHomeFactoryException()
	{
	}

	/** Constructor.
	 * @param	s	message. */
	public BeanHomeFactoryException(String s)
	{
		super(s);
	}

	/** Constructor.
	 * @param	e	Exception to extract message from. */
	public BeanHomeFactoryException(Exception e)
	{
		super(e.getMessage());
	}
}
