package com.stasdev.backend.model.services;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;

public interface Preparer {
    ApplicationUser prepareToSave(ApplicationUser userForSave, Role userRole);
    Account prepareToSave(Account account);
}
