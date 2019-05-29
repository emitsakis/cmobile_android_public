package certh.hit.cmobile

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import certh.hit.cmobile.utils.Helper

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("certh.hit.cmobile", appContext.packageName)
    }

    @Test
    fun getGuid(){
        Helper.createUID()
        assertEquals("certh.hit.cmobile", "certh.hit.cmobile")

    }
}
