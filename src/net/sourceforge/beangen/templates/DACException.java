package ejb.exception;

// Standard imports
import java.io.*;

public class DACException extends java.lang.Exception {

    /** Error code for unknown error field */
    private String message = "DataAccessCommand Unknown Error";

    /** Stored <code>Exception</code> for root cause */
    private Throwable rootCause;

    public DACException() {
        super();
    }

    public DACException(String message) {
        super( message );
        this.message = message;
    }

    public DACException(Throwable rootCause) {
        this.rootCause = rootCause;
    }

    public DACException(String message, Throwable rootCause) {
        this( message );
        this.rootCause = rootCause;
    }

    public String getMessage() {
        if (rootCause != null) {
            return super.getMessage() + ": " + rootCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (rootCause != null) {
            System.err.println("Root cause: ");
            rootCause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        if (rootCause != null) {
            stream.print("Root cause: ");
            rootCause.printStackTrace(stream);
        }
    }

    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (rootCause != null) {
            writer.print("Root cause: ");
            rootCause.printStackTrace(writer);
        }
    }

    public Throwable getRootCause() {
        return rootCause;
    }
}