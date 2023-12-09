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
import androidx.navigation.findNavController
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
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //left empty to satisfy sensor logic
    }

    override fun onSensorChanged(event: SensorEvent) {
        //left empty to satisfy sensor logic
    }

    //register and deregister sensors when onPause is active and onResume is active.
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

        private var isGameFinished = false

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

            if (isGameFinished) {
                // Display a message or alert the user that the maze is completed
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 40f
                }
                canvas.drawText("Maze Completed!", width / 4f, height / 2f, paint)

                findNavController().navigate(R.id.levelActivity2)
            }
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
                isGameFinished = true


            }
        }

        private inner class Cell(var column: Int, var row: Int) {
            var topWall = true
            var leftWall = true
            var rightWall = true
            var bottomWall = true
            var visited = false
        }

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

                        Log.w("SensorEvent", "Values array is null or empty")

                    }
                } else {

                    Log.w("SensorEvent", "Event is not from the gyroscope sensor")

                }
            } else {

                Log.w("SensorEvent", "Received a null SensorEvent")
            }

        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, "Sensor accuracy changed: $accuracy for sensor: $sensor")
        }


    }

}
