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

import java.util.StringTokenizer;

public class TextUtils {

	// helper method to the jar stream
	public static String toJarPath(String _package)
	{
		if(_package == null || _package.equals(""))
			return "";

		StringBuffer sb = new StringBuffer();
		for(int i=0; i<_package.length(); i++)
		{
			if(_package.charAt(i) == '.')
				sb.append("/");
			else
				sb.append(_package.charAt(i));
		}

		sb.append("/");
		return sb.toString();
	}

	// String manipulation to ease the class name generation
	public static String toSunClassName(String s)
	{
		StringBuffer sb = new StringBuffer();

		if(s != null)
		{
			StringTokenizer st = new StringTokenizer(s, " _");
			String sz = null;
			while(st.hasMoreTokens())
			{
				sz = st.nextToken();
				sz = sz.toLowerCase();
				sz = sz.substring(0, 1).toUpperCase() + sz.substring(1);
				sb.append(sz);
			}
		}

		return sb.toString();
	}

	// very naive way to make words more readable...
	public static String toPlural(String s)
	{
		if(s.charAt(s.length() - 1) == 'y')
			return s.substring(0, s.length()-1) + "ies";
		else
			return s + "s";
	}

	// convert simple types to java types
	public static String toSQLType(String s)
	{
		if(s == null)
		{
			return "Unknown";
		}

		if(s.equals("bigint"))			return "java.lang.Long";
		if(s.equals("binary"))			return "byte[]";
		if(s.equals("bit"))				return "java.lang.Boolean";
		if(s.equals("blob"))			return "java.io.InputStream";
		if(s.equals("char"))			return "java.lang.String";
		if(s.equals("clob"))			return "java.sql.Clob";
		if(s.equals("date"))			return "java.sql.Date";
		if(s.equals("decimal"))			return "java.math.BigDecimal";
		if(s.equals("double"))			return "java.lang.Double";
		if(s.equals("float"))			return "java.lang.Double";
		if(s.equals("integer"))			return "java.lang.Integer";
		if(s.equals("longvarbinary"))	return "byte[]";
		if(s.equals("longvarchar"))		return "java.lang.String";
		if(s.equals("numeric"))			return "java.math.BigDecimal";
		if(s.equals("real"))			return "java.lang.Float";
		if(s.equals("smallint"))		return "java.lang.Short";
		if(s.equals("time"))			return "java.sql.Time";
		if(s.equals("timestamp"))		return "java.sql.Timestamp";
		if(s.equals("tinyint"))			return "java.lang.Byte";
		if(s.equals("varbinary"))		return "byte[]";
		if(s.equals("varchar"))			return "java.lang.String";

		if(s.indexOf("PK")==-1)
			System.out.println("Unknown DataType: " + s + " assuming Parameterized Type...");
		return s;
	}

	public static String getJDBCSetter(String s)
	{
		if(s == null)
		{
			return "Unknown";
		}

		if(s.equals("bigint"))			return "setLong";
		if(s.equals("binary"))			return "setBytes";
		if(s.equals("bit"))				return "setBoolean";
		if(s.equals("blob"))			return "setBlob";
		if(s.equals("char"))			return "setString";
		if(s.equals("clob"))			return "setClob";
		if(s.equals("date"))			return "setDate";
		if(s.equals("decimal"))			return "setBigDecimal";
		if(s.equals("double"))			return "setDouble";
		if(s.equals("float"))			return "setDouble";
		if(s.equals("integer"))			return "setInt";
		if(s.equals("longvarbinary"))	return "setBytes";
		if(s.equals("longvarchar"))		return "setString";
		if(s.equals("numeric"))			return "setBigDecimal";
		if(s.equals("real"))			return "setFloat";
		if(s.equals("smallint"))		return "setShort";
		if(s.equals("time"))			return "setTime";
		if(s.equals("timestamp"))		return "setTimestamp";
		if(s.equals("tinyint"))			return "setByte";
		if(s.equals("varbinary"))		return "setBytes";
		if(s.equals("varchar"))			return "setString";
		if(s.equals("null"))			return "setNull";

		if(s.indexOf("PK")==-1)
			System.out.println("Unknown DataType: " + s + " assuming Parameterized Type...");
		return s;
	}

