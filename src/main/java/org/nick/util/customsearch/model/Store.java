package org.nick.util.customsearch.model;

/**
 * Created by VNikolaenko on 29.06.2015.
 */
/*@Value
@RequiredArgsConstructor
@EqualsAndHashCode(of = "link")*/
public class Store implements Searchable {
    private final String link;
    private final String title;

    public Store(String link, String title) {
        this.link = link;
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }
}
