package com.k2sol.kinetix;

import com.k2sol.kinetix.translator.Translator;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
*   WordCounter keeps track of words added.
*   <p>All word are converted into English using <class>{@link Translator}
*
 *  <p>Any non alphabetic words are filtered out and not taken into account
 *  <p>For any non alphabetic word, Look up for a word would return 0
 *  <p>For any non English word, Look up returns the count of English Word
*
*/
public class WordCounter {

    private Translator translator;

    private Pattern alphabeticPattern;

    private ConcurrentMap<String, AtomicLong> wordCountMap = new ConcurrentHashMap<>();

    public WordCounter(Translator translator) {
        this.translator = translator;
        alphabeticPattern = Pattern.compile("^[a-zA-Z]+$");
    }

    public void addWords(List<String> words) {
        words.stream().
                filter(word -> alphabeticPattern.matcher(word).matches()).
                map(word -> translator.translate(word)).forEach(this::addWord);
    }

    private void addWord(String word) {
        AtomicLong previousValue = wordCountMap.putIfAbsent(word.toUpperCase(), new AtomicLong(1));
        if(previousValue != null) {
            previousValue.addAndGet(1);
        }
    }

    public long getWordCount(String word) {
        return alphabeticPattern.matcher(word).matches() ?
                wordCountMap.getOrDefault(translator.translate(word).toUpperCase(), new AtomicLong(0)).get() : 0;
    }
}
