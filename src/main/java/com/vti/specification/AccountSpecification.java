
package com.vti.specification;

import com.vti.entity.Account;
import com.vti.entity.Role;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AccountSpecification {

    private AccountSpecification() {
    }

    public static Specification<Account> filter(
            String search,
            Role role,
            Long departmentId
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {

                String keyword = "%" +
                        search.trim().toLowerCase(Locale.ROOT) +
                        "%";

                Predicate searchCondition = cb.or(

                        cb.like(
                                cb.lower(root.get("username")),
                                keyword
                        ),

                        cb.like(
                                cb.lower(root.get("firstName")),
                                keyword
                        ),

                        cb.like(
                                cb.lower(root.get("lastName")),
                                keyword
                        )
                );

                predicates.add(searchCondition);
            }

            if (role != null) {

                predicates.add(
                        cb.equal(root.get("role"), role)
                );
            }

            if (departmentId != null) {

                predicates.add(
                        cb.equal(
                                root.get("department").get("id"),
                                departmentId
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
