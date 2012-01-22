package hippo.example;

import hippo.client.ScriptingSessionFactory;
import hippo.client.amqp.AmqpScriptingSessionFactoryFinder;
import hippo.client.rhino.DefaultScriptingSession;
import hippo.client.rhino.ScriptingRoot;
import hippo.client.rhino.SessionCache;
import hippo.example.domain.LocalCounter;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Client {

    public static void main(String[] args) throws Exception {
        // Registry registry = findRegistry(args);
        // RmiScriptingSessionFactoryFinder finder = new
        // RmiScriptingSessionFactoryFinder(registry);
        // ScriptingSessionFactory counterFactory =
        // finder.findFactory("Counter");
        // ScriptingSessionFactory timerFactory = finder.findFactory("Timer");
        // ScriptingSessionFactory describerFactory =
        // finder.findFactory("Describer");

        Connection connection = makeConnection();
        AmqpScriptingSessionFactoryFinder finder = new AmqpScriptingSessionFactoryFinder(connection);
        ScriptingSessionFactory counterFactory = finder.findFactory("Counter");
        ScriptingSessionFactory timerFactory = finder.findFactory("Timer");
        ScriptingSessionFactory describerFactory = finder.findFactory("Describer");

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
            System.exit(0);
        }
    }

    private static Context enterContext() {
        Context cx = Context.enter();
        Context.getCurrentContext().setOptimizationLevel(-1);
        return cx;
    }

    public static Registry findRegistry(String[] args) throws RemoteException {
        String server = "localhost";
        if (args.length == 1) {
            server = args[0];
        }
        Registry registry = LocateRegistry.getRegistry(server);
        return registry;
    }

    private static Connection makeConnection() throws IOException {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost("localhost");
        cf.setPort(5672);
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setVirtualHost("/");
        return cf.newConnection();
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
