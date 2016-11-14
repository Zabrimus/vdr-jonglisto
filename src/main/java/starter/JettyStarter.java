package starter;

import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.runner.Runner;

public class JettyStarter {

    public static void main(String[] args) throws Exception {
        ProtectionDomain domain = JettyStarter.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        List<String> list = new ArrayList<String>(Arrays.asList((String[]) args));
        list.add("--path");
        list.add("/");
        list.add(location.toExternalForm());

        String[] newArgs = new String[list.size()];
        newArgs = list.toArray(newArgs);

        Runner.main(newArgs);
    }
}
