package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import java.lang.Integer.max
import java.lang.Integer.min

class MyBot : Bot {
    val MAX_LOOKBACK = 10

    fun predictOpponentsNextMoveFromLookbackDistanceOf(lookbackDistance: Int, opponentsMoves: List<Move>): Move? {
        if (opponentsMoves.size > lookbackDistance) {
            val mostRecentMoves = opponentsMoves.takeLast(lookbackDistance)
            // Look for mostRecentMoves in opponentsMoves
            for (i in 0..opponentsMoves.size-(lookbackDistance+1)) {
                var matchFound = true
                for (offset in 0..(lookbackDistance-1)) {
                    if (opponentsMoves[i+offset] != mostRecentMoves[offset]) matchFound =false
                }
                if (!matchFound) continue
                return opponentsMoves[i+lookbackDistance]
            }
        }
        return null
    }

    override fun makeMove(gamestate: Gamestate): Move {
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move

        // We're playerone
        val opponentsMoves = gamestate.rounds.map { it.p2 }.toList()

        for (lookbackDistance in min(MAX_LOOKBACK, max(opponentsMoves.size-2, 1)) downTo 1) {
            val opponentsNextMove = predictOpponentsNextMoveFromLookbackDistanceOf(lookbackDistance, opponentsMoves)
            if (opponentsNextMove == null) continue

            return when (opponentsNextMove) {
                Move.S -> Move.R
                Move.R -> Move.P
                Move.P -> Move.S
                Move.D -> Move.W
                else -> Move.S
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