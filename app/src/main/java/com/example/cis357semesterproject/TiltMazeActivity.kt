package com.example.cis357semesterproject

import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


enum class Direction {
    UP, DOWN, LEFT, RIGHT
}
class TiltMazeActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Initialize accelerometer and gyroscope sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Create GameView and set it as the content view
        gameView = GameView(this, null)
        setContentView(gameView)
    }

    /*
        for the sake of attempting to get this to chooch (11/26)
        i'm leaving onAccuracyChanged and onSensorChanged blank here
        this was suggested because there are already some logic made in the
        GameView class down below, not sure what we'd implement up here.

        Corey
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Implement your sensor handling logic here
        // You can leave it empty if you don't need to handle sensor changes in the activity
    }


    // Register and unregister sensors in onResume and onPause methods
    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    inner class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs), SensorEventListener {
        private var COLUMNS = 5
        private var ROWS = 5
        private var cells: Array<Array<Cell>> = arrayOf()

        private lateinit var player: Cell
        private lateinit var exit: Cell

        private val WALL_THICKNESS = 4f
        private var cellSize = 0f
        private var hMargin = 0f
        private var vMargin = 0f
        private val wallPaint = Paint()
        private val playerPaint = Paint()
        private val exitPaint = Paint()

        private val random = SecureRandom()

        private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        init {
            sensorManager.registerListener(
                this,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            wallPaint.color = Color.BLACK
            wallPaint.strokeWidth = WALL_THICKNESS

            playerPaint.color = Color.RED

            exitPaint.color = Color.BLUE

            createMaze()
        }

        private val gyroscopeListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Do nothing
            }

            override fun onSensorChanged(event: SensorEvent?) {
                // Handle gyroscope data to control player movement
                if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    val angularSpeedX = event.values[0]
                    val angularSpeedY = event.values[1]

                    // Customize player movement based on gyroscope data
                    // Example: Adjust player position based on angular speed
                    if (angularSpeedX > 10f) movePlayer(Direction.RIGHT)
                    else if (angularSpeedX < -10f) movePlayer(Direction.LEFT)

                    if (angularSpeedY > 10f) movePlayer(Direction.DOWN)
                    else if (angularSpeedY < -10f) movePlayer(Direction.UP)
                }
            }
        }

        override fun onDetachedFromWindow() {
            // Unregister sensor listener when the view is detached
            sensorManager.unregisterListener(gyroscopeListener)
            super.onDetachedFromWindow()
        }

        private fun createMaze() {
            val stack = Stack<Cell>()
            var current: Cell
            var next: Cell?

            cells = Array(COLUMNS) { column ->
                Array(ROWS) { row ->
                    Cell(column, row)
                }
            }

            player = cells[0][0]
            exit = cells[COLUMNS - 1][ROWS - 1]

            current = cells[0][0]
            current.visited = true

            do {
                next = getNeighbour(current)
                if (next != null) {
                    removeWall(current, next)
                    stack.push(current)
                    current = next
                    current.visited = true
                } else {
                    current = stack.pop()
                }
            } while (stack.isNotEmpty())
        }

        private fun getNeighbour(current: Cell): Cell? {
            val neighbours = ArrayList<Cell>()

            // left neighbour
            if (current.column > 0 && !cells[current.column - 1][current.row].visited)
                neighbours.add(cells[current.column - 1][current.row])

            // right neighbour
            if (current.column < COLUMNS - 1 && !cells[current.column + 1][current.row].visited)
                neighbours.add(cells[current.column + 1][current.row])

            // top neighbour
            if (current.row > 0 && !cells[current.column][current.row - 1].visited)
                neighbours.add(cells[current.column][current.row - 1])

            // bottom neighbour
            if (current.row < ROWS - 1 && !cells[current.column][current.row + 1].visited)
                neighbours.add(cells[current.column][current.row + 1])

            return if (neighbours.isNotEmpty()) {
                val index = random.nextInt(neighbours.size)
                neighbours[index]
            } else null
        }

        private fun removeWall(current: Cell, next: Cell) {
            when {
                current.column == next.column && current.row == next.row + 1 -> {
                    current.topWall = false
                    next.bottomWall = false
                }

                current.column == next.column && current.row == next.row - 1 -> {
                    current.bottomWall = false
                    next.topWall = false
                }

                current.column == next.column + 1 && current.row == next.row -> {
                    current.leftWall = false
                    next.rightWall = false
                }

                current.column == next.column - 1 && current.row == next.row -> {
                    current.rightWall = false
                    next.leftWall = false
                }
            }
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.GREEN)

            val width = width
            val height = height

            cellSize = if (width / height < COLUMNS.toFloat() / ROWS.toFloat()) {
                width / (COLUMNS + 1).toFloat()
            } else {
                height / (ROWS + 1).toFloat()
            }

            hMargin = (width - COLUMNS * cellSize) / 2
            vMargin = (height - ROWS * cellSize) / 2

            canvas.translate(hMargin, vMargin)

            for (x in 0 until COLUMNS) {
                for (y in 0 until ROWS) {
                    val cell = cells[x][y]

                    if (cell.topWall) {
                        canvas.drawLine(
                            x * cellSize, y * cellSize,
                            (x + 1) * cellSize, y * cellSize,
                            wallPaint
                        )
                    }

                    if (cell.leftWall) {
                        canvas.drawLine(
                            x * cellSize, y * cellSize,
                            x * cellSize, (y + 1) * cellSize,
                            wallPaint
                        )
                    }

                    if (cell.bottomWall) {
                        canvas.drawLine(
                            x * cellSize, (y + 1) * cellSize,
                            (x + 1) * cellSize, (y + 1) * cellSize,
                            wallPaint
                        )
                    }

                    if (cell.rightWall) {
                        canvas.drawLine(
                            (x + 1) * cellSize, y * cellSize,
                            (x + 1) * cellSize, (y + 1) * cellSize,
                            wallPaint
                        )
                    }
                }
            }

            val margin = cellSize / 10

            //player rectangle

            canvas.drawRect(
                player.column * cellSize + margin,
                player.row * cellSize + margin,
                (player.column + 1) * cellSize - margin,
                (player.row + 1) * cellSize - margin,
                playerPaint
            )

            //exit rectangle

            canvas.drawRect(
                exit.column * cellSize + margin,
                exit.row * cellSize + margin,
                (exit.column + 1) * cellSize - margin,
                (exit.row + 1) * cellSize - margin,
                exitPaint
            )
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) return true

            if (event.action == MotionEvent.ACTION_MOVE) {
                val x = event.x
                val y = event.y

                val playerCenterX = hMargin + (player.column + 0.5f) * cellSize
                val playerCenterY = vMargin + (player.row + 0.5f) * cellSize

                val dx = x - playerCenterX
                val dy = y - playerCenterY

                val absDx = abs(dx)
                val absDy = abs(dy)

                if (absDx > cellSize || absDy > cellSize) {
                    if (absDx > absDy) {
                        // move in x-direction
                        if (dx > 0) movePlayer(Direction.RIGHT) //all Direction.DIRECTION FIXME
                        else movePlayer(Direction.LEFT)
                    } else {
                        // move in y-direction
                        if (dy > 0) movePlayer(Direction.DOWN)
                        else movePlayer(Direction.UP)
                    }
                }

                return true
            }

            return super.onTouchEvent(event)
        }

        private fun movePlayer(direction: Direction) {
            when (direction) {
                Direction.UP -> if (!player.topWall) player = cells[player.column][player.row - 1]
                Direction.DOWN -> if (!player.bottomWall) player = cells[player.column][player.row + 1]
                Direction.LEFT -> if (!player.leftWall) player = cells[player.column - 1][player.row]
                Direction.RIGHT -> if (!player.rightWall) player = cells[player.column + 1][player.row]
            }

            checkExit()
            invalidate()
        }

        private fun checkExit() {
            if (player == exit) {
                COLUMNS++
                ROWS++
                createMaze()
            }
        }

        private inner class Cell(var column: Int, var row: Int) {
            var topWall = true
            var leftWall = true
            var rightWall = true
            var bottomWall = true
            var visited = false
        }

        /*
        onSensorChanged had issues with SensorEvent(?), I took out the question mark, but that insinuates
        that the sensor will NEVER be null, which in our case would throw a NULLPOINTEREXCEPTION.

        we don't want that, so I'm suggesting we implement some sort of if-null catching cases and
        hopefully fixing it later.

        I initialized some logs to give us some input onto what was breaking and how, we can implement
        what the code does later at this point

        COREY
        */
        override fun onSensorChanged(event: SensorEvent) {

            if (event != null) {

                // Check if the sensor in the event matches the gyroscope sensor

                if (event.sensor == gyroscopeSensor) {

                    // Check if values array is not null and has at least one element

                    if (event.values?.isNotEmpty() == true) {
                        val angularSpeedX = event.values[1]
                        val angularSpeedY = event.values[0]

                        // Customize player movement based on gyroscope data
                        // Example: Adjust player position based on angular speed
                        if (angularSpeedX > 2.0f) movePlayer(Direction.RIGHT)
                        else if (angularSpeedX < -2.0f) movePlayer(Direction.LEFT)

                        if (angularSpeedY > 2.0f) movePlayer(Direction.DOWN)
                        else if (angularSpeedY < -2.0f) movePlayer(Direction.UP)
                    } else {
                        // Handle the case when values array is null or empty
                        // Example: Log a warning or set default values
                        Log.w("SensorEvent", "Values array is null or empty")

                        // Set default values or take appropriate action
                        // For example:
                        // val defaultAngularSpeedX = 0.0f
                        // val defaultAngularSpeedY = 0.0f
                        // movePlayer(Direction.STOP) // Stop player movement or set default direction TODO
                    }
                } else {
                    // Handle the case when the sensor in the event is not the gyroscope sensor
                    // Example: Log a warning or take appropriate action
                    Log.w("SensorEvent", "Event is not from the gyroscope sensor")

                    // Take appropriate action
                    // For example:
                    // ignore the event or stop processing TODO
                }
            } else {
                // Handle the case when the event is null
                // Example: Log a warning or take appropriate action
                Log.w("SensorEvent", "Received a null SensorEvent")

                // Take appropriate action
                // For example:
                // ignore the event or stop processing TODO
            }

        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, "Sensor accuracy changed: $accuracy for sensor: $sensor")
            //TODO("Not yet implemented")
        }


    }

}
