package com.example.cis357semesterproject

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast


class MazeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mazeView = MazeView(this)
        setContentView(mazeView)
    }
}

/*
0 start
1 empty
2 solid
3 end
*/

class MazeView(context: Context) : View(context), SensorEventListener {

    private lateinit var ballBitmap: Bitmap
    private var ballX = 700f
    private var ballY = 50f
    private var cellWidth = width / 10f
    private var cellHeight = height / 20f



    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    init {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        ballBitmap = BitmapFactory.decodeResource(resources, R.drawable.ball3)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellWidth = w / 10f
        cellHeight = h / 20f
    }

    private fun isSolidBlock(x: Float, y: Float): Boolean {
        val gridX = (x / cellWidth).toInt()
        val gridY = (y / cellHeight).toInt()

        // Boundary check to avoid ArrayIndexOutOfBoundsException
        if (gridX < 0 || gridY < 0 || gridX >= mazeArray[0].size || gridY >= mazeArray.size) {
            return false
        }

        return mazeArray[gridY][gridX] == 2
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Update ball position based on accelerometer data
            val xAcceleration = event.values[0]
            val yAcceleration = event.values[1]

            // Adjust the ball's position based on accelerometer data
            ballX -= xAcceleration
            ballY += yAcceleration

            // collision variables
            val newX = ballX - xAcceleration
            val newY = ballY + yAcceleration

            //check for collision
            if(isSolidBlock(newX, newY)){
                ballX = newX
                ballY = newY
            }

            adjustBallPosition()

            // Redraw the view to update the ball's position
            invalidate()
        }
    }

    private fun adjustBallPosition(){
        // Adjust ball position to prevent it from going off-screen or into solid blocks
        if (ballX < 0 || isSolidBlock(ballX, ballY)) ballX = 0f
        if (ballX > width - ballBitmap.width || isSolidBlock(ballX, ballY))
        ballX = (width - ballBitmap.width).toFloat()
        if (ballY < 0 || isSolidBlock(ballX, ballY)) ballY = 0f
        if (ballY > height - ballBitmap.height || isSolidBlock(ballX, ballY))
        ballY = (height - ballBitmap.height).toFloat()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this example
    }

    private val floorPaint = Paint().apply {
        color = Color.BLACK
    }

    private val emptyPaint = Paint().apply {
        color = Color.WHITE
    }



    private val mazeArray: Array<IntArray> = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(2, 2, 2, 2, 2, 2, 1, 1, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 1, 2, 2, 2, 2, 2, 2, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 2, 2, 2, 1, 1, 2, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 2, 1, 1, 2, 2, 2, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 1, 1, 2, 2, 2, 2, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 2, 2, 2, 2, 2, 1, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 1, 2, 2, 2, 2, 2, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 2, 2, 2, 2, 2, 1, 1, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 1, 1, 2, 2, 2, 2, 2, 2),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // empty row for ball to roll
        intArrayOf(2, 2, 2, 2, 2, 2, 2, 2, 3, 2)
    )

    //TODO: Fix ball position and size, and collision logic
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        super.onDraw(canvas)

        val cellWidth = width / 10f
        val cellHeight = height / 20f


        for (i in mazeArray.indices) {
            for (j in mazeArray[i].indices) {
                val left = j * cellWidth
                val top = i * cellHeight
                val right = (j + 1) * cellWidth
                val bottom = (i + 1) * cellHeight

                when (mazeArray[i][j]) {
                    0, 1, 3 -> canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        emptyPaint
                    )

                    2 -> canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        floorPaint
                    )
                }
            }
        }
        canvas.drawBitmap(ballBitmap, ballX, ballY, null)

        checkForEndgame(cellWidth, cellHeight)
    }

    private fun checkForEndgame(cellWidth: Float, cellHeight: Float){
        val ballColumn = (ballX / cellWidth).toInt()
        val ballRow = (ballY / cellHeight).toInt()

        if(mazeArray[ballRow][ballColumn] == 3){
            endGame()
        }

    }
    private fun endGame(){
        (context as Activity).runOnUiThread{

            Toast.makeText(context, "Game Over, you won!", Toast.LENGTH_LONG).show()

        }

    }

}
