package hippo.example;

import hippo.client.DefaultScriptingSession;
import hippo.client.ScriptingRoot;
import hippo.client.ScriptingSessionService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Client {
    public static void main(String[] args) throws IOException, NotBoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        Registry registry = LocateRegistry.getRegistry("localhost");

        ScriptingSessionService service = (ScriptingSessionService) registry.lookup("ScriptingSessionRegistry");

        DefaultScriptingSession session = new DefaultScriptingSession(service);
        session.start();
        // //////////

        Context cx = Context.enter();
        Context.getCurrentContext().setOptimizationLevel(-1);
        try {
            Scriptable std = cx.initStandardObjects();
            ScriptableObject.defineClass(std, LocalCounter.class);
            ScriptingRoot root = new ScriptingRoot(session);
            root.setPrototype(std);

            // Object res = cx.evaluateReader(root, new
            // StringReader("var x = new Counter();\n" + "x.inc();\n"
            // + "x.inc(); " + "var y = x.clone(); " + "x.inc();"
            // +
            // " Packages.java.lang.System.out.println(x.get() + ' ' + y.get());"
            // + " x.copy(y);"
            // +
            // "Packages.java.lang.System.out.println(x.get() + ' ' + y.get());"),
            // "<>", 1, null);

            long start = System.currentTimeMillis();

            cx.evaluateString(root, "var c = new LocalCounter(0);\n" + "for (i=0; i < 10000; i++) {\n" + "  c.inc();\n"
                    + "}\n" + "\n" + "Packages.java.lang.System.out.println(c.get());", "<>", -1, null);
            System.out.println(System.currentTimeMillis() - start);
        } finally {
            Context.exit();
            session.end();
        }
    }
}
