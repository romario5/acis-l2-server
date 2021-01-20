package net.sf.l2j;


import net.sf.l2j.commons.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class L2DatabaseFactory
{
	public static L2DatabaseFactory getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public L2DatabaseFactory() throws SQLException
	{

	}
	
	public void shutdown()
	{

	}
	
	/**
	 * Use brace as a safty precaution in case name is a reserved word.
	 * @param whatToCheck the list of arguments.
	 * @return the list of arguments between brackets.
	 */
	public static final String safetyString(String... whatToCheck)
	{
		final StringBuilder sb = new StringBuilder();
		for (String word : whatToCheck)
		{
			if (sb.length() > 0)
				sb.append(", ");
			
			sb.append('`');
			sb.append(word);
			sb.append('`');
		}
		return sb.toString();
	}
	
	public Connection getConnection()
	{
		Connection con;
		try {
			con = ConnectionPool.getConnection();
		} catch (SQLException e) {
			return null;
		}
		return con;
	}
	
	private static class SingletonHolder
	{
		protected static final L2DatabaseFactory _instance;
		
		static
		{
			try
			{
				_instance = new L2DatabaseFactory();
			}
			catch (Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}
}