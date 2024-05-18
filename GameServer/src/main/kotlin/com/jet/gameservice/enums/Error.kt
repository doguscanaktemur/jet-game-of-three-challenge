package com.jet.gameservice.enums

enum class Error(val message: String, val code: String) {
    NOT_YOUR_TURN(
        "This is not your turn.",
        "NOT_YOUR_TURN"
    ),
    RESULTING_NUMBER_NOT_INTEGER(
        "The resulting number '%s' is not an integer.",
        "RESULTING_NUMBER_NOT_INTEGER"
    ),
    RESULTING_NUMBER_TOO_SMALL(
        "The resulting number '%d' is less than 2.",
        "RESULTING_NUMBER_TOO_SMALL"
    ),
    ILLEGAL_ADDED_NUMBER(
        "added number '%d' is illegal. Legal values are: 1, 0, -1",
        "ILLEGAL_ADDED_NUMBER"
    ),
    ADDED_NUMBER_NOT_INTEGER(
        "The added number '%s' is not an integer.",
        "ADDED_NUMBER_NOT_INTEGER"
    ),
    NOT_DIVISIBLE_BY_3(
        "The resulting number '%d' not divisible by 3.",
        "NOT_DIVISIBLE_BY_3"
    ),
    PARAMETER_IS_NULL(
        "The parameter '%s' can't be null.",
        "PARAMETER_IS_NULL"
    );

}