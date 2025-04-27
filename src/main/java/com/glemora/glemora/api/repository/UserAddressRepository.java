package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress,Long> {

    void  deleteByUserId(Long userId);
}
