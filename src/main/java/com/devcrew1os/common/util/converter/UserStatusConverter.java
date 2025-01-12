package com.devcrew1os.common.util.converter;

import com.devcrew1os.common.enums.UserStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserStatus attribute) {
        return (attribute != null) ? attribute.getValue() : null;
    }

    @Override
    public UserStatus convertToEntityAttribute(Integer dbData) {
        return (dbData != null) ? UserStatus.fromValue(dbData) : null;
    }
}
