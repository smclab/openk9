/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.tika.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * A utility class for cleaning raw text by removing unwanted HTML tags, correcting word breaks,
 * removing unnecessary characters, and normalizing whitespace.
 */
public class TextCleaner {

    /**
     * Cleans the raw text by performing the following operations:
     * 1. Decodes HTML entities and removes HTML tags.
     * 2. Corrects word breaks that are split across lines.
     * 3. Removes sequences of long dots (more than 3 dots).
     * 4. Removes unwanted characters while keeping numbers and certain symbols like slashes.
     * 5. Normalizes whitespace by replacing multiple spaces, tabs, or newlines with a single space.
     *
     * If an exception occurs during the cleaning process, the method will return the original raw text.
     *
     * @param rawText The raw input text to be cleaned.
     * @return The cleaned text after applying the necessary transformations, or the original raw text if an error occurs.
     */
    public static String cleanText(String rawText) {
        // Check if the input is null, return empty string if true
        if (rawText == null) {
            return "";
        }

        try {
            // 1. Decode HTML entities and remove HTML tags using Jsoup
            String cleanedText = Jsoup.clean(rawText, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));

            // 2. Correct word breaks like "Organiz- \nzation" -> "Organization".
            cleanedText = cleanedText.replaceAll("(\\w+)-\\s*\\n(\\w+)", "$1$2");

            // 3. Remove sequences of long dots (more than 3) like "...........".
            cleanedText = cleanedText.replaceAll("\\.{4,}", "");

            // 4. Remove unwanted characters while keeping numbers with / (e.g., 3/27).
            cleanedText = cleanedText.replaceAll("[^a-zA-Z0-9 .,;:?!()/%-]|(?<!\\d)/|/(?!\\d)", "");

            // 5. Normalize whitespace by replacing newlines or multiple spaces with a single space, also removing tabs.
            cleanedText = cleanedText.replaceAll("[ \t]+", " ").replaceAll("\\n+", " ");

            return cleanedText.trim();
        } catch (Exception e) {
            // In case of an exception, return the raw text without any cleaning
            return rawText; // Return the original raw text
        }
    }

}

