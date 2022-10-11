package com.pvs.spent.data

data class ExpenseFirestore(
    public var ofUser: String = "",
    public var id: Int = 0,
    public var title: String = "",
    public var amount: Float = 0f,
    public var ofCategory: String = "",
    public var createdOn: String = "",
    public var backedUp: Int = 0
)