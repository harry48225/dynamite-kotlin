package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import kotlin.random.Random

class MyBot : Bot {
    override fun makeMove(gamestate: Gamestate): Move {
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move

        // We're playerone
        val opponentsMoves = gamestate.rounds.map { it.p2 }.toList()
        val lookbackDistance = 3

        if (opponentsMoves.size > 3) {
            val mostRecentMoves = opponentsMoves.takeLast(lookbackDistance)
            // Look for mostRecentMoves in opponentsMoves
            for (i in 0..opponentsMoves.size-(lookbackDistance+1)) {
                var matchFound = true
                for (offset in 0..(lookbackDistance-1)) {
                    if (opponentsMoves[i+offset] != mostRecentMoves[offset]) matchFound =false
                }
                if (!matchFound) continue

                val opponentsNextMove = opponentsMoves[i+lookbackDistance]

                if (opponentsNextMove == Move.S) return Move.R
                if (opponentsNextMove == Move.R) return Move.P
                if (opponentsNextMove == Move.P) return Move.S
            }
        }
        return Move.S
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }
}