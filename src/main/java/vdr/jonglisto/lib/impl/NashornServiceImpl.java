package vdr.jonglisto.lib.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private Invocable nashornScript;

    final private ObjectMapper mapper;

    public NashornServiceImpl() {
        mapper = new ObjectMapper();
    }

    protected void initScript(String filename) throws FileNotFoundException, ScriptException {
        nashornScript = (Invocable) compileScript(filename).getEngine();
    }

    private CompiledScript compileScript(String file) throws FileNotFoundException, ScriptException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        final Compilable compilable = (Compilable) engine;
        final CompiledScript compiled = compilable.compile(new FileReader(file));
        compiled.eval();

        return compiled;
    }

    protected Map<String, Object> callNashornFunction(String name, Object input)
            throws NoSuchMethodException, ScriptException {
        Map<String, Object> result = new HashMap<>();
        Bindings bindings = (Bindings) nashornScript.invokeFunction(name, input);

        bindings.keySet().stream().forEach(s -> result.put(s, bindings.get(s)));
        return result;
    }

    protected List<Map<String, Object>> callNashornFunctionArray(String name, Object input)
            throws NoSuchMethodException, ScriptException {
        List<Map<String, Object>> result = new ArrayList<>();

        Bindings bindings = (Bindings) nashornScript.invokeFunction(name, input);

        for (int i = 0; i < bindings.keySet().size(); ++i) {
            Map<String, Object> r = new HashMap<>();

            Bindings bin = (Bindings) bindings.get(String.valueOf(i));
            bin.keySet().stream().forEach(s -> r.put(s, bin.get(s)));
            result.add(r);
        }

        return result;
    }

    protected <T> T mapToObject(Map<String, Object> input, Class<T> clazz) {
        return mapper.convertValue(input, clazz);
    }
}
