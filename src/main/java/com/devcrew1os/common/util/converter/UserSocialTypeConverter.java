package com.devcrew1os.common.util.converter;

import com.devcrew1os.common.enums.UserSocialType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class UserSocialTypeConverter implements AttributeConverter<UserSocialType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserSocialType attribute) {
        return (attribute != null) ? attribute.getValue() : null;
    }

    @Override
    public UserSocialType convertToEntityAttribute(Integer dbData) {
        return (dbData != null) ? UserSocialType.fromValue(dbData) : null;
    }
}
