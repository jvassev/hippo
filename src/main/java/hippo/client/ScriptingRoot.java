package hippo.client;

import hippo.client.impl.Constructor;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.ScriptableObject;

public class ScriptingRoot extends ScriptableObject {

    private static final long serialVersionUID = 1620417396498909133L;
    private Set<String> knownTypes;
    private final DefaultScriptingSession session;

    public void registerType(String type) {
        knownTypes.add(type);
        ScriptableObject.putProperty(this, type, new Constructor(session, type));
    }

    public ScriptingRoot(DefaultScriptingSession session) throws RemoteException {
        knownTypes = new HashSet<String>();
        this.session = session;
        for (String type : session.getTypes()) {
            registerType(type);
        }
    }

    @Override
    public String getClassName() {
        return "ScriptingRoot";
    }
}
