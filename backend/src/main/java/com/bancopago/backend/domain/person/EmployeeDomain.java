package com.bancopago.backend.domain.person;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;

import java.util.UUID;

public class EmployeeDomain extends PersonDomain {

    private final String position;
    private final String area;
    private final String costCenter;
    private final String contractType;

    public EmployeeDomain(UUID id, DocumentNumber documentNumber, String name,
                          Email email, String phone,
                          String position, String area, String costCenter, String contractType) {
        super(id, documentNumber, name, email, phone, PersonType.EMPLOYEE);
        this.position = requirePosition(position);
        this.area = requireArea(area);
        this.costCenter = TextHelper.applyTrim(costCenter);
        this.contractType = TextHelper.applyTrim(contractType);
    }

    public EmployeeDomain(String name, DocumentNumber documentNumber,
                          Email email, String phone,
                          String position, String area, String costCenter, String contractType) {
        this(null, documentNumber, name, email, phone, position, area, costCenter, contractType);
    }

    private static String requirePosition(String position) {
        var trimmed = TextHelper.applyTrim(position);
        if (TextHelper.isBlank(trimmed)) {
            throw InvalidPersonException.create(PersonError.POSITION_REQUIRED);
        }
        return trimmed;
    }

    private static String requireArea(String area) {
        var trimmed = TextHelper.applyTrim(area);
        if (TextHelper.isBlank(trimmed)) {
            throw InvalidPersonException.create(PersonError.AREA_REQUIRED);
        }
        return trimmed;
    }

    public String getPosition() { return position; }
    public String getArea() { return area; }
    public String getCostCenter() { return costCenter; }
    public String getContractType() { return contractType; }
}
