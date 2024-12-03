package com.example.acrhitectureapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailsActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var infoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resultText = findViewById(R.id.title)

        infoText = findViewById(R.id.info)

        // Извлечение переданного значения из Intent
        val result = intent.getStringExtra("CLASS_NAME") ?: "Нет данных"
        resultText.text = result




        infoText.text = description(result)


        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            finish()

        }
    }

    private fun description(result: String): String {
        return when (result) {
            "Архитектура Ахеменидов" -> getString(R.string.one)
            "Архитектура Древнего Египта" -> getString(R.string.two)
            "Интернациональный стиль" -> getString(R.string.three)
            "Архитектура новизны" -> getString(R.string.four)
            "Архитектура времен королевы Анны" -> getString(R.string.five)
            "Архитектура русского возрождения" -> getString(R.string.six)
            else -> "Неизвестный стиль"
        }
    }
}
