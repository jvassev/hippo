package hippo.client.rhino;

import hippo.client.ApiDefinition;
import hippo.client.TypeDefinition;
import hippo.client.rhino.impl.Constructor;

import java.rmi.RemoteException;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptingRoot extends ScriptableObject {

    private static final long serialVersionUID = 1620417396498909133L;

    private final DefaultScriptingSession session;

    private ApiDefinition apiDefinition;

    public ScriptingRoot(DefaultScriptingSession session) throws RemoteException {
        this.session = session;
        apiDefinition = session.getApiDefinition();
        defineApi();
    }

    private void defineApi() {
        for (TypeDefinition type : apiDefinition.getTypes().values()) {
            if (type.isInstantiatable()) {
                ScriptableObject.putProperty(this, type.getName(), new Constructor(session, type));
            }
        }
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (apiDefinition.variableDefined(name)) {
            try {
                return session.toJs(session.getVariable(name));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            return super.get(name, start);
        }
    }

    @Override
    public String getClassName() {
        return "ScriptingRoot";
    }
}
