import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class FreeFallActivity(private val context: Context, private val    onFreeFallDetected: () -> Unit) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    var lastHeartRate: Int = -1 // Public to be accessed by the activity

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this context
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> detectFreeFall(it)
                Sensor.TYPE_HEART_RATE -> handleHeartRate(it)
            }
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

    private fun handleHeartRate(event: SensorEvent) {
        lastHeartRate = event.values[0].toInt() // Store the latest heart rate
        Log.d("FreeFallDetector", "Heart Rate updated: $lastHeartRate")

    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
