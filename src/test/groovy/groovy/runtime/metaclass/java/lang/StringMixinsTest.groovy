package groovy.runtime.metaclass.java.lang

import com.objectpartners.groovy.ext.CustomMetaClassCreationHandle
import org.junit.BeforeClass
import org.junit.Test

/**
 * User: danielwoods
 * Date: 7/26/13
 */
class StringMixinsTest {

    @BeforeClass
    static void init() {
        GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(new CustomMetaClassCreationHandle())
    }

    @Test
    void testToJson() {
        def json = '{ "id": "1", "name": "Dan Woods", "twitter": "@danveloper" }'.toJson()

        assert json instanceof Map
        assert json.id == "1"
    }
}
