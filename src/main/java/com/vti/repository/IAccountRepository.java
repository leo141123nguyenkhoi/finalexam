
package com.vti.repository;

import com.vti.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IAccountRepository
        extends JpaRepository<Account, Long>,
        JpaSpecificationExecutor<Account> {

    Optional<Account> findByUsernameIgnoreCase(
            String username
    );

    Optional<Account> findByEmailIgnoreCase(
            String email
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    @Modifying(flushAutomatically = true)
    @Query("""
       UPDATE Account a
       SET a.department = null
       WHERE a.department.id = :departmentId
       """)
    int detachDepartment(
            @Param("departmentId") Long departmentId
    );
}
