package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import com.softwire.dynamite.game.Round
import java.lang.Exception
import java.lang.Integer.*
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.min
import kotlin.collections.average


class MyBot : Bot {

    enum class Outcome {
        WIN,
        LOSE,
        DRAW
    }
    val MAX_LOOKBACK = 20
    val RANDOM_THRESHOLD = 12
    var dynamiteRemaining = 100
    val MAX_DYNAMTIE_PROBABILITY = 1.0
    val DYNAMITE_DRAW_OFFSET = 0.1
    val DYNAMITE_DRAW_SCALING = 0.1
    var ourScore = 0
    var theirScore = 0
    var drawTally =  0
    var theirDynamite = 100

    var opponentDrawLengthBeforeDynamite = listOf<Int>()
    var drawLengthCount = List(1000) {0}.toMutableList()
    var opponentDynamiteByDrawLengthCount = List(1000) {0}.toMutableList()
    var opponentWaterByDrawLengthCount = List(1000) {0}.toMutableList()
    var outcomeLog = listOf<Outcome>()

    private fun randomDouble(): Double {
        val randomInt: Int = (1..10000).shuffled().first()
        return randomInt / 10000.0
    }

    private fun probToPlayDynamite(): Double {
       val numberOfRoundsLeft = predictNumberOfRoundsLeft()
       val baseProbability = dynamiteRemaining.toDouble() / numberOfRoundsLeft
        return baseProbability
    }

    private fun predictNumberOfRoundsLeft(): Int {
        val ourWinProportion = ourScore.toDouble() / (ourScore + theirScore)
        val theirWinProportion = theirScore.toDouble() / (ourScore + theirScore)
        return floor(min((1000-ourScore)/ourWinProportion, (1000-theirScore)/theirWinProportion)).toInt()
    }

    private fun determineWinnerOfLastRound(outcome: Round) {
        if (outcome.p2 == Move.D) theirDynamite --

        if (outcome.p1 == outcome.p2) {
            drawTally++
            outcomeLog += Outcome.DRAW
            return
        }

        if (drawTally > 0) {
            drawLengthCount[drawTally - 1] += 1

            when (outcome.p2) {
                Move.D -> {
                    opponentDrawLengthBeforeDynamite += drawTally
                    opponentDynamiteByDrawLengthCount[drawTally-1] += 1
                }

                Move.W -> opponentWaterByDrawLengthCount[drawTally-1] += 1
            }
        }

        val prize = 1 + drawTally
        drawTally = 0

        val winPairs = listOf(
            listOf(Move.R, Move.S),
            listOf(Move.S, Move.P),
            listOf(Move.P, Move.R),
            listOf(Move.W, Move.D)
        )

        for (winPair in winPairs) {
            if (outcome.p1 == winPair[0] && outcome.p2 == winPair[1]) {
                ourScore += prize
                outcomeLog += Outcome.WIN
                return
            } else if (outcome.p2 == winPair[0] && outcome.p1 == winPair[1]) {
                theirScore += prize
                outcomeLog += Outcome.LOSE
                return
            }
        }

        if (outcome.p1 == Move.W) {
            theirScore += prize
            outcomeLog += Outcome.LOSE
        } else if (outcome.p2 == Move.W) {
            ourScore += prize
            outcomeLog += Outcome.WIN
        } else if (outcome.p1 == Move.D) {
            ourScore += prize
            outcomeLog += Outcome.WIN
        } else if (outcome.p2 == Move.D) {
            theirScore += prize
            outcomeLog += Outcome.LOSE
        } else {
            throw Exception("We were wrong")
        }
    }

    private fun randomMove(): Move {
        return listOf(Move.R, Move.P, Move.S).shuffled().first()

        if (randomDouble() < probToPlayDynamite()) return Move.D

        val r = randomDouble()
        if (r < 0.33) return Move.S
        if (r < 0.66) return Move.R

        return Move.P
    }

    private fun predictOpponentsNextMoveFromLookbackDistanceOf(lookbackDistance: Int, opponentsMoves: List<Move>): Move? {
        if (opponentsMoves.size > lookbackDistance) {
            val mostRecentMoves = opponentsMoves.takeLast(lookbackDistance)
            // Look for mostRecentMoves in opponentsMoves
            for (i in 0..opponentsMoves.size-(lookbackDistance+1)) {
                var matchFound = true
                for (offset in 0 until lookbackDistance) {
                    if (opponentsMoves[i+offset] != mostRecentMoves[offset]) matchFound =false
                }
                if (!matchFound) continue
                return opponentsMoves[i+lookbackDistance]
            }
        }
        return null
    }

    private fun determineMoveByIncreasingSequenceLookback(opponentsMoves: List<Move>): Move? {
        for (lookbackDistance in min(MAX_LOOKBACK, max(opponentsMoves.size-2, 1)) downTo 1) {
            val opponentsNextMove = predictOpponentsNextMoveFromLookbackDistanceOf(lookbackDistance, opponentsMoves)

            val ourMove = when (opponentsNextMove) {
                Move.S -> Move.R
                Move.R -> Move.P
                Move.P -> Move.S
                Move.D -> Move.W
                else -> null
            }

            if (ourMove == Move.W && theirDynamite == 0) continue

            return ourMove
        }
        return null
    }

    private fun determineMoveByDrawTally(): Move? {
        if (dynamiteRemaining > 0 && opponentWaterByDrawLengthCount[drawTally-1]/(drawLengthCount[drawTally-1]+0.00001) < randomDouble()) {
                dynamiteRemaining--
                return Move.D
        } else if (theirDynamite > 0 && opponentDynamiteByDrawLengthCount[drawTally - 1]/(drawLengthCount[drawTally - 1]+0.00001) > randomDouble()) {
                return Move.W
        }
        return null
    }

    override fun makeMove(gamestate: Gamestate): Move {
        if (gamestate.rounds.size > 0) determineWinnerOfLastRound(gamestate.rounds.last())

        // We're player one
        val opponentsMoves = gamestate.rounds.map { it.p2 }.toList()

        var ourMove = determineMoveByIncreasingSequenceLookback(opponentsMoves)
        if (drawTally > 0 && ourMove == null) ourMove = determineMoveByDrawTally()
        if (ourMove == null) ourMove = randomMove()

        return ourMove
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }
}