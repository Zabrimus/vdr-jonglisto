package vdr.jonglisto.lib.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NashornServiceImpl extends ServiceBase {

    private Invocable svdrpScript;

    final private ObjectMapper mapper;
    
    public NashornServiceImpl() {        
        mapper = new ObjectMapper();
    }

    protected void initScript(String filename) throws FileNotFoundException, ScriptException {
        svdrpScript = (Invocable) compileScript(filename).getEngine();
    }
    
    private CompiledScript compileScript(String file) throws FileNotFoundException, ScriptException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        final Compilable compilable = (Compilable) engine;
        final CompiledScript compiled = compilable.compile(new FileReader(file));
        compiled.eval();

        return compiled;
    }

    protected Map<String, Object> callFunction(String name, Object input) throws NoSuchMethodException, ScriptException {
        Map<String, Object> result = new HashMap<>();
        Bindings bindings = (Bindings) svdrpScript.invokeFunction(name, input);
        
        bindings.keySet().stream().forEach(s -> result.put(s, bindings.get(s)));
        return result;
    }    
    
    protected <T> T mapToObject(Map<String, Object> input, Class<T> clazz) {
        return mapper.convertValue(input, clazz);
    }
}
