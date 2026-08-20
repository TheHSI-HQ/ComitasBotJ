package cloud.thehsi.ComitasBotJ.Sandbox;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class SandboxPluginClassLoader extends URLClassLoader {

    public SandboxPluginClassLoader(URL pluginJar, ClassLoader parent) {
        super(new URL[]{pluginJar}, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";

        URL resource = findResource(path);

        if (resource == null) {
            throw new ClassNotFoundException(name);
        }

        try (var input = resource.openStream()) {
            byte[] original = input.readAllBytes();
            byte[] transformed = SandboxTransformer.transform(original);

            return defineClass(
                    name,
                    transformed,
                    0,
                    transformed.length
            );
        } catch (IOException e) {
            throw new ClassNotFoundException(
                    "Failed to load plugin class: " + name,
                    e
            );
        }
    }
}