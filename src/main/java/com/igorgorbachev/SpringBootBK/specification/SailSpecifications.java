package com.igorgorbachev.SpringBootBK.specification;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import org.springframework.data.jpa.domain.Specification;

public class SailSpecifications {
    public static Specification<Sail> withKlientId(Long klientId) {
        return (root, query, cb) ->
                klientId != null ? cb.equal(root.get("klient").get("id"), klientId) : null;
    }

    public static Specification<Sail> withStatusId(Long statusId) {
        return (root, query, cb) ->
                statusId != null ? cb.equal(root.get("status").get("id"), statusId) : null;
    }

    public static Specification<Sail> withOplataId(Long oplataId) {
        return (root, query, cb) ->
                oplataId != null ? cb.equal(root.get("oplata").get("id"), oplataId) : null;
    }
}
