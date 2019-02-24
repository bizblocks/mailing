package com.lokoproject.mailing.utils;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Antonlomako. created on 17.02.2019.
 */
public class ReflectionHelper {



    public static List<Class<?>> findClassesImplementing(final Class<?> interfaceClass, final Package fromPackage,ClassLoader classLoader) {

        if (interfaceClass == null) {
            return null;
        }

        if (fromPackage == null) {
            return null;
        }

        final List<Class<?>> rVal = new ArrayList<Class<?>>();
        try {
            final Class<?>[] targets = getAllClassesFromPackage(fromPackage.getName(),classLoader);
            for (Class<?> aTarget : targets) {
                if (aTarget == null) {
                    continue;
                }
                else if (aTarget.equals(interfaceClass)) {
                    continue;
                }
                else if (!interfaceClass.isAssignableFrom(aTarget)) {
                    continue;
                }
                else {
                    rVal.add(aTarget);
                }
            }
        }
        catch (Exception ignored) {
        }

        return rVal;
    }

    /**
     * Load all classes from a package.
     *
     */
    public static Class[] getAllClassesFromPackage(final String packageName,ClassLoader classLoader) throws ClassNotFoundException, IOException {
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Find file in package
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if(files==null) return classes;
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }


}
