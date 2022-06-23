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
        
        if (opponentsMoves.size > 3) {
            val mostRecentMoves = opponentsMoves.takeLast(3)
            // Look for mostRecentMoves in opponentsMoves
            for (i in 0..opponentsMoves.size-4) {
                if (opponentsMoves[i] == mostRecentMoves[0] && opponentsMoves[i+1] == mostRecentMoves[1] && opponentsMoves[i+2] == mostRecentMoves[2]) {
                    val opponentsNextMove = opponentsMoves[i+3]

                    if (opponentsNextMove == Move.S) return Move.R
                    if (opponentsNextMove == Move.R) return Move.P
                    if (opponentsNextMove == Move.P) return Move.S
                }
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