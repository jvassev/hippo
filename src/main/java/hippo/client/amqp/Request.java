package hippo.client.amqp;

import hippo.client.Proxy;

import java.io.Serializable;


public class Request implements Serializable {

    private static final long serialVersionUID = 6303471534106933878L;

    public static final int getApiDefinition = 8;

    public static final int getProperty = 1;

    public static final int setProperty = 2;

    public static final int getVariable = 3;

    public static final int closeSession = 4;

    public static final int newObject = 5;

    public static final int invokeMethod = 6;

    public static final int openSession = 7;


    private int requestType;

    private Proxy self;

    private String variableName;

    private Object[] args;

    private String method;

    private String propertyName;

    private String typeName;

    private Object value;

    private String sessionId;


    public int getRequestType() {
        return requestType;
    }


    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }


    public Proxy getSelf() {
        return self;
    }


    public void setSelf(Proxy self) {
        this.self = self;
    }


    public String getVariableName() {
        return variableName;
    }


    public void setVariableName(String variable) {
        this.variableName = variable;
    }


    public Object[] getArgs() {
        return args;
    }


    public void setArgs(Object[] args) {
        this.args = args;
    }


    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }


    public String getPropertyName() {
        return propertyName;
    }


    public void setPropertyName(String property) {
        this.propertyName = property;
    }


    public String getTypeName() {
        return typeName;
    }


    public void setTypeName(String type) {
        this.typeName = type;
    }


    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }


    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public String getSessionId() {
        return sessionId;
    }
}
