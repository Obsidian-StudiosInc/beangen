package ejb.exception;

// Standard imports
import java.io.*;

public class DisconnectedBeanException extends java.lang.Exception {

    /** Error code for unknown mandatory field */
    private String message = "bean/command.unknown";

    /** Stored <code>Exception</code> for root cause */
    private Throwable rootCause;

    public DisconnectedBeanException() {
        super();
    }

    public DisconnectedBeanException(String message) {
        super( message );
        this.message = message;
    }

    public DisconnectedBeanException(Throwable rootCause) {
        this.rootCause = rootCause;
    }

    public DisconnectedBeanException(String message, Throwable rootCause) {
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