	public static String getJDBCGetter(String s)
	{
		if(s == null)
		{
			return "Unknown";
		}

		if(s.equals("bigint"))			return "getLong";
		if(s.equals("binary"))			return "getBytes";
		if(s.equals("bit"))				return "getBoolean";
		if(s.equals("blob"))			return "getBlob";
		if(s.equals("char"))			return "getString";
		if(s.equals("clob"))			return "getClob";
		if(s.equals("date"))			return "getDate";
		if(s.equals("decimal"))			return "getBigDecimal";
		if(s.equals("double"))			return "getDouble";
		if(s.equals("float"))			return "getDouble";
		if(s.equals("integer"))			return "getInt";
		if(s.equals("longvarbinary"))	return "getBytes";
		if(s.equals("longvarchar"))		return "getString";
		if(s.equals("numeric"))			return "getBigDecimal";
		if(s.equals("real"))			return "getFloat";
		if(s.equals("smallint"))		return "getShort";
		if(s.equals("time"))			return "getTime";
		if(s.equals("timestamp"))		return "getTimestamp";
		if(s.equals("tinyint"))			return "getByte";
		if(s.equals("varbinary"))		return "getBytes";
		if(s.equals("varchar"))			return "getString";
		if(s.equals("null"))			return "getNull";

		if(s.indexOf("PK")==-1)
			System.out.println("Unknown DataType: " + s + " assuming Parameterized Type...");
		return s;
	}

	public static String toJavaFieldType(String s)
	{
		if(s == null)
		{
			return "Unknown";
		}

		if(s.equals("big-decimal"))		return "java.math.BigDecimal";
		if(s.equals("boolean"))			return "boolean";
		if(s.equals("byte"))			return "byte";
		if(s.equals("bytes"))			return "byte[]";
		if(s.equals("char"))			return "char";
		if(s.equals("chars"))			return "char[]";
		if(s.equals("clob"))			return "java.sql.Clob";
		if(s.equals("date"))			return "java.util.Date";
		if(s.equals("double"))			return "double";
		if(s.equals("float"))			return "float";
		if(s.equals("integer"))			return "int";
		if(s.equals("locale"))			return "java.util.Locale";
		if(s.equals("long"))			return "long";
		if(s.equals("other"))			return "Object";
		if(s.equals("short"))			return "short";
		if(s.equals("string"))			return "String";
		if(s.equals("strings"))			return "String[]";
		if(s.equals("stream"))			return "java.io.InputStream";

		if(s.indexOf("PK")==-1)
			System.out.println("Unknown DataType: " + s);
		return s;
	}

	public static String toJavaObjectFieldType(String s)
	{
		if(s == null)
		{
			return "Unknown";
		}

		if(s.equals("big-decimal"))		return "java.math.BigDecimal";
		if(s.equals("boolean"))			return "Boolean";
		if(s.equals("byte"))			return "Byte";
		if(s.equals("bytes"))			return "byte[]";
		if(s.equals("char"))			return "Char";
		if(s.equals("chars"))			return "char[]";
		if(s.equals("clob"))			return "java.sql.Clob";
		if(s.equals("date"))			return "java.util.Date";
		if(s.equals("double"))			return "Double";
		if(s.equals("float"))			return "Float";
		if(s.equals("integer"))			return "Integer";
		if(s.equals("locale"))			return "java.util.Locale";
		if(s.equals("long"))			return "Long";
		if(s.equals("other"))			return "Object";
		if(s.equals("short"))			return "Short";
		if(s.equals("string"))			return "String";
		if(s.equals("strings"))			return "String[]";
		if(s.equals("stream"))			return "java.io.InputStream";

		if(s.indexOf("PK")==-1)
			System.out.println("Unknown DataType: " + s);
		return s;
	}

