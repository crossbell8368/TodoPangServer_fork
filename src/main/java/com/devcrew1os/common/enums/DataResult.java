package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataResult {
    INVALID_STATUS("Invalid data status"),
    NOT_FOUND("Request data no found"),
    NOT_PREPARED("Request data is not prepared"),
    ALREADY_DEPLOYED("Request data already deployed"),
    ALREADY_NEUTRALIZED("Request data already neutralized"),
    CATEGORY_NOT_DEPLOYED("Category not deployed"),
    INCOMPLETE_DATA("Incomplete data"),
    SUCCESSFULLY_DEPLOY("Successfully deployed"),
    SUCCESSFULLY_NEUTRALIZED("Successfully neutralized"),
    SUCCESSFULLY_DELETED("Successfully deleted");

    private final String desc;

    public boolean isSuccess(){
        return this == SUCCESSFULLY_NEUTRALIZED || this == SUCCESSFULLY_DELETED || this == SUCCESSFULLY_DEPLOY;
    }
}
