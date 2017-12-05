package com.example.mateusz.songle

import android.text.Html
import android.text.Spanned
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    var numberOfWordsInGame: Int = wordsInGame.size
    var numberOfWordsFound: Int = 0

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

        numberOfWordsFound += 1
    }

    // Add whole line
    fun addLine(line: ArrayList<String>, number: Int) {
        // TODO: Implement
    }

    // Get the string representing given type of words
    // TODO: Sort wordsCount by interest
    // TODO: Better error handling
    // TODO: In list show only lines with words?
    fun getWords(type: ViewType): Spanned {
        // As list
        if (type == ViewType.List) {
            var result = ""

            // Parse each line replacing each series of non-found words with an underscore
            for (i in 0 until wordsList.size) {
                result += (i+1).toString() + ". "
                val line = wordsList[i]
                var underscorePlaced = false

                if (line[0] == "") {
                    result += "_ "
                    underscorePlaced = true
                }
                else
                    result += line[0] + " "

                for (j in 1 until line.size) {
                    result += when {
                        line[j] == "" && !underscorePlaced -> "_ "
                        line[j] == ""                      -> ""
                        else                               -> line[j] + " "
                    }

                    // Check if underscore was placed recently
                    underscorePlaced = result[result.length-2] == '_'
                }

                result += "<br>"
            }

            return Html.fromHtml(result)
        }
        // As count
        else if (type == ViewType.Count) {
            // For nice result
            var result = ""
            val valueToColor = hashMapOf(
                    WordValue.Unclassified to "<font color='#757575'>Unclassified</font>",
                    WordValue.Boring to "<font color='#388E3C'>Boring</font>",
                    WordValue.Notboring to "<font color='#FFC107'>Not Boring</font>",
                    WordValue.Interesting to "<font color='#FF9800'>Interesting</font>",
                    WordValue.Veryinteresting to "<font color='#AA0000'>Very Interesting</font>"
            )

            // For each WordValue get all words
            for ((key, value) in wordsCount) {
                // If no words of that value, ignore
                if (value.size == 0)
                    continue

                // Print the value and then all words and their counts
                result += valueToColor[key] + "<br>"
                for ((word, count) in value) {
                    result += word + " x" + count.toString() + "<br>"
                }
            }

            return Html.fromHtml(result)
        }
        else {
            return Html.fromHtml("Error")
        }
    }
}