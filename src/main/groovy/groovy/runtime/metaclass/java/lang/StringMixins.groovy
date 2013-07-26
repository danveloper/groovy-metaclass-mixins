package groovy.runtime.metaclass.java.lang

import groovy.json.JsonSlurper

/**
 * User: danielwoods
 * Date: 7/25/13
 */
class StringMixins {

    static def toJson(String input) {
        new JsonSlurper().parseText(input)
    }
}
