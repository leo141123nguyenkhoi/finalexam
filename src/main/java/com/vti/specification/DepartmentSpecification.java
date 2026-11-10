
package com.vti.specification;

import com.vti.entity.Department;
import com.vti.entity.DepartmentType;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DepartmentSpecification {

    private DepartmentSpecification() {
    }

    public static Specification<Department> filter(
            String search,
            DepartmentType type,
            LocalDate minDate,
            LocalDate maxDate
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {

                String keyword = "%" +
                        search.trim().toLowerCase(Locale.ROOT) +
                        "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                keyword
                        )
                );
            }

            if (type != null) {

                predicates.add(
                        cb.equal(root.get("type"), type)
                );
            }

            if (minDate != null) {

                LocalDateTime fromDate = minDate.atStartOfDay();

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdDate"),
                                fromDate
                        )
                );
            }

            if (maxDate != null) {

                LocalDateTime toDate =
                        maxDate.plusDays(1).atStartOfDay();

                predicates.add(
                        cb.lessThan(
                                root.get("createdDate"),
                                toDate
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
