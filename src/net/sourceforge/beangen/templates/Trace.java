/***************************************************************************/
/*                                                                         */
/* (c) Copyright IBM Corp. 2003  All rights reserved.                      */
/*                                                                         */
/* This sample program is owned by International Business Machines         */
/* Corporation or one of its subsidiaries ("IBM") and is copyrighted       */
/* and licensed, not sold.                                                 */
/*                                                                         */
/* You may copy, modify, and distribute this sample program in any         */
/* form without payment to IBM, for any purpose including developing,      */
/* using, marketing or distributing programs that include or are           */
/* derivative works of the sample program.                                 */
/*                                                                         */
/* The sample program is provided to you on an "AS IS" basis, without      */
/* warranty of any kind.  IBM HEREBY  EXPRESSLY DISCLAIMS ALL WARRANTIES,  */
/* EITHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED   */
/* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.     */
/* Some jurisdictions do not allow for the exclusion or limitation of      */
/* implied warranties, so the above limitations or exclusions may not      */
/* apply to you.  IBM shall not be liable for any damages you suffer as    */
/* a result of using, modifying or distributing the sample program or      */
/* its derivatives.                                                        */
/*                                                                         */
/* Each copy of any portion of this sample program or any derivative       */
/* work,  must include the above copyright notice and disclaimer of        */
/* warranty.                                                               */
/*                                                                         */
/***************************************************************************/
package ejb.util;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Tracing utility class
 *
 * @author Scott Clee
 */
public class Trace
{
	private static final String     sVERSION = "1.0";
    private static final DateFormat sDF      = DateFormat.getTimeInstance();

	private static int sLastPosition = -1;
	private static int sMargin       = 0;

	private static int TRACE_OUT   = 0;
	private static int TRACE_ERROR = 0;
	private static int TRACE_WIDTH = 94;

	public static final int SEVERE = 1;
	public static final int WARNING = 2;
	public static final int INFO = 3;
	public static final int CONFIG = 4;
	public static final int FINE = 5;

	// Initialize everything on class creation
	static { initialize(); }

	public static void out(String txt)
	{
		out(txt, 5);
	}

	public static void outln(String txt)
	{
		out(txt + '\n');
	}

	public static void out(String txt, int level)
	{
		out(txt, level, false);
	}

	public static void outln(String txt, int level)
	{
		out(txt + '\n', level);
	}

	public static void out(String txt, int level, boolean forceTime)
	{
		if (TRACE_OUT != 0 && level <= TRACE_OUT)
		{
			String time = "";

			// Set the time variable if it has been forced or if we don't have a remembered last position
			if (forceTime || sLastPosition == -1)
			{
				time = "(" + sDF.format(new Date()) + ") ";
				sMargin = time.length();
			}

			outputMessage(System.out, time + txt);
		}
	}

	public void outln(String txt, int level, boolean forceTime)
	{
		out(txt + '\n', level, forceTime);
	}

	public static void err(String txt)
	{
		err(txt, 5);
	}

	public static void errln(String txt)
	{
		err(txt + '\n');
	}

	public static void err(String txt, int level)
	{
		err(txt, level, false);
	}

	public static void errln(String txt, int level)
	{
		err(txt + '\n', level);
	}

	public static void err(String txt, int level, boolean forceTime)
	{
		if (TRACE_ERROR != 0 && level <= TRACE_ERROR)
		{
			String time = "";

			// Set the time variable if it has been forced or if we don't have a remembered last position
			if (forceTime || sLastPosition == -1)
			{
				time = "(" + sDF.format(new Date()) + ") ";
				sMargin = time.length();
			}

			outputMessage(System.err, time + txt);
		}
	}

	public static void errln(String txt, int level, boolean forceTime)
	{
		err(txt + '\n', level, forceTime);
	}

    /**
     * Formats the given String and outputs it to the
     * given PrintStream
     *
     * @param out    PrintStream to output to, this is usually stdtxt or stderr
     * @param txt    Text to output
     */
    private synchronized static void outputMessage(PrintStream out, String txt)
	{
		int     strLength       = txt.length();
		boolean endsWithNewLine = false;
		int     startIndex      = 0;
        int     endIndex        = 0;

		// Trace width has to be at least as long as margin + 1 otherwise
		// nothing would print out
		if (TRACE_WIDTH < sMargin + 1) TRACE_WIDTH = sMargin + 1;

		// Strip the end carriage return if there is one
		if (txt.charAt(strLength - 1) == '\n')
		{
			txt = txt.substring(0, strLength - 1);
			strLength--;
			endsWithNewLine = true;
		}

        // The -1 was needed in the methods that called this one
        // to determine if a timestamp was needed. We need to set
        // it set to 0 here
        if (sLastPosition == -1) sLastPosition = 0;

        // Loop through and print each line of the text
		while (true)
		{
			boolean foundCR = false;

            // If not at a start of new trace message or if last output position
            // happens to be fully at the desired trace width then output a margin
			if (startIndex != 0 || (sLastPosition == TRACE_WIDTH))
            {
                // If we are at the end of the trace width then need to output
                // a carriage return to move to the next line
                if (sLastPosition == TRACE_WIDTH) out.println("");

                for (int count = 0; count < sMargin; count++) out.print(" ");

                sLastPosition = sMargin;
            }

            // The end index is the start index plus what ever
            // space is left for output before trace width is met
            endIndex = startIndex + (TRACE_WIDTH - sLastPosition);

            // If the end index is greater than the strings length
            // then set it to the end of the string
            if (endIndex > strLength) endIndex = strLength;

			// If there's a carriage return before endIndex then set
            // endIndex to it
			int crIndex = txt.substring(startIndex, endIndex).indexOf('\n');

			if (crIndex != -1)
			{
				endIndex = startIndex + crIndex;
				foundCR = true;
			}

            // If we're at the end of the string then print it out
            // with no carriage return, make a note of the current
            // screen position and break out of the loop
            if (endIndex == strLength)
            {
                out.print(txt.substring(startIndex, endIndex));
                sLastPosition += endIndex - startIndex;
                break;
            }

            // If we get here then print out the current line
            out.println(txt.substring(startIndex, endIndex));
            sLastPosition = 0;
            startIndex = endIndex;
			if (foundCR) startIndex++;
        }

        // If we originally stripped a carriage return from the
        // text then print it out here
		if (endsWithNewLine)
		{
			out.println("");
			sLastPosition = -1;
		}
    }

    /**
     * Check for various JVM parameters and set variables
     * accordingly
     */
	private static void initialize()
	{
		// Get any -D options
		String tmpString = System.getProperty("TRACE_OUT");

		try
		{
			int tmpValue = Integer.parseInt(tmpString);
			TRACE_OUT = tmpValue;
		}
		catch (Exception e) {}

		tmpString = System.getProperty("TRACE_ERROR");

		try
		{
			int tmpValue = Integer.parseInt(tmpString);
			TRACE_ERROR = tmpValue;
		}
		catch (Exception e) {}

		tmpString = System.getProperty("TRACE_WIDTH");

		try
		{
			int tmpValue = Integer.parseInt(tmpString);
			TRACE_WIDTH = tmpValue;
		}
		catch (Exception e) {}

		tmpString = System.getProperty("TRACE_VERSION");

		if (TRACE_OUT > 0 && !"HIDE".equals(tmpString)) System.out.println("Trace Formatter by Scott Clee (Version " + sVERSION + ")\n");
	}
}