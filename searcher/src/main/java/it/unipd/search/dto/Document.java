/**
 * Package containing domain classes used to represent documents
 * exchanged between services and persisted in the system.
 */
package it.unipd.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents a document with identifying information, metadata,
 * and textual content.
 *
 * <p>A {@code Document} instance encapsulates:</p>
 * <ul>
 *   <li>a unique identifier</li>
 *   <li>a source URL</li>
 *   <li>a title</li>
 *   <li>the main textual content</li>
 *   <li>inferred topics or tags</li>
 * </ul>
 *
 * <p>Equality and hash code are defined based on the {@code id} and
 * {@code title} fields, ensuring consistent behavior when instances
 * are used in collections such as {@link java.util.HashSet} or as
 * keys in {@link java.util.HashMap}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    /**
     * Unique identifier of the document.
     */
    @JsonProperty("id")
    private String id;

    /**
     * URL pointing to the source or original location of the document.
     */
    @JsonProperty("url")
    private String url;

    /**
     * Title of the document.
     */
    @JsonProperty("title")
    private String title;

    /**
     * Main textual content of the document.
     */
    @JsonProperty("main_content")
    private String main_content;

    /**
     * Inferred topic associated with the document.
     */
    @JsonProperty("topic")
    private String topic;

    /**
     * Default constructor required for JSON deserialization.
     */
    public Document() {
    }

    /**
     * Constructs a new {@code Document} instance with all attributes.
     *
     * @param id           the unique identifier of the document
     * @param url          the URL associated with the document
     * @param title        the title of the document
     * @param main_content the main textual content of the document
     * @param topic        inferred topic related to the document
     */
    public Document(String id, String url, String title, String main_content, String topic) {
        this.id = id;
        Objects.requireNonNull(url);
        this.url = url;
        Objects.requireNonNull(title);
        this.title = title;
        this.main_content = main_content;
        this.topic = topic;
    }

    /**
     * Returns the unique identifier of the document.
     *
     * @return the document identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the document.
     *
     * @param newId the identifier to assign
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Returns the URL associated with the document.
     *
     * @return the document URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the document.
     *
     * @param newUrl the URL to assign
     */
    public void setUrl(String newUrl) {
        this.url = newUrl;
    }

    /**
     * Returns the title of the document.
     *
     * @return the document title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the document.
     *
     * @param newTitle the title to assign
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Returns the main textual content of the document.
     *
     * @return the document content
     */
    public String getMain_content() {
        return main_content;
    }

    /**
     * Sets the main textual content of the document.
     *
     * @param newContent the content to assign
     */
    public void setMain_content(String newContent) {
        this.main_content = newContent;
    }

    /**
     * Returns the inferred topic associated with the document.
     *
     * @return the document topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Indicates whether this document is equal to another object.
     *
     * <p>Two {@code Document} instances are considered equal if they
     * have the same {@code id} and {@code title}.</p>
     *
     * @param obj the object to compare with this instance
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Document doc = (Document) obj;
        return Objects.equals(id, doc.id) && Objects.equals(title, doc.title);
    }

    /**
     * Returns a hash code value for the document.
     *
     * <p>The hash code is computed using the {@code id} and {@code title}
     * fields, ensuring consistency with {@link #equals(Object)}.</p>
     *
     * @return a hash code value for this document
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    /**
     * Returns a human-readable string representation of the document.
     *
     * @return string representation of the document
     */
    @Override
    public String toString() {
        return "Document: id " + this.id +
                " title " + this.title +
                " url " + this.url +
                " topic " + this.topic + "\n";
    }
}
