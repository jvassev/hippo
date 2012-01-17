package hippo.server;

import hippo.client.ScriptingSession;


public interface SessionLocator {

    ScriptingSession lookup(String sessionId);
}
