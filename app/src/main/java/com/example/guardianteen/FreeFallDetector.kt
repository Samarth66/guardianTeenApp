import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class FreeFallDetector(private val context: Context, private val onFreeFallDetected: () -> Unit) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this context
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            detectFreeFall(it)
        }
    }

    private fun detectFreeFall(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt((x * x + y * y + z * z).toDouble())

        val freeFallThreshold = 2.0 // Adjust this threshold based on your needs

        if (magnitude < freeFallThreshold) {
            onFreeFallDetected()
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
