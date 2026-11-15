package com.virtuatame.tuipet

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = TextView(this).apply {
            text = "TuiPet\n\nYour virtual pet lives on your home screen.\nAdd the TuiPet widget to get started."
            textSize = 20f
            setPadding(32, 48, 32, 32)
        }
        setContentView(message)
    }
}
