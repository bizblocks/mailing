package com.lokoproject.mailing.utils;


import com.lokoproject.mailing.notification.event.AbstractNotificationEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

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

    public static List getClassesNamesFromJar(String jarName,String packageName) {
        ArrayList classes = new ArrayList();


        try {
            JarInputStream jarFile = new JarInputStream(new FileInputStream(
                    jarName));
            JarEntry jarEntry;

            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if ((jarEntry.getName().contains(packageName))&&(jarEntry.getName().endsWith(".class"))) {

                    classes.add(jarEntry.getName().replaceAll("/", "\\."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
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
        ArrayList<Class> classes = new ArrayList<Class>();
        List<String> classNames=new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String fileName=resource.getFile().contains("file:/")? resource.getFile().replace("file:/",""):resource.getFile();
            classNames.addAll(getClassesNamesFromJar(fileName.substring(0,fileName.indexOf("!")),path));
        }
        classNames.forEach(className->{
            try {
                classes.add(classLoader.loadClass(className.replaceAll(".class","")));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        });

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

    public static Collection<Class> getAllAvailableNotificationEvents(){
        ClassLoader classLoader=ReflectionHelper.class.getClassLoader();
        List<Class> result=new ArrayList<>();
        try {
            for(Class classItem:getAllClassesFromPackage("com.lokoproject.mailing.notification.event",classLoader)){
                if((!classItem.isInterface())&&(!Modifier.isAbstract( classItem.getModifiers()))){
                    result.add(classItem);
                }
            };
            return result;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static AbstractNotificationEvent getNotificationEvent(String name){
        for(Class classItem:getAllAvailableNotificationEvents()){
            if(classItem.getSimpleName().toLowerCase().startsWith(name.toLowerCase())) try {
                return (AbstractNotificationEvent) classItem.newInstance();
            } catch (Exception ignored) {
            }
        };
        throw new IllegalArgumentException("there is no event class with name "+name);
    }


}
