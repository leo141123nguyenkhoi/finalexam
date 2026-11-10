
package com.vti.service;

import com.vti.dto.AccountDTO;
import com.vti.dto.PageResponse;
import com.vti.entity.Role;
import com.vti.form.CreatingAccountForm;
import com.vti.form.UpdatingAccountForm;

import java.util.List;

public interface IAccountService {

    PageResponse<AccountDTO> list(
            int page,
            String search,
            Role role,
            Long departmentId,
            String sortBy,
            String direction
    );

    AccountDTO get(long id);

    AccountDTO create(CreatingAccountForm form);

    AccountDTO update(
            long id,
            UpdatingAccountForm form,
            String actor
    );

    void deleteBatch(List<Long> ids, String actor);
}
