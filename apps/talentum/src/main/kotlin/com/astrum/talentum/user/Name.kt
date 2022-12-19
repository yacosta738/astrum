package com.astrum.talentum.user

data class Name(val firstName: FirstName, val lastName: LastName) : Comparable<Name> {

    constructor(firstName: String, lastName: String) : this(
        FirstName(firstName),
        LastName(lastName)
    )

    fun fullName(): String {
        return "$firstName $lastName"
    }

    override operator fun compareTo(other: Name): Int {
        return COMPARATOR.compare(this, other)
    }

    companion object {
        private val COMPARATOR = Comparator.comparing(Name::firstName).thenComparing(Name::lastName)
    }
}