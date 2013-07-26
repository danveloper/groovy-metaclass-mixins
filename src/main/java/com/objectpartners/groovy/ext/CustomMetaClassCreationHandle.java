package com.objectpartners.groovy.ext;

import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;

/**
 * User: danielwoods
 * Date: 7/25/13
 */
class CustomMetaClassCreationHandle extends MetaClassRegistry.MetaClassCreationHandle {

    protected MetaClass createNormalMetaClass(Class theClass,MetaClassRegistry registry) {
        if (GeneratedClosure.class.isAssignableFrom(theClass)) {
            return new ClosureMetaClass(registry,theClass);
        } else {
            return new MixinResolvingMetaClass(registry, theClass);
        }
    }
}
