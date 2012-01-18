package hippo.example;

import hippo.client.ScriptingSessionFactory;
import hippo.client.rhino.DefaultScriptingSession;
import hippo.client.rhino.ScriptingRoot;
import hippo.client.rhino.SessionCache;
import hippo.example.domain.LocalCounter;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Client {

    public static void main(String[] args) throws Exception {
        Registry registry = findRegistry(args);

        ScriptingSessionFactory counterFactory = (ScriptingSessionFactory) registry.lookup("Counter");
        ScriptingSessionFactory timerFactory = (ScriptingSessionFactory) registry.lookup("Timer");
        ScriptingSessionFactory describerFactory = (ScriptingSessionFactory) registry.lookup("Describer");


        Context cx = enterContext();
        try {
            Scriptable std = cx.initStandardObjects();
            ScriptableObject.defineClass(std, LocalCounter.class);
            Scriptable root = chain(std, counterFactory, timerFactory, describerFactory);

            long start = System.currentTimeMillis();

            cx.evaluateString(root, "Packages.java.lang.System.out.println(env.value);var c = new Counter(0);\n"
                    + "for (i=0; i < 5000; i++) {\n" + "  c.inc();\n" + "}\n" + "\n"
                    + "Packages.java.lang.System.out.println(c.value);"
                    + "Packages.java.lang.System.out.println('elapsed: ' + timer.elapsed);"
                    + "Packages.java.lang.System.out.println(describer.describe(c));" + "var t = describer.touch(c);"
                    + "Packages.java.lang.System.out.println(t.value);", "<no file>", -1, null);
            System.out.println(System.currentTimeMillis() - start);
        } finally {
            Context.exit();
        }
    }

    private static Context enterContext() {
        Context cx = Context.enter();
        Context.getCurrentContext().setOptimizationLevel(-1);
        return cx;
    }

    private static Registry findRegistry(String[] args) throws RemoteException {
        String server = "localhost";
        if (args.length == 1) {
            server = args[0];
        }
        Registry registry = LocateRegistry.getRegistry(server);
        return registry;
    }

    private static Scriptable chain(Scriptable root, ScriptingSessionFactory... factories) throws RemoteException {
        Scriptable next = root;
        SessionCache cache = new SessionCache();
        for (ScriptingSessionFactory scriptingSessionFactory : factories) {
            DefaultScriptingSession session = new DefaultScriptingSession(scriptingSessionFactory, cache);
            ScriptingRoot r = new ScriptingRoot(session);
            r.setPrototype(next);
            next = r;
        }

        return next;
    }
}