	// convert objects into native java types (when possible)
	public static String toNativeJavaType(String s)
	{
		if(s.equals("java.lang.Boolean"))	return "boolean";
		if(s.equals("java.lang.Byte"))		return "byte";
		if(s.equals("java.lang.Character"))	return "char";
		if(s.equals("java.lang.Double"))	return "double";
		if(s.equals("java.lang.Float"))		return "float";
		if(s.equals("java.lang.Integer"))	return "int";
		if(s.equals("java.lang.Long"))		return "long";
		if(s.equals("java.lang.Short"))		return "short";
		if(s.equals("java.lang.String"))	return "String";

		return s;
	}

	public static String getJavaNativeFromObject(String s)
	{
		if(s.equals("Boolean"))		return ".booleanValue()";
		if(s.equals("Byte"))		return ".byteValue()";
		if(s.equals("Character"))	return ".charValue()";
		if(s.equals("Double"))		return ".doubleValue()";
		if(s.equals("Float"))		return ".floatValue()";
		if(s.equals("Integer"))		return ".intValue()";
		if(s.equals("Long"))		return ".longValue()";
		if(s.equals("Short"))		return ".shortValue()";
		return "";
	}

	public static String getJavaObjectConstructorForNative(String type, String name)
	{
		if(type.equals("boolean"))	return "new Boolean(" + name + ")";
		if(type.equals("byte"))		return "new Byte(" + name + ")";
		if(type.equals("char"))		return "new Character(" + name + ")";
		if(type.equals("double"))	return "new Double(" + name + ")";
		if(type.equals("float"))	return "new Float(" + name + ")";
		if(type.equals("int"))		return "new Integer(" + name + ")";
		if(type.equals("long"))		return "new Long(" + name + ")";
		if(type.equals("short"))	return "new Short(" + name + ")";
		return name;
	}

	public static boolean isNative(String type)
	{
		if(type.equals("boolean"))	return true;
		if(type.equals("byte"))		return true;
		if(type.equals("char"))		return true;
		if(type.equals("double"))	return true;
		if(type.equals("float"))	return true;
		if(type.equals("int"))		return true;
		if(type.equals("long"))		return true;
		if(type.equals("short"))	return true;
		return false;
	}

	// convert objects into native java types (when possible)
	public static String getInitialValueFor(String s)
	{
		if(s.equals("boolean"))	return "false";
		if(s.equals("byte"))	return "0";
		if(s.equals("char"))	return "'\\0'";
		if(s.equals("double"))	return "0d";
		if(s.equals("float"))	return "0f";
		if(s.equals("int"))		return "0";
		if(s.equals("long"))	return "0l";
		if(s.equals("short"))	return "0";

		return "null";
	}

	// String manipulation to ease the method name generation
	public static String toSunMethodName(String s)
	{
		StringBuffer sb = new StringBuffer();

		if(s != null)
		{
			StringTokenizer st = new StringTokenizer(s, " _");
			String sz = null;
			boolean fst = true;
			while(st.hasMoreTokens())
			{
				sz = st.nextToken();
				if(fst)
				{
					sz = sz.substring(0, 1).toLowerCase() + sz.substring(1);
					fst = false;
				}
				else
					sz = sz.substring(0, 1).toUpperCase() + sz.substring(1);
				sb.append(sz);
			}
		}

		return sb.toString();
	}

	// String manipulation to ease the method parameter name generation
	public static String toSunParameterName(String s)
	{
		StringBuffer sb = new StringBuffer();

		if(s != null)
		{
			int idx = s.lastIndexOf(".");
			if(idx != -1)
				s = s.substring(idx + 1);

			StringTokenizer st = new StringTokenizer(s, " _");
			String sz = null;
			boolean fst = true;
			while(st.hasMoreTokens())
			{
				sz = st.nextToken();
				if(fst)
				{
					sz = sz.substring(0, 1).toLowerCase() + sz.substring(1);
					fst = false;
				}
				else
					sz = sz.substring(0, 1).toUpperCase() + sz.substring(1);
				sb.append(sz);
			}
		}

		return sb.toString();
	}
}