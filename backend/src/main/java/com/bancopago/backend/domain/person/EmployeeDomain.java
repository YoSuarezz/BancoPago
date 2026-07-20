package com.bancopago.backend.domain.person;

import com.bancopago.backend.domain.enums.PersonType;
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
        this.position = position;
        this.area = area;
        this.costCenter = costCenter;
        this.contractType = contractType;
    }

    public EmployeeDomain(String name, DocumentNumber documentNumber,
                          Email email, String phone,
                          String position, String area, String costCenter, String contractType) {
        this(null, documentNumber, name, email, phone, position, area, costCenter, contractType);
    }

    public String getPosition() { return position; }
    public String getArea() { return area; }
    public String getCostCenter() { return costCenter; }
    public String getContractType() { return contractType; }
}
