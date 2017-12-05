package com.example.mateusz.songle

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by mateusz on 02/12/17.
 */

enum class ViewType {
    List,
    Count
}

enum class WordValue {
    Unclassified,
    Boring,
    Notboring,
    Interesting,
    Veryinteresting
}

data class Word(val word: String, val interest: WordValue)

class WordsFound(lyrics: List<List<String>>,
                 private val wordsInGame: HashMap<ArrayList<Int>, Word>) {

    private var wordsList: ArrayList<ArrayList<String>> = ArrayList(lyrics.size)
    private var wordsCount: HashMap<WordValue, HashMap<String, Int>> = hashMapOf(
            WordValue.Veryinteresting to hashMapOf(),
            WordValue.Interesting to hashMapOf(),
            WordValue.Notboring to hashMapOf(),
            WordValue.Boring to hashMapOf(),
            WordValue.Unclassified to hashMapOf()
    )

    // Constructor
    init {
        // TODO: Bug with wordsList size?
        wordsList = ArrayList(lyrics.size)
        for (i in 0 until lyrics.size) {
            wordsList.add(ArrayList(lyrics[i].size))
            for (j in 0 until lyrics[i].size)
                wordsList[i].add("")
        }
    }

    // Add a word both to the list and the count
    fun addWord(place: ArrayList<Int>) {
        val newWord = wordsInGame[place]

        wordsList[place[0]-1][place[1]-1] = newWord!!.word

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
    // TODO: In list show only lines with words?
    fun getWords(type: ViewType): String {
        // As list
        if (type == ViewType.List) {
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

                for (i in 1 until line.size) {
                    result += when {
                        line[i] == "" && !underscorePlaced -> "_ "
                        line[i] == ""                      -> ""
                        else                               -> line[i] + " "
                    }

                    // Check if underscore was placed recently
                    underscorePlaced = result[result.length-2] == '_'
                }

                result += "\n"
            }

            return result
        }
        // As count
        else if (type == ViewType.Count) {
            // For nice result
            var result = ""

            // For each WordValue get all words
            for ((key, value) in wordsCount) {
                // If no words of that value, ignore
                if (value.size == 0)
                    continue

                // Print the value and then all words and their counts
                result += key.name + "\n"
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