from app.core.firebase_config import DB
import re
import emoji

def url_exists(uid, url):
    query = DB.collection("users").document(uid).collection("urls") \
        .where("url", "==", url.strip()).limit(1)

    return any(query.stream())


def processing_content(content):
    """
    Process the content to remove special characters, emojis, and punctuation.
    
    :param content: The input content to process.
    :type content: str
    :return: The processed content.
    :rtype: str
    """

    # Convert to lowercase
    metadata = content.lower()

    # Remove likes and comments (e.g., "254k likes, 47k likes, 6,653 likes, 38 comments")
    metadata = re.sub(r"\d+(,\d+|k)? likes(, \d+(,\d+|k)? comments)? - ", "", metadata)

    # Remove author and date (e.g., "lifeflowerez on November 9, 2024")
    metadata = re.sub(r"[a-zA-Z0-9_]+ on [A-Za-z]+\s\d{1,2},\s\d{4}:", "", metadata)
    
    # Replace multiple spaces with a single space
    metadata = re.sub(r"\s+", " ", metadata)
    
    # Remove emojis using emoji library
    metadata = emoji.replace_emoji(metadata, replace="")
    
    # Strip leading and trailing whitespace
    metadata = metadata.strip()
    
    return metadata