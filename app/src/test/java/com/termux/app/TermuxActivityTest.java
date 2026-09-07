package io.stardew.app;

import io.stardew.shared.termux.data.TermuxUrlUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;

public class TermuxActivityTest {

    private void assertUrlsAre(String text, String... urls) {
        LinkedHashSet<String> expected = new LinkedHashSet<>();
        Collections.addAll(expected, urls);
        Assert.assertEquals(expected, TermuxUrlUtils.extractUrls(text));
    }

    @Test
    public void testExtractUrls() {
        assertUrlsAre("hello http://example.io_stardew world", "http://example.io_stardew");

        assertUrlsAre("http://example.io_stardew\nhttp://another.io_stardew", "http://example.io_stardew", "http://another.io_stardew");

        assertUrlsAre("hello http://example.io_stardew world and http://more.example.io_stardew with secure https://more.example.io_stardew",
            "http://example.io_stardew", "http://more.example.io_stardew", "https://more.example.io_stardew");

        assertUrlsAre("hello https://example.io_stardew/#bar https://example.io_stardew/foo#bar",
            "https://example.io_stardew/#bar", "https://example.io_stardew/foo#bar");
    }

}
