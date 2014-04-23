package scripting;

import clientside.MapleCharacter;
import clientside.MapleClient;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import server.MaplePortal;
import tools.FileoutputUtil;

public class PortalScriptManager {

    private static final PortalScriptManager instance = new PortalScriptManager();
    private final Map<String, PortalScript> scripts = new HashMap();
    private static final ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("javascript").getFactory();

    public static final PortalScriptManager getInstance() {
        return instance;
    }

    private final PortalScript getPortalScript(String scriptName) {
        if (this.scripts.containsKey(scriptName)) {
            return (PortalScript) this.scripts.get(scriptName);
        }

        File scriptFile = new File("scripts/portal/" + scriptName + ".js");
        if (!scriptFile.exists()) {
            return null;
        }

        FileReader fr = null;
        ScriptEngine portal = sef.getScriptEngine();
        try {
            fr = new FileReader(scriptFile);
            CompiledScript compiled = ((Compilable) portal).compile(fr);
            compiled.eval();
        } catch (Exception e) {
            System.err.println("Error executing Portalscript: " + scriptName + ":" + e);
            FileoutputUtil.log("Log_Script_Except.txt", "Error executing Portal script. (" + scriptName + ") " + e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    System.err.println("ERROR CLOSING" + e);
                }
            }
        }
        PortalScript script = (PortalScript) ((Invocable) portal).getInterface(PortalScript.class);
        this.scripts.put(scriptName, script);
        return script;
    }

    public final void executePortalScript(MaplePortal portal, MapleClient c) {
        PortalScript script = getPortalScript(portal.getScriptName());

        if (script != null) {
            try {
                script.enter(new PortalPlayerInteraction(c, portal));
            } catch (Exception e) {
                System.err.println("Error entering Portalscript: " + portal.getScriptName() + " : " + e);
            }
        } else {
            System.out.println("Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
            FileoutputUtil.log("Log_Script_Except.txt", "Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
        }
    }

    public final void clearScripts() {
        this.scripts.clear();
    }
}