package com.example.puzzles

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var puzzleContainer: LinearLayout
    private lateinit var btnShuffle: Button
    private lateinit var btnNextPuzzle: Button
    private var puzzlePieces = mutableListOf<ImageView>()
    private val rows = 4
    private val cols = 4

    // Список доступных картинок для пазлов
    private val imageResources = listOf(
        R.drawable.puzzle_image11,
        R.drawable.puzzle_image123
        // Добавь здесь другие картинки
    )

    private var currentPuzzleIndex = 0

    // Храним выбранный кусочек для перемещения
    private var selectedPiece: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        puzzleContainer = findViewById(R.id.puzzleContainer)
        btnShuffle = findViewById(R.id.btnShuffle)
        btnNextPuzzle = findViewById(R.id.Новый пазл)

        // Создаем первый пазл
        createPuzzle()

        btnShuffle.setOnClickListener {
            shufflePuzzle()
        }

        btnNextPuzzle.setOnClickListener {
            nextPuzzle()
        }
    }

    private fun createPuzzle() {
        puzzleContainer.removeAllViews()
        puzzlePieces.clear()

        val originalBitmap = createTestBitmap()

        // Создаем строки и столбцы для пазла
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
                val pieceBitmap = splitBitmap(originalBitmap, i, j)

                val imageView = createPuzzlePiece(pieceBitmap, pieceIndex)
                puzzlePieces.add(imageView)
                rowLayout.addView(imageView)
            }
            puzzleContainer.addView(rowLayout)
        }

        // Обновляем текст кнопки
        updateNextButtonText()
    }

    private fun createPuzzlePiece(bitmap: Bitmap, index: Int): ImageView {
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                weight = 1f
            }
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_XY
            tag = index

            // Добавляем отступы чтобы видеть границы
            setPadding(4, 4, 4, 4)
            setBackgroundColor(Color.DKGRAY)

            // Простой клик для выбора и перемещения
            setOnClickListener {
                onPieceClicked(this)
            }
        }
    }

    private fun onPieceClicked(clickedPiece: ImageView) {
        if (selectedPiece == null) {
            // Первый клик - выбираем кусочек (без уведомления)
            selectedPiece = clickedPiece
            clickedPiece.setBackgroundColor(Color.RED)
        } else {
            // Второй клик - меняем местами
            if (selectedPiece != clickedPiece) {
                swapPieces(selectedPiece!!, clickedPiece)
                selectedPiece?.setBackgroundColor(Color.DKGRAY)
                selectedPiece = null
                checkPuzzleSolved()
            } else {
                // Клик на тот же кусочек - отмена выбора
                selectedPiece?.setBackgroundColor(Color.DKGRAY)
                selectedPiece = null
            }
        }
    }

    private fun swapPieces(piece1: ImageView, piece2: ImageView) {
        val tempDrawable = piece1.drawable
        val tempTag = piece1.tag

        piece1.setImageDrawable(piece2.drawable)
        piece1.tag = piece2.tag

        piece2.setImageDrawable(tempDrawable)
        piece2.tag = tempTag
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
            Toast.makeText(this, "Пазл успешно собран!", Toast.LENGTH_LONG).show()
        }
    }

    private fun shufflePuzzle() {
        // Просто меняем местами случайные кусочки 20 раз
        repeat(20) {
            val randomIndex1 = (0 until puzzlePieces.size).random()
            val randomIndex2 = (0 until puzzlePieces.size).random()
            if (randomIndex1 != randomIndex2) {
                swapPieces(puzzlePieces[randomIndex1], puzzlePieces[randomIndex2])
            }
        }
        Toast.makeText(this, "Пазл перемешан!", Toast.LENGTH_SHORT).show()
    }

    private fun nextPuzzle() {
        // Переходим к следующему пазлу
        currentPuzzleIndex = (currentPuzzleIndex + 1) % imageResources.size
        createPuzzle()
        Toast.makeText(this, "Новый пазл загружен!", Toast.LENGTH_SHORT).show()
    }

    private fun updateNextButtonText() {
        val totalPuzzles = imageResources.size
        val currentNumber = currentPuzzleIndex + 1
        btnNextPuzzle.text = "Следующий пазл ($currentNumber/$totalPuzzles)"
    }

    private fun createTestBitmap(): Bitmap {
        // Берем текущую картинку из списка
        val currentResId = imageResources[currentPuzzleIndex]

        try {
            val bitmap = BitmapFactory.decodeResource(resources, currentResId)
            if (bitmap != null) {
                // Масштабируем до квадрата 600x600 для пазла
                return Bitmap.createScaledBitmap(bitmap, 600, 600, true)
            }
        } catch (e: Exception) {
            // Если картинка не найдена, переходим к следующей
            currentPuzzleIndex = (currentPuzzleIndex + 1) % imageResources.size
            if (currentPuzzleIndex != 0) { // Чтобы избежать бесконечного цикла
                return createTestBitmap()
            }
        }

        // Если картинок нет - создаем разноцветный тест
        return createColorfulTestBitmap()
    }

    private fun createColorfulTestBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Твой старый код с цветами как запасной вариант
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
                    color = colors[(i * cols + j) % colors.size]
                    style = android.graphics.Paint.Style.FILL
                }

                canvas.drawRect(
                    j * sectionWidth.toFloat() + 5,
                    i * sectionHeight.toFloat() + 5,
                    (j + 1) * sectionWidth.toFloat() - 5,
                    (i + 1) * sectionHeight.toFloat() - 5,
                    paint
                )

                val textPaint = android.graphics.Paint().apply {
                    color = Color.BLACK
                    textSize = 60f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val number = (i * cols + j + 1).toString()
                canvas.drawText(
                    number,
                    (j * sectionWidth + sectionWidth / 2).toFloat(),
                    (i * sectionHeight + sectionHeight / 2).toFloat() + 20,
                    textPaint
                )
            }
        }

        return bitmap
    }

    private fun splitBitmap(bitmap: Bitmap, row: Int, col: Int): Bitmap {
        val pieceWidth = bitmap.width / cols
        val pieceHeight = bitmap.height / rows

        return Bitmap.createBitmap(
            bitmap,
            col * pieceWidth,
            row * pieceHeight,
            pieceWidth,
            pieceHeight
        )
    }
}