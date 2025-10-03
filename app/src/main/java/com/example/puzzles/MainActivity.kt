package com.example.puzzles

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var puzzleContainer: LinearLayout
    private lateinit var btnShuffle: Button
    private var puzzlePieces = mutableListOf<ImageView>()
    private val rows = 3
    private val cols = 3
    private var pieceWidth = 0
    private var pieceHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        puzzleContainer = findViewById(R.id.puzzleContainer)
        btnShuffle = findViewById(R.id.btnShuffle)

        // Ждем когда layout будет готов для получения размеров
        puzzleContainer.post {
            createPuzzle()
        }

        btnShuffle.setOnClickListener {
            shufflePuzzle()
        }
    }

    private fun createPuzzle() {
        puzzleContainer.removeAllViews()
        puzzlePieces.clear()

        // Создаем тестовое изображение (можно заменить на свою картинку)
        val originalBitmap = createTestBitmap()

        pieceWidth = puzzleContainer.width / cols
        pieceHeight = puzzleContainer.height / rows

        val pieces = splitBitmap(originalBitmap)

        // Создаем контейнеры для строк
        for (i in 0 until rows) {
            val rowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0
                ).apply {
                    weight = 1f
                }
                orientation = LinearLayout.HORIZONTAL
            }

            for (j in 0 until cols) {
                val pieceIndex = i * cols + j
                val pieceBitmap = pieces[pieceIndex]

                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        weight = 1f
                    }
                    setImageBitmap(pieceBitmap)
                    scaleType = ImageView.ScaleType.FIT_XY
                    tag = pieceIndex // Сохраняем оригинальную позицию

                    // Добавляем возможность перетаскивания
                    setOnLongClickListener { view ->
                        val shadowBuilder = View.DragShadowBuilder(view)
                        view.startDragAndDrop(null, shadowBuilder, view, 0)
                        true
                    }

                    setOnDragListener(dragListener)
                }

                puzzlePieces.add(imageView)
                rowLayout.addView(imageView)
            }
            puzzleContainer.addView(rowLayout)
        }
    }

    private val dragListener = View.OnDragListener { view, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                view.alpha = 0.5f
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.setBackgroundColor(Color.LTGRAY)
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.setBackgroundColor(Color.TRANSPARENT)
                true
            }
            DragEvent.ACTION_DROP -> {
                view.setBackgroundColor(Color.TRANSPARENT)

                val draggedView = event.localState as ImageView
                val targetView = view as ImageView

                // Меняем местами изображения
                swapPieces(draggedView, targetView)

                // Проверяем, собран ли пазл
                checkPuzzleSolved()
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                view.alpha = 1.0f
                view.setBackgroundColor(Color.TRANSPARENT)
                true
            }
            else -> false
        }
    }

    private fun swapPieces(view1: ImageView, view2: ImageView) {
        val tempDrawable = view1.drawable
        val tempTag = view1.tag

        view1.setImageDrawable(view2.drawable)
        view1.tag = view2.tag

        view2.setImageDrawable(tempDrawable)
        view2.tag = tempTag
    }

    private fun checkPuzzleSolved() {
        var isSolved = true
        for (i in puzzlePieces.indices) {
            if (puzzlePieces[i].tag != i) {
                isSolved = false
                break
            }
        }

        if (isSolved) {
            // Пазл собран!
            android.widget.Toast.makeText(this, "Пазл собран!", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun shufflePuzzle() {
        val indices = puzzlePieces.indices.toList().shuffled()

        for (i in puzzlePieces.indices) {
            puzzlePieces[i].tag = indices[i]
            // Здесь нужно обновить изображения согласно новым позициям
            // Для простоты просто перемешаем теги
        }

        // Обновляем отображение
        updatePuzzleDisplay()
    }

    private fun updatePuzzleDisplay() {
        // В реальном приложении здесь нужно обновить изображения
        // согласно их тегам (позициям)
    }

    private fun createTestBitmap(): Bitmap {
        // Создаем простой разноцветный bitmap для теста
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val colors = listOf(
            Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.CYAN, Color.MAGENTA,
            Color.GRAY, Color.LTGRAY, Color.DKGRAY
        )

        val sectionWidth = bitmap.width / cols
        val sectionHeight = bitmap.height / rows

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val paint = android.graphics.Paint().apply {
                    color = colors[i * cols + j]
                    style = android.graphics.Paint.Style.FILL
                }
                canvas.drawRect(
                    j * sectionWidth.toFloat(),
                    i * sectionHeight.toFloat(),
                    (j + 1) * sectionWidth.toFloat(),
                    (i + 1) * sectionHeight.toFloat(),
                    paint
                )
            }
        }

        return bitmap
    }

    private fun splitBitmap(bitmap: Bitmap): List<Bitmap> {
        val pieces = mutableListOf<Bitmap>()
        val pieceWidth = bitmap.width / cols
        val pieceHeight = bitmap.height / rows

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val piece = Bitmap.createBitmap(
                    bitmap,
                    j * pieceWidth,
                    i * pieceHeight,
                    pieceWidth,
                    pieceHeight
                )
                pieces.add(piece)
            }
        }
        return pieces
    }
}