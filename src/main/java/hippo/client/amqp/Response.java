package hippo.client.amqp;

import java.io.Serializable;


public class Response implements Serializable {

    private static final long serialVersionUID = -714564076095163731L;

    private Serializable result;

    private Throwable exception;

    public Serializable getResult() {
        return result;
    }


    public void setResult(Serializable result) {
        this.result = result;
    }


    public Throwable getException() {
        return exception;
    }


    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
