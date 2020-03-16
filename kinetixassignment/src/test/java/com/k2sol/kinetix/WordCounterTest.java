package com.k2sol.kinetix;

import com.k2sol.kinetix.translator.Translator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.LongStream;


public class WordCounterTest {

    private Translator translator = Mockito.mock(Translator.class);

    private WordCounter wordCounter;

    @Before
    public void setUp() {
        wordCounter = new WordCounter(translator);
        Mockito.when(translator.translate("TEST")).thenReturn("TEST");
        Mockito.when(translator.translate("test")).thenReturn("test");

        Mockito.when(translator.translate("flower")).thenReturn("flower");
        Mockito.when(translator.translate("flor")).thenReturn("flower");
        Mockito.when(translator.translate("blume")).thenReturn("flower");

    }

    @Test
    public void getCountReturnsZero_WhenNoWordsAdded() {
        Assert.assertEquals(0, wordCounter.getWordCount("TEST"));
    }


    @Test
    public void getCountReturnsZero_WhenInValidWordIsAdded() {
        wordCounter.addWords(Collections.singletonList("TEST-"));
        Assert.assertEquals(0, wordCounter.getWordCount("TEST-"));
    }


    @Test
    public void getCountReturnsOne_WhenAWordIsAdded() {
        wordCounter.addWords(Collections.singletonList("TEST"));
        Assert.assertEquals(1, wordCounter.getWordCount("TEST"));
    }

    @Test
    public void getCountReturnsOne_WhenWordAddedAndWordLookedForDifferInCase() {
        wordCounter.addWords(Collections.singletonList("test"));
        Assert.assertEquals(1, wordCounter.getWordCount("TEST"));
    }


    @Test
    public void getCountReturnsTwo_WhenAWordIsAddedTwice() {
        wordCounter.addWords(Arrays.asList("TEST", "TEST"));
        Assert.assertEquals(2, wordCounter.getWordCount("TEST"));
    }


    @Test
    public void getCountReturnsThree_WhenAWordIsAddedThriceInDifferentLanguages() {
        wordCounter.addWords(Arrays.asList("flower", "flor", "blume"));
        Assert.assertEquals(3, wordCounter.getWordCount("flower"));
    }

    @Test
    public void getCountReturnsOne_WhenAWordIsAddedInOneLanguagesAndCountRetrievedUsingNonEnglishLanguage() {
        wordCounter.addWords(Collections.singletonList("flower"));
        Assert.assertEquals(1, wordCounter.getWordCount("flor"));
    }

    @Test
    public void getCountReturnsCorrectValue_WhenWordsAddedFromDifferentThreads() throws InterruptedException {

        CountDownLatch countDownLatch  = new CountDownLatch(10);

        IntStream.rangeClosed(1, 10).parallel().forEach(value ->
                new Thread((new WordAdderRunnable(countDownLatch, wordCounter, Collections.singletonList("flower"), 100))).start());

        countDownLatch.await();
        Assert.assertEquals(1000, wordCounter.getWordCount("flor"));
    }


    private static class WordAdderRunnable implements Runnable {

        private CountDownLatch countDownLatch;
        private WordCounter wordCounter;
        private final List<String> words;
        private final long noOfTimes;

        WordAdderRunnable(CountDownLatch countDownLatch, WordCounter wordCounter, List<String> words, long noOfTimes) {
            this.countDownLatch = countDownLatch;
            this.wordCounter = wordCounter;
            this.words = words;
            this.noOfTimes = noOfTimes;
        }

        @Override
        public void run() {
            LongStream.rangeClosed(1, noOfTimes).forEach(value -> wordCounter.addWords(words));
            countDownLatch.countDown();
        }
    }
}