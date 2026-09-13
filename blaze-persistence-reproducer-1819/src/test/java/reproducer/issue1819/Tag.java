package reproducer.issue1819;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Tag {

    @Column(name = "tag_key")
    private String key;

    @Column(name = "tag_value")
    private String value;

    protected Tag() {
    }

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) other;
        return Objects.equals(key, tag.key) && Objects.equals(value, tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
