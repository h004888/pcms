package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.dto.request.CreateFamilyMemberRequest;
import com.pcms.customerportal.dto.request.UpdateFamilyMemberRequest;
import com.pcms.customerportal.dto.response.FamilyMemberResponse;
import com.pcms.customerportal.dto.response.JsonStringUtil;
import com.pcms.customerportal.entity.CustomerFamily;
import com.pcms.customerportal.enums.RecordStatus;
import com.pcms.customerportal.repository.CustomerFamilyRepository;
import com.pcms.customerportal.service.FamilyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FamilyServiceImpl implements FamilyService {

    private static final Logger log = LoggerFactory.getLogger(FamilyServiceImpl.class);

    private final CustomerFamilyRepository repo;

    public FamilyServiceImpl(CustomerFamilyRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> list(UUID currentCustomerId) {
        return repo.findByOwnerIdAndStatusOrderByCreatedAtDesc(currentCustomerId, RecordStatus.ACTIVE)
                .stream()
                .map(FamilyMemberResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyMemberResponse get(UUID currentCustomerId, UUID memberId) {
        return FamilyMemberResponse.from(loadOwned(currentCustomerId, memberId));
    }

    @Override
    @Transactional
    public FamilyMemberResponse create(UUID currentCustomerId, CreateFamilyMemberRequest req) {
        CustomerFamily f = new CustomerFamily();
        f.setOwnerId(currentCustomerId);
        f.setMemberName(req.memberName());
        f.setRelationship(req.relationship());
        f.setDob(req.dob());
        f.setGender(req.gender());
        f.setAllergies(JsonStringUtil.fromList(req.allergies()));
        f.setChronicConditions(JsonStringUtil.fromList(req.chronicConditions()));

        CustomerFamily saved = repo.save(f);
        log.info("[family] added member id={} owner={} name={}",
                saved.getId(), currentCustomerId, saved.getMemberName());
        return FamilyMemberResponse.from(saved);
    }

    @Override
    @Transactional
    public FamilyMemberResponse update(UUID currentCustomerId, UUID memberId, UpdateFamilyMemberRequest req) {
        CustomerFamily f = loadOwned(currentCustomerId, memberId);
        if (req.memberName() != null)        f.setMemberName(req.memberName());
        if (req.relationship() != null)      f.setRelationship(req.relationship());
        if (req.dob() != null)               f.setDob(req.dob());
        if (req.gender() != null)            f.setGender(req.gender());
        if (req.allergies() != null)         f.setAllergies(JsonStringUtil.fromList(req.allergies()));
        if (req.chronicConditions() != null) f.setChronicConditions(JsonStringUtil.fromList(req.chronicConditions()));
        return FamilyMemberResponse.from(repo.save(f));
    }

    @Override
    @Transactional
    public void softDelete(UUID currentCustomerId, UUID memberId) {
        CustomerFamily f = loadOwned(currentCustomerId, memberId);
        f.setStatus(RecordStatus.INACTIVE);
        repo.save(f);
        log.info("[family] soft-deleted member id={} owner={}", memberId, currentCustomerId);
    }

    private CustomerFamily loadOwned(UUID currentCustomerId, UUID memberId) {
        CustomerFamily f = repo.findByIdAndStatus(memberId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Family member not found: " + memberId,
                        "Không tìm thấy thành viên gia đình: " + memberId));
        if (!f.getOwnerId().equals(currentCustomerId)) {
            throw new InvalidOperationException(
                    "Family member does not belong to current customer",
                    "Thành viên không thuộc về khách hàng hiện tại");
        }
        return f;
    }
}
