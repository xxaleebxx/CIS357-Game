package com.example.cis357semesterproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.graphics.BitmapFactory


class MazeActivity : AppCompatActivity() {
    private lateinit var ballBitmap: Bitmap



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

class MazeView(context: Context) : View(context) {

    private lateinit var ballBitmap: Bitmap
    private var ballX = 50f
    private var ballY = 50f

    init {
        ballBitmap = BitmapFactory.decodeResource(resources, R.drawable.ball)
    }


    private val floorPaint = Paint().apply {
        color = Color.BLACK
    }

    private val emptyPaint = Paint().apply {
        color = Color.WHITE
    }

    private val mazeArray: Array<IntArray> = arrayOf(
        intArrayOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1),
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

    //TO DO: Fix ball position and size, and collision logic
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        super.onDraw(canvas)
        val cellWidth = width / 10
        val cellHeight = height / 20

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
    }
}
