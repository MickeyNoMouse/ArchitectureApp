package com.example.acrhitectureapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var imageView: ImageView
    private lateinit var buttonLoad: Button
    private lateinit var buttonInfo: Button
    private lateinit var resultText: TextView
    private lateinit var buttonDetails: Button

    // TensorFlow Lite интерпретатор
    private lateinit var tflite: Interpreter

    // Размер входного изображения для модели
    private val imageSize = 128

    private var selectedImage: Bitmap? = null

    private var currentClass: String? = null


    // Запуск выбора изображения из галереи
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            selectedImage = bitmap
            currentClass = null // Очистим результат при выборе нового изображения
            resultText.text = "Нажмите кнопку определить архитектурный стиль"
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация элементов интерфейса
        imageView = findViewById(R.id.imageView)
        buttonLoad = findViewById(R.id.button_load)
        buttonInfo = findViewById(R.id.button_info)
        resultText = findViewById(R.id.class_result)
        buttonDetails = findViewById(R.id.button_dop_info)

        // Загрузка модели
        tflite = Interpreter(loadModelFile("model.tflite"))

        // Обработка кнопки "Загрузить фото"
        buttonLoad.setOnClickListener {

            pickImageLauncher.launch("image/*")
        }


        // Обработка кнопки "Определить класс"
        buttonInfo.setOnClickListener {
            selectedImage?.let { bitmap ->
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
                val result = classifyImage(resizedBitmap)
                currentClass = "$result"
                resultText.text = "$result"
            } ?: run {
                resultText.text = "Пожалуйста, загрузите фото!"
            }
        }
// Обработка кнопки с доп инфой
        buttonDetails.setOnClickListener {
            if (currentClass != null) {
            // Открыть DetailsActivity с передачей текущего класса
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("CLASS_NAME", currentClass)
            startActivity(intent)
        } else {
            resultText.text = "Сначала определите архитектурный стиль!"
        }
    }
}

    // Загрузка модели TensorFlow Lite
    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Преобразование Bitmap в ByteBuffer для модели
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in intValues) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    // Классификация изображения
    private fun classifyImage(bitmap: Bitmap): String {
        val byteBuffer = bitmapToByteBuffer(bitmap)

        // Выходной массив должен соответствовать форме [1, 6]
        val output = Array(1) { FloatArray(6) }
        tflite.run(byteBuffer, output)

        // Извлечение результатов из первого измерения
        val probabilities = output[0]

        // Индекс максимального значения - это предсказанный класс
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        return when (maxIndex) {
            0 -> "Архитектура Ахеменидов"
            1 -> "Архитектура Древнего Египта"
            2 -> "Интернациональный стиль"
            3 -> "Архитектура новизны"
            4 -> "Архитектура времен королевы Анны"
            5 -> "Архитектура русского возрождения"
            else -> "Неизвестный стиль"
        }
    }
}
