package io.jenkins.plugins.emoji.symbols;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Emojis Tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmojisTest {

    /**
     * Test count of entries in 'emojis.list' vs. existing SVG files.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    @Order(0)
    void testSVG() throws Exception {
        File emojiList = new File("./src/main/resources/io/jenkins/plugins/emoji/symbols/Emojis/emojis.list");
        File svgFolder = new File("./src/main/resources/images/symbols");

        String[] folderContents = svgFolder.list();
        assertThat(folderContents, notNullValue());
        assertThat(folderContents, not(emptyArray()));
        assertThat(folderContents, arrayWithSize(Emojis.getAvailableIcons().size()));
        assertThat(folderContents, arrayWithSize(Emojis.getAvailableEmojis().size()));

        List<String> entries = Files.readAllLines(emojiList.toPath(), StandardCharsets.UTF_8);
        assertThat(entries, hasSize(folderContents.length));

        for (String svg : folderContents) {
            String emoji = Emojis.getAvailableIcons()
                    .get(svg.replaceFirst("emoji_", "").replaceFirst(".svg", ""));
            assertThat(emoji, notNullValue());
        }

        for (String entry : entries) {
            String[] content = entry.split(":");
            assertThat(content, arrayWithSize(2));

            assertThat(content[0], matchesPattern("^[a-z0-9_]+$"));

            String iconClassName = Emojis.getAvailableIcons().get(content[0]);
            assertThat(iconClassName, notNullValue());
            assertThat(iconClassName, is(Emojis.getIconClassName(content[0])));

            String emoji = Emojis.getAvailableEmojis().get(content[0]);
            assertThat(emoji, notNullValue());
            assertThat(emoji, is(content[1]));

            String svg = Files.readString(
                    new File(svgFolder, "emoji_" + content[0] + ".svg").toPath(), StandardCharsets.UTF_8);
            assertThat(svg, containsString(content[1]));
        }
    }

    /**
     * Test missing 'emojis.list'.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    @Order(1)
    void testMissingResource() throws Exception {
        File emojiList = new File("./target/classes/io/jenkins/plugins/emoji/symbols/Emojis/emojis.list");
        File backup = new File("./target/classes/io/jenkins/plugins/emoji/symbols/Emojis/emojis.list.backup");

        try {
            assertThat(emojiList.renameTo(backup), is(true));

            validateEmojisInstance();
        } finally {
            assertThat(backup.renameTo(emojiList), is(true));
        }
    }

    /**
     * Test invalid 'emojis.list'.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    @Order(2)
    void testInvalidResource() throws Exception {
        File emojiList = new File("./target/classes/io/jenkins/plugins/emoji/symbols/Emojis/emojis.list");
        File backup = new File("./target/classes/io/jenkins/plugins/emoji/symbols/Emojis/emojis.list.backup");

        try {
            assertThat(emojiList.renameTo(backup), is(true));
            assertThat(emojiList.createNewFile(), is(true));
            Files.writeString(emojiList.toPath(), "invalid", StandardCharsets.UTF_8);

            validateEmojisInstance();
        } finally {
            assertThat(emojiList.delete(), is(true));
            assertThat(backup.renameTo(emojiList), is(true));
        }
    }

    private static void validateEmojisInstance() throws Exception {
        Constructor<Emojis> ctor = Emojis.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Emojis emojis = ctor.newInstance();

        Field availableIconsField = emojis.getClass().getDeclaredField("availableIcons");
        availableIconsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> availableIcons = (Map<String, String>) availableIconsField.get(emojis);

        assertThat(availableIcons, notNullValue());
        assertThat(availableIcons, anEmptyMap());

        Field availableEmojisField = emojis.getClass().getDeclaredField("availableEmojis");
        availableEmojisField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> availableEmojis = (Map<String, String>) availableEmojisField.get(emojis);

        assertThat(availableEmojis, notNullValue());
        assertThat(availableEmojis, anEmptyMap());
    }
}
