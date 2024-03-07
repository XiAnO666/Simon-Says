package com.example.simonsays

import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.simonsays.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sequence: MutableList<Int>
    private var sequenceIndex = 0
    private var isPlayerTurn = false
    private var score = 0 // Variable para almacenar la puntuación del jugador
    private val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

    // Declara las variables globales para SoundPool
    private lateinit var soundPool: SoundPool
    private val soundIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sequence = mutableListOf()

        // Configurar SoundPool
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        }

        // Cargar los sonidos
        soundIds.add(soundPool.load(this, R.raw.red_sound, 1))
        soundIds.add(soundPool.load(this, R.raw.green_sound, 1))
        soundIds.add(soundPool.load(this, R.raw.blue_sound, 1))
        soundIds.add(soundPool.load(this, R.raw.yellow_sound, 1))
        soundIds.add(soundPool.load(this, R.raw.game_over_sound, 1))

        // Configurar OnClickListener para el botón de inicio
        binding.startButton.setOnClickListener {
            startGame()
        }

        // Configurar OnClickListener para las imágenes
        binding.imageView1.setOnClickListener { onImageViewClicked(it) }
        binding.imageView2.setOnClickListener { onImageViewClicked(it) }
        binding.imageView3.setOnClickListener { onImageViewClicked(it) }
        binding.imageView4.setOnClickListener { onImageViewClicked(it) }
    }

    private fun startGame() {
        sequence.clear()
        sequence.add((1..4).random())
        sequenceIndex = 0
        isPlayerTurn = false

        // Mostrar un Toast antes de mostrar la secuencia
        Toast.makeText(this, "Are you ready?", Toast.LENGTH_SHORT).show()

        // Mostrar la secuencia en el fondo de la aplicación
        showSequence()
    }

    private fun showSequence() {
        val handler = Handler()
        var delay = 0L

        for ((index, number) in sequence.withIndex()) {
            handler.postDelayed({
                window.decorView.setBackgroundColor(colors[number - 1])
                Log.d("SimonSays", "Simon says: Color $number")

                // Reproducir el sonido correspondiente al color
                soundPool.play(soundIds[number - 1], 1f, 1f, 1, 0, 1f)
            }, delay)

            // Si no es el último color en la secuencia, agregar un retraso adicional
            if (index < sequence.size - 1) {
                delay += 600 // 600 ms de retraso antes del próximo color
                handler.postDelayed({
                    // Restaurar el fondo blanco antes de mostrar el próximo color
                    window.decorView.setBackgroundColor(Color.WHITE)
                }, delay)
            }

            // Incrementar el delay para el siguiente color
            delay += 1000 // Aumentar el retraso a 1 segundo entre cada color
        }

        // Agregar un delay adicional después de la secuencia completa antes de habilitar el turno del jugador
        handler.postDelayed({
            // Restaurar el fondo blanco antes de habilitar el turno del jugador
            window.decorView.setBackgroundColor(Color.WHITE)
            enablePlayerTurn()
        }, delay) // No necesitamos agregar tiempo extra después de la secuencia completa
    }

    private fun enablePlayerTurn() {
        isPlayerTurn = true
        sequenceIndex = 0
    }

    fun onImageViewClicked(view: View) {
        if (!isPlayerTurn) return

        val tappedIndex = when (view.id) {
            R.id.imageView1 -> 1
            R.id.imageView2 -> 2
            R.id.imageView3 -> 3
            R.id.imageView4 -> 4
            else -> return
        }

        Log.d("SimonSays", "Player tapped: Color $tappedIndex")

        // Reproducir el sonido correspondiente al color que pulsa el jugador
        soundPool.play(soundIds[tappedIndex - 1], 1f, 1f, 1, 0, 1f)

        if (tappedIndex == sequence[sequenceIndex]) {
            sequenceIndex++
            if (sequenceIndex == sequence.size) {
                // El jugador ha completado correctamente la secuencia actual
                // Añadir un nuevo color a la secuencia
                val newColor = (1..4).random()
                sequence.add(newColor)
                Log.d("SimonSays", "New color added: Color $newColor")
                // Aumentar la puntuación del jugador
                score++
                updateScore()
                // Mostrar la siguiente secuencia
                showSequence()
            }
        } else {
            // El jugador se equivocó, fin del juego
            Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show()
            soundPool.play(soundIds[soundIds.size - 1], 1f, 1f, 1, 0, 1f) // Reproducir el sonido de "game over"
            // Reiniciar el juego
            startGame()
        }
    }
    private fun updateScore() {
        binding.textView.text = "Score: $score"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar los recursos de SoundPool
        soundPool.release()
    }
}

