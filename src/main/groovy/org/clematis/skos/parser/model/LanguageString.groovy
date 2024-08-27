package org.clematis.skos.parser.model

class LanguageString {

    String value = ""
    String language = ""

    LanguageString(String value) {
        this(value, "")
    }

    LanguageString(String value, String language) {
        this.value = value != null ? value.trim() : value
        this.language = language != null ? language.trim() : language
    }

    @Override
    String toString() {
        return value + (language ? "@$language" : "")
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        LanguageString that = (LanguageString) o

        if (language != that.language) {
            return false
        }
        if (value != that.value) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (value != null ? value.hashCode() : 0)
        result = 31 * result + (language != null ? language.hashCode() : 0)
        return result
    }
}
