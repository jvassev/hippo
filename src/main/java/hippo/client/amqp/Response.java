package hippo.client.amqp;

import java.io.Serializable;


public class Response implements Serializable {

    private static final long serialVersionUID = -714564076095163731L;

    private Object result;

    private Throwable exception;

    public Object getResult() {
        return result;
    }


    public void setResult(Object result) {
        this.result = result;
    }


    public Throwable getException() {
        return exception;
    }


    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
