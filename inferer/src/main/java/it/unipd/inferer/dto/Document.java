/**
 * Package containing domain classes for document management.
 */
package it.unipd.inferer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * Represents a document with identifying information, metadata, and textual content.
 * <p>
 * A {@code documents} instance encapsulates:
 * <ul>
 *   <li>a unique identifier</li>
 *   <li>a source URL</li>
 *   <li>a title</li>
 *   <li>the main textual content</li>
 *   <li>associated topics or tags</li>
 * </ul>
 * </p>
 * <p>
 * Equality and hash code are defined based on the {@code id} and {@code title}
 * fields, allowing consistent behavior when instances are used in collections
 * such as {@link java.util.HashSet} or as keys in {@link java.util.HashMap}.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    /**
     * Unique identifier of the document.
     */
    @JsonProperty("id")
    private String id;

    /**
     * URL pointing to the source or location of the document.
     */
    @JsonProperty("url")
    private String url;

    /**
     * Title of the document.
     */
    @JsonProperty("title")
    private String title;

    /**
     * Full textual content of the document.
     */
    @JsonProperty("main_content")
    private String main_content;

    /**
     * Inferred topics related to the document
     */
    @JsonProperty("topic")
    private String topic;


    public Document() {
    }

    /**
     * Constructs a new {@code documents} instance with all its attributes.
     *
     * @param id      the unique identifier of the document
     * @param url     the URL associated with the document
     * @param title   the title of the document
     * @param main_content the main textual main_content of the document
     * @param topic topic string inferred, related to the document
     //*
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
     * Sets a new unique identifier for the document.
     *
     * @param newId the new identifier to assign
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
     * Sets a new URL for the document.
     *
     * @param newUrl the new URL to assign
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
     * Sets a new title for the document.
     *
     * @param newTitle the new title to assign
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Returns the textual content of the document.
     *
     * @return the document content
     */
    public String getMain_content() {
        return main_content;
    }

    /**
     * Sets new textual content for the document.
     *
     * @param newContent the new content to assign
     */
    public void setMain_content(String newContent) {
        this.main_content = newContent;
    }

    /*
    /**
     * Returns the topics or keywords associated with the document.
     *
     * @return the document topics
     */
/*    public String getTopics() {
        return topics;
    }
*/
    /*
    /**
     * Sets new topics or keywords for the document.
     *
     * @param newTopics the new topics to assign
     */
    /*
    public void setTopics(String newTopics) {
        this.topics = newTopics;
    }
*/

    /**
     * Indicates whether this document is equal to another object.
     * <p>
     * Two {@code documents} objects are considered equal if and only if
     * they are of the same class and have the same {@code id} and {@code title}.
     * </p>
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
     * <p>
     * The hash code is computed using the {@code id} and {@code title} fields,
     * ensuring consistency with the {@link #equals(Object)} method.
     * </p>
     *
     * @return a hash code value for this document
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
