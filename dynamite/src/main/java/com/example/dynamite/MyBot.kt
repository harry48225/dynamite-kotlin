package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import com.softwire.dynamite.game.Round
import java.lang.Exception
import java.lang.Integer.max
import java.lang.Integer.min

class MyBot : Bot {
    val MAX_LOOKBACK = 20
    var dynamiteRemaining = 100
    var ourScore = 0
    var theirScore = 0
    var drawTally =  0

//    fun probToPlayDynamite(sequenceLengthFound: Int) {
//        val expectedRemainingNumberOfRounds = min()
//    }

    fun determineWinnerOfLastRound(outcome: Round) {
        if (outcome.p1 == outcome.p2) {
            drawTally++
            return
        }

        val prize = 1 + drawTally
        drawTally = 0

        val winPairs = listOf(listOf(Move.R, Move.S),
            listOf(Move.S, Move.P),
            listOf(Move.P, Move.R),
            listOf(Move.W, Move.D))

        for (winPair in winPairs) {
            if (outcome.p1 == winPair[0] && outcome.p2 == winPair[1]) {
                ourScore += prize
                return
            } else if (outcome.p2 == winPair[0] && outcome.p1 == winPair[1]) {
                theirScore += prize
                return
            }
        }

        if (outcome.p1 == Move.W) {
            theirScore += prize
        } else if (outcome.p2 == Move.W) {
            ourScore += prize
        } else if (outcome.p1 == Move.D) {
            ourScore += prize
        } else if (outcome.p2 == Move.D) {
            theirScore += prize
        } else {
            throw Exception("We were wrong")
        }
    }

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
        // Determine winner of last round
        if (gamestate.rounds.size > 0) determineWinnerOfLastRound(gamestate.rounds.last())
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move

        // We're playerone
        val opponentsMoves = gamestate.rounds.map { it.p2 }.toList()

        for (lookbackDistance in min(MAX_LOOKBACK, max(opponentsMoves.size-2, 1)) downTo 1) {
            val opponentsNextMove = predictOpponentsNextMoveFromLookbackDistanceOf(lookbackDistance, opponentsMoves)
            if (opponentsNextMove == null) continue

            //println(lookbackDistance)

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