import re


def clean_text(raw_text):
    """
    Cleans the input text by performing several text processing steps.

    This function removes HTML tags, corrects hyphenated words that are split across lines,
    eliminates excessive punctuation, and removes unwanted characters while preserving
    certain formats like fractions. It also normalizes whitespace by replacing multiple spaces
    or newlines with a single space.

    Parameters:
    raw_text (str): The raw input text that needs to be cleaned.

    Returns:
    str: The cleaned text with unwanted elements removed and formatting corrected.
    """
    # 1. Remove HTML tags like <br>, <p>, etc.
    cleaned_text = re.sub(r"<[^>]+>", "", raw_text)

    # 2. Correct word breaks like "Organiz- \nzation" -> "Organization"
    cleaned_text = re.sub(r"-\s*\n", "", cleaned_text)

    # 3. Remove sequences of long dots (more than 3) like "..........."
    cleaned_text = re.sub(r"\.{4,}", "", cleaned_text)

    # 4. Remove unwanted characters while keeping numbers with / (e.g., 3/27)
    cleaned_text = re.sub(
        r"[^a-zA-Z0-9\s.,;:?!()/%-]|(?<!\d)/|/(?!\d)", "", cleaned_text
    )

    # 5. Replace newlines or multiple spaces with a single space
    cleaned_text = re.sub(r"\s+", " ", cleaned_text)

    return cleaned_text.strip()
