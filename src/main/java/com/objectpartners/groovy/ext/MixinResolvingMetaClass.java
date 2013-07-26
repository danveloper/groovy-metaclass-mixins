package com.objectpartners.groovy.ext;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: danielwoods
 * Date: 7/25/13
 */
class MixinResolvingMetaClass extends MetaClassImpl {
    static final Map<Class, Class> cachedMixins = new ConcurrentHashMap<>();

    MixinResolvingMetaClass(MetaClassRegistry registry, Class theClass) {
        super(registry, theClass);
    }

    // This is the method that gets called when you invoke a method on a class
    public Object invokeMethod(Object obj, String name, Object[] args) {
        Object mixin = cachedMixins.get(obj.getClass());
        if (mixin == null) {
            try {
                // Resolve the mixin from the convention package and class nomenclature
                // In this case, we'll use `groovy.runtime.metaclass.[package].[class]Mixins` to find the mixins for a given class
                Class mixinClass = Class.forName("groovy.runtime.metaclass."+obj.getClass().getName()+"Mixins");
                cachedMixins.put(obj.getClass(), mixinClass);
                mixin = cachedMixins.get(obj.getClass());
            } catch (ClassNotFoundException e) {
                // There was no mixin class discovered, so kick this call up north for processing
                return super.invokeMethod(obj, name, args);
            }

        }

        // Shouldn't happen, but never-say-never :-)
        if (mixin == null) throw new RuntimeException("Could not resolve base class for invocation.");

        // Invoke the discovered mixin
        return handleStaticInvoke((Class) mixin, obj, name, args);
    }

    private Object handleStaticInvoke(Class mixin, Object obj, String name, Object[] args) {
        // Mixin methods are always defined as static methods, so try to discover them here.
        Method staticMethod = getStaticMethod(mixin, name, argsWithSelf(obj, args));

        // If the mixin didn't have this method don't exist...
        if (staticMethod == null) {

            // Try to resolve the static method call from the static meta method registry.
            // This call is also responsible for resolving receiver method calls that were *actually* static method calls.
            MetaMethod metaMethod = getStaticMetaMethod(name, args);
            if (metaMethod != null) {
                // Invoke the resolved method and return the result.
                return metaMethod.invoke(mixin, args);
            }

            // If we still didn't find a method, throw the expected exception.
            throw new MissingMethodExceptionNoStack(name, theClass, args, true);

        }

        // If the mixin did have this method, then go ahead and invoke it and return the result.
        return invokeStaticMethod(mixin, name, argsWithSelf(obj, args));
    }

    private Object[] argsWithSelf(Object self, Object[] args) {
        Object[] selfArgs = new Object[args.length+1];
        selfArgs[0] = self;
        for (int i=0;i<args.length;i++) {
            selfArgs[i+1] = args[i];
        }
        return selfArgs;
    }

    private static Object invokeStaticMethod(Class target, String name, Object[] args) {
        try {
            return getStaticMethod(target, name, args).invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getStaticMethod(Class target, String name, Object[] args) {
        try {
            return target.getMethod(name, MetaClassHelper.castArgumentsToClassArray(args));
        } catch (Exception e) {
            return null;
        }
    }
}
