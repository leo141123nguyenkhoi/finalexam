
package com.vti.config;

import com.vti.repository.IAccountRepository;
import com.vti.repository.IDepartmentRepository;
import com.vti.specification.AccountSpecification;
import com.vti.specification.DepartmentSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DatabaseCheckRunner implements CommandLineRunner {

    private final IAccountRepository accountRepository;
    private final IDepartmentRepository departmentRepository;



    @Override
    public void run(String... args) {

        long totalAccount = accountRepository.count();
        long totalDepartment = departmentRepository.count();

        System.out.println("=================================");
        System.out.println("KIEM TRA DATABASE FINAL EXAM");
        System.out.println("So luong Account: " + totalAccount);
        System.out.println("So luong Department: " + totalDepartment);
        System.out.println("=================================");


        var accountPage = accountRepository.findAll(
                AccountSpecification.filter(null, null, null),
                PageRequest.of(
                        0,
                        10,
                        Sort.by(Sort.Direction.DESC, "createdDate")
                )
        );

        var departmentPage = departmentRepository.findAll(
                DepartmentSpecification.filter(null, null, null, null),
                PageRequest.of(
                        0,
                        10,
                        Sort.by(Sort.Direction.DESC, "totalMember")
                )
        );

        System.out.println("===== KIEM TRA SPECIFICATION =====");

        System.out.println(
                "Tong Account: " + accountPage.getTotalElements()
        );

        System.out.println(
                "Account trang 1: " + accountPage.getNumberOfElements()
        );

        System.out.println(
                "Tong Department: " + departmentPage.getTotalElements()
        );

        System.out.println(
                "Department trang 1: " + departmentPage.getNumberOfElements()
        );

    }
}
