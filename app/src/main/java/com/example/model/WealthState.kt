package com.example.model

data class ProfileState(
    val name: String = "",
    val age: String = "",
    val residency: String = "Indian Resident",
    val occupation: String = "Salaried – Private Sector",
    val city: String = "",
    val maritalStatus: String = "Married",
    val spouseName: String = "",
    val spouseAge: String = "",
    val spouseOccupation: String = "",
    val dependents: String = "",
    val riskScore: Int = 2
)

data class IncomeState(
    val annual: String = "",
    val spouseAnnual: String = "",
    val monthlyExp: String = "",
    val savings: String = "",
    val totalDebt: String = "",
    val emi: String = "",
    val emergencyCorpus: String = "",
    val efMonths: String = "6",
    val savingsWhere: String = "Savings Account"
)

data class ProtectionState(
    val hasTerm: String = "No",
    val termSA: String = "",
    val termPremium: String = "",
    val termExpiry: String = "",
    val hasHealth: String = "No",
    val healthSA: String = "",
    val healthType: String = "Family Floater",
    val hasCI: String = "No",
    val ciSA: String = "",
    val hasAccident: String = "No",
    val accidentSA: String = "",
    val hasProperty: String = "No"
)

data class RetirementState(
    val retireAge: String = "60",
    val hasNPS: String = "No",
    val npsCorpus: String = "",
    val npsMonthly: String = "",
    val hasEPF: String = "No",
    val epfBalance: String = "",
    val hasPPF: String = "No",
    val ppfBalance: String = "",
    val hasGratuity: String = "No",
    val gratuityEst: String = "",
    val retireMonthly: String = "",
    val pensionAvail: String = "No",
    val pensionAmt: String = "",
    val retireNotes: String = ""
)

data class EstateState(
    val willStatus: String = "No Will",
    val nominations: String = "None",
    val nominationGaps: String = "",
    val trust: String = "No",
    val huf: String = "No",
    val poa: String = "No",
    val poaHolder: String = "",
    val locker: String = "No",
    val liabilitiesAmt: String = "",
    val digitalAssets: String = "",
    val familyBusiness: String = "No",
    val estateNotes: String = ""
)

data class GoalsState(
    val selected: List<String> = emptyList(),
    val horizon: String = "10–15 years",
    val notes: String = ""
)

data class AdvisorState(
    val name: String = "Jitender Chaudhary",
    val firm: String = "FV India",
    val phone: String = "9582250638",
    val email: String = "jitender@fvindia.com",
    val tagline: String = "Trust. Transparency. Prosperity."
)
