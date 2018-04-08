package com.glodanif.spgt

import java.awt.Robot

class MouseController {

    private val robot = Robot()

    fun onMouseEvent(event: String) {

        val coordinates = event
                .split(":")
                .map { it.trim() }

        val x = coordinates[0].substringBefore(".").toInt()
        val y = coordinates[1].substringBefore(".").toInt()

        robot.mouseMove(x, y)

        System.out.print("\r[$x : $y]")
    }
}
