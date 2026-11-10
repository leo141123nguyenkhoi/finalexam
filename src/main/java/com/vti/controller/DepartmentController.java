
package com.vti.controller;

import com.vti.dto.DepartmentDTO;
import com.vti.dto.PageResponse;
import com.vti.entity.DepartmentType;
import com.vti.form.DepartmentForm;
import com.vti.service.IDepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Department", description = "Quan ly phong ban")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final IDepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Danh sach va loc Department")
    public PageResponse<DepartmentDTO> getAllDepartments(

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            DepartmentType type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate minDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate maxDate,

            @RequestParam(defaultValue = "totalMember")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        return departmentService.list(
                page,
                search,
                type,
                minDate,
                maxDate,
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lay Department theo ID")
    public EntityModel<DepartmentDTO> getDepartment(
            @PathVariable Long id
    ) {

        DepartmentDTO department = departmentService.get(id);

        return EntityModel.of(
                department,
                Link.of("/api/departments/" + id).withSelfRel(),
                Link.of("/api/departments").withRel("collection")
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tao Department moi")
    public DepartmentDTO createDepartment(
            @Valid @RequestBody DepartmentForm form
    ) {
        return departmentService.create(form);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cap nhat Department")
    public DepartmentDTO updateDepartment(

            @PathVariable Long id,

            @Valid @RequestBody DepartmentForm form
    ) {
        return departmentService.update(id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xoa Department")
    public void deleteDepartment(
            @PathVariable Long id
    ) {
        departmentService.delete(id);
    }

    @PutMapping("/{id}/members")
    @Operation(summary = "Them nhieu Account vao Department")
    public Map<String, String> addMembers(

            @PathVariable Long id,

            @RequestBody List<Long> accountIds
    ) {

        departmentService.addMembers(id, accountIds);

        return Map.of(
                "message",
                "Members added successfully"
        );
    }

    @DeleteMapping("/{id}/members/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xoa Account khoi Department")
    public void removeMember(

            @PathVariable Long id,

            @PathVariable Long accountId
    ) {

        departmentService.removeMember(id, accountId);
    }
}
