package com.bitespeed.task.repository;

import com.bitespeed.task.models.Contact;
import com.bitespeed.task.enums.LinkPrecedenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByPhoneNumber(String phoneNumber);

    List<Contact> findByEmail(String email);

    List<Contact> findAllByLinkedId(Long id);


    @Modifying
    @Query("UPDATE Contact e SET e.linkedId = :linkedId, e.linkPrecedence = :linkPrecedence, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    @Transactional
    void updateContactById(@Param("id") Long id, @Param("linkedId") Long linkedId, @Param("linkPrecedence") LinkPrecedenceType linkPrecedence);

}
