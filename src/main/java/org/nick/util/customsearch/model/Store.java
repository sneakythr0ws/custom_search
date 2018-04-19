package org.nick.util.customsearch.model;

/**
 * Created by VNikolaenko on 29.06.2015.
 */
/*@Value
@RequiredArgsConstructor
@EqualsAndHashCode(of = "link")*/
public class Store implements Searchable {
    private final String link;

    public Store(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Store{" +
                "link='" + link + '\'' +
                '}';
    }
}
