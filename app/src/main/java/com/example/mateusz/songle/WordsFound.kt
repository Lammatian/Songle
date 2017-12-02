package com.example.mateusz.songle

import java.util.*
import kotlin.collections.HashMap

/**
 * Created by mateusz on 02/12/17.
 */

enum class ViewType {
    LIST,
    COUNT
}

enum class WordValue {
    UNCLASSIFIED,
    BORING,
    NOTBORING,
    INTERESTING,
    VERYINTERESTING
}

data class Word(val word: String, val interest: WordValue)

class WordsFound(lyrics: ArrayList<ArrayList<String>>, wordsInGame: HashMap<IntArray, Word>) {

    // TODO: Word 'interestingness' as enum
    private val wordsInGame = wordsInGame
    private var wordsList: ArrayList<ArrayList<String>> = ArrayList(lyrics.size)
    private var wordsCount: HashMap<WordValue, HashMap<String, Int>> = hashMapOf(
            WordValue.VERYINTERESTING to hashMapOf(),
            WordValue.INTERESTING to hashMapOf(),
            WordValue.NOTBORING to hashMapOf(),
            WordValue.BORING to hashMapOf(),
            WordValue.UNCLASSIFIED to hashMapOf()
    )

    // Constructor
    init {
        for (i in 0 until lyrics.size) {
            wordsList[i] = ArrayList(lyrics[i].size)
        }
    }

    // Add a word both to the list and the count
    fun addWord(place: IntArray) {
        val newWord = wordsInGame[place]

        wordsList[place[0]][place[1]] = newWord!!.word

        // Check if word already appeared or not
        if (wordsCount[newWord.interest]!!.containsKey(newWord.word))
            wordsCount[newWord.interest]!![newWord.word] = wordsCount[newWord.interest]!![newWord.word]!! + 1
        else
            wordsCount[newWord.interest]!![newWord.word] = 1
    }

    // Add whole line
    fun addLine(line: ArrayList<String>, number: Int) {
        // TODO: Implement
    }

    // Get the string representing given type of words
    // TODO: Sort wordsCount by interest
    // TODO: Better error handling
    fun getWords(type: ViewType): String {
        // As list
        if (type == ViewType.LIST) {
            var result = ""

            // Parse each line replacing each series of non-found words with an underscore
            for (line in wordsList) {
                var underscorePlaced = false

                if (line[0] == "") {
                    result += "_ "
                    underscorePlaced = true
                }
                else
                    result += line[0] + " "

                for (i in 1..line.size) {
                    result += when {
                        line[i] == "" && !underscorePlaced -> "_ "
                        line[i] == ""                      -> ""
                        else                               -> line[i] + " "
                    }

                    // Check if underscore was placed recently
                    underscorePlaced = result[result.length-2] == '_'
                }
            }

            return result
        }
        // As count
        else if (type == ViewType.COUNT) {
            // For nice result
            val keyToString = hashMapOf(
                    WordValue.UNCLASSIFIED to "UNCLASSIFIED",
                    WordValue.BORING to "BORING",
                    WordValue.NOTBORING to "NOT BORING",
                    WordValue.INTERESTING to "INTERESTING",
                    WordValue.VERYINTERESTING to "VERY INTERESTING"
            )
            var result = ""

            // For each WordValue get all words
            for ((key, value) in wordsCount) {
                // If no words of that value, ignore
                if (value.size == 0)
                    continue

                // Print the value and then all words and their counts
                result += keyToString[key] + "\n"
                for ((word, count) in value) {
                    result += word + " x" + count.toString() + "\n"
                }
            }

            return result
        }
        else {
            return "Error"
        }
    }
}