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

    var outcomeLog = listOf<Outcome>()

    fun randomDouble(): Double {
        val randomInt: Int = (1..10000).shuffled().first()
        return randomInt / 10000.0
    }

    fun probToPlayDynamite(sequenceLengthFound: Int): Double {
       val numberOfRoundsLeft = predictNumberOfRoundsLeft()

       // Need to do draws
       val baseProbability = dynamiteRemaining.toDouble() / numberOfRoundsLeft

       val a = ((-2)*Math.log(baseProbability))/RANDOM_THRESHOLD

        val probabilty = min(MAX_DYNAMTIE_PROBABILITY, (drawTally)*exp(-a*(sequenceLengthFound - RANDOM_THRESHOLD/2)))

        return probabilty
    }

    fun predictNumberOfRoundsLeft(): Int {
        val ourWinProportion = ourScore.toDouble() / (ourScore + theirScore)
        val theirWinProportion = theirScore.toDouble() / (ourScore + theirScore)
        return floor(min((1000-ourScore)/ourWinProportion, (1000-theirScore)/theirWinProportion)).toInt()
    }

    fun determineWinnerOfLastRound(outcome: Round) {
        if (outcome.p2 == Move.D) theirDynamite --
        if (outcome.p2 == Move.D && drawTally != 0) {
            opponentDrawLengthBeforeDynamite += drawTally
        }

        if (outcome.p1 == outcome.p2) {
            drawTally++
            outcomeLog += Outcome.DRAW
            return
        }

        //println(predictNumberOfRoundsLeft() + ourScore + theirScore)

        val prize = 1 + drawTally
        drawTally = 0

        val winPairs = listOf(listOf(Move.R, Move.S),
            listOf(Move.S, Move.P),
            listOf(Move.P, Move.R),
            listOf(Move.W, Move.D))

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

        //print(dynamiteRemaining.toString()+",")
        // Determine winner of last round
        if (gamestate.rounds.size > 0) determineWinnerOfLastRound(gamestate.rounds.last())
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move

        // We're playerone
        val opponentsMoves = gamestate.rounds.map { it.p2 }.toList()
        //val opponentsMoves = gamestate.rounds.flatMap { l }

        var ourMove: Move? = null
        var sequenceLength = -1

        for (lookbackDistance in min(MAX_LOOKBACK, max(opponentsMoves.size-2, 1)) downTo 1) {
            val opponentsNextMove = predictOpponentsNextMoveFromLookbackDistanceOf(lookbackDistance, opponentsMoves)
            if (opponentsNextMove == null) continue

            //println(lookbackDistance)


            ourMove = when (opponentsNextMove) {
                Move.S -> Move.R
                Move.R -> Move.P
                Move.P -> Move.S
                Move.D -> Move.W
                else -> Move.S
            }

            if (ourMove == Move.W && theirDynamite == 0) continue
            sequenceLength = lookbackDistance
            break
        }

//        var r = randomDouble()
//
//        if (r < probToPlayDynamite(sequenceLength) && dynamiteRemaining > 0) {
//            dynamiteRemaining--
//            return Move.D
//        } else if (ourMove != null) {
//            return ourMove
//        }

        val mostCommonDrawLengthBeforeOpponentDynamite = opponentDrawLengthBeforeDynamite.average()

        if (drawTally > 0) {
            if (mostCommonDrawLengthBeforeOpponentDynamite < 1 && dynamiteRemaining > 0 && randomDouble() < 0.5) {
                dynamiteRemaining--
                return Move.D
            }
            else if (drawTally >= mostCommonDrawLengthBeforeOpponentDynamite && theirDynamite > 0) {
                return Move.W
            }
            else if (drawTally <= mostCommonDrawLengthBeforeOpponentDynamite - 1 && dynamiteRemaining > 0) {
                dynamiteRemaining--
                return Move.D
            }
        }

        if (ourMove != null) return ourMove
        return Move.S
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }
}