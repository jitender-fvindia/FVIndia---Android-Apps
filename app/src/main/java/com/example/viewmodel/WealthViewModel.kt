package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class ScoreResults(
    val divScore: Int,
    val insScore: Int,
    val efScore: Int,
    val retScore: Int,
    val estScore: Int,
    val taxScore: Int,
    val debtScore: Int,
    val overall: Int,
    val efTarget: Double,
    val efPct: Int,
    val monthlyExp: Double,
    val annualIncome: Double,
    val totalPortfolio: Double
)

class WealthViewModel : ViewModel() {

    private val _currentStep = MutableStateFlow("welcome")
    val currentStep = _currentStep.asStateFlow()

    private val _profile = MutableStateFlow(ProfileState())
    val profile = _profile.asStateFlow()

    private val _income = MutableStateFlow(IncomeState())
    val income = _income.asStateFlow()

    private val _protection = MutableStateFlow(ProtectionState())
    val protection = _protection.asStateFlow()

    private val _retirement = MutableStateFlow(RetirementState())
    val retirement = _retirement.asStateFlow()

    private val _estate = MutableStateFlow(EstateState())
    val estate = _estate.asStateFlow()

    private val _goals = MutableStateFlow(GoalsState())
    val goals = _goals.asStateFlow()

    private val _advisor = MutableStateFlow(AdvisorState())
    val advisor = _advisor.asStateFlow()

    // Map track of selected assets by id -> boolean
    private val _assets = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val assets = _assets.asStateFlow()

    // Map track of asset approximate values by id -> value string
    private val _assetValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val assetValues = _assetValues.asStateFlow()

    private val _generating = MutableStateFlow(false)
    val generating = _generating.asStateFlow()

    private val _report = MutableStateFlow("")
    val report = _report.asStateFlow()

    // UI Steps list
    val steps = listOf("welcome", "profile", "income", "assets", "protection", "retirement", "estate", "goals", "results")

    fun navigateTo(step: String) {
        if (steps.contains(step)) {
            _currentStep.value = step
        }
    }

    fun navigateNext() {
        val currentIdx = steps.indexOf(_currentStep.value)
        if (currentIdx < steps.lastIndex) {
            _currentStep.value = steps[currentIdx + 1]
        }
    }

    fun navigateBack() {
        val currentIdx = steps.indexOf(_currentStep.value)
        if (currentIdx > 0) {
            _currentStep.value = steps[currentIdx - 1]
        }
    }

    // State Mutation Helpers
    fun updateProfile(updater: (ProfileState) -> ProfileState) {
        _profile.value = updater(_profile.value)
    }

    fun updateIncome(updater: (IncomeState) -> IncomeState) {
        _income.value = updater(_income.value)
    }

    fun updateProtection(updater: (ProtectionState) -> ProtectionState) {
        _protection.value = updater(_protection.value)
    }

    fun updateRetirement(updater: (RetirementState) -> RetirementState) {
        _retirement.value = updater(_retirement.value)
    }

    fun updateEstate(updater: (EstateState) -> EstateState) {
        _estate.value = updater(_estate.value)
    }

    fun updateGoals(updater: (GoalsState) -> GoalsState) {
        _goals.value = updater(_goals.value)
    }

    fun updateAdvisor(updater: (AdvisorState) -> AdvisorState) {
        _advisor.value = updater(_advisor.value)
    }

    fun toggleAsset(assetId: String, isSelected: Boolean) {
        val updated = _assets.value.toMutableMap()
        updated[assetId] = isSelected
        _assets.value = updated
    }

    fun updateAssetValue(assetId: String, value: String) {
        val updated = _assetValues.value.toMutableMap()
        updated[assetId] = value
        _assetValues.value = updated
    }

    fun resetAssessment() {
        _profile.value = ProfileState()
        _income.value = IncomeState()
        _protection.value = ProtectionState()
        _retirement.value = RetirementState()
        _estate.value = EstateState()
        _goals.value = GoalsState()
        _assets.value = emptyMap()
        _assetValues.value = emptyMap()
        _report.value = ""
        _generating.value = false
        _currentStep.value = "profile"
    }

    // Asset id to category helper
    private val assetCategories = mapOf(
        "direct_eq" to "Equity & Growth",
        "eq_mf" to "Equity & Growth",
        "etf" to "Equity & Growth",
        "pms" to "Equity & Growth",
        "aif" to "Equity & Growth",
        "esop" to "Equity & Growth",
        "unlisted" to "Equity & Growth",
        "intl_eq" to "Equity & Growth",
        "bank_fd" to "Fixed Income & Debt",
        "corp_bonds" to "Fixed Income & Debt",
        "debt_mf" to "Fixed Income & Debt",
        "govt_sec" to "Fixed Income & Debt",
        "rbi_bonds" to "Fixed Income & Debt",
        "ppf_fi" to "Fixed Income & Debt",
        "nsc_kvp" to "Fixed Income & Debt",
        "sgb" to "Fixed Income & Debt",
        "res_prop" to "Real Estate",
        "com_prop" to "Real Estate",
        "reit" to "Real Estate",
        "invit" to "Real Estate",
        "plot" to "Real Estate",
        "agri" to "Real Estate",
        "phys_gold" to "Gold & Commodities",
        "gold_etf" to "Gold & Commodities",
        "silver" to "Gold & Commodities",
        "nre_acc" to "NRI-Specific Instruments",
        "nro_acc" to "NRI-Specific Instruments",
        "fcnr" to "NRI-Specific Instruments",
        "overseas_eq" to "NRI-Specific Instruments",
        "nps_a" to "Retirement Vehicles",
        "epf_a" to "Retirement Vehicles",
        "pension_plan" to "Retirement Vehicles",
        "crypto" to "Alternative & Digital",
        "p2p" to "Alternative & Digital",
        "startup" to "Alternative & Digital",
        "art" to "Alternative & Digital"
    )

    fun computeScores(): ScoreResults {
        val prof = _profile.value
        val inc = _income.value
        val prot = _protection.value
        val ret = _retirement.value
        val est = _estate.value

        val annualIncome = inc.annual.toDoubleOrNull() ?: 0.0
        val monthlyExp = inc.monthlyExp.toDoubleOrNull() ?: (annualIncome / 12 * 0.6)

        // 1. Portfolio Diversification Score
        val selectedAssetIds = _assets.value.filter { it.value }.keys
        val distinctCategories = selectedAssetIds.mapNotNull { assetCategories[it] }.toSet()
        val divScore = clamp(distinctCategories.size * 13 + selectedAssetIds.size * 3, 0, 100)

        // 2. Protection / Insurance Score
        var insScore = 0
        if (prot.hasTerm == "Yes") {
            insScore += 30
            val termSAVal = prot.termSA.toDoubleOrNull() ?: 0.0
            if (termSAVal >= annualIncome * 10) {
                insScore += 10
            }
        }
        if (prot.hasHealth == "Yes") insScore += 25
        if (prot.hasCI == "Yes") insScore += 15
        if (prot.hasAccident == "Yes") insScore += 10
        if (prot.hasProperty == "Yes") insScore += 10
        insScore = clamp(insScore, 0, 100)

        // 3. Emergency / Contingency Score
        val efCorpus = inc.emergencyCorpus.toDoubleOrNull() ?: 0.0
        val targetMonths = inc.efMonths.toDoubleOrNull() ?: 6.0
        val efTarget = monthlyExp * targetMonths
        val efPct = if (efTarget > 0) ((efCorpus / efTarget) * 100).toInt() else 0
        val efScore = clamp(efPct, 0, 100)

        // 4. Retirement Score
        var retScore = 0
        if (ret.hasNPS == "Yes") retScore += 20
        if (ret.hasEPF == "Yes") retScore += 20
        if (ret.hasPPF == "Yes") retScore += 15
        if (ret.retireMonthly.isNotEmpty()) retScore += 25
        retScore = clamp(retScore, 0, 100)

        // 5. Estate / Succession Score
        var estScore = 0
        if (est.willStatus.contains("executed")) estScore += 35
        else if (est.willStatus.contains("progress")) estScore += 15
        if (est.nominations == "Complete") estScore += 30
        else if (est.nominations == "Partial") estScore += 12
        if (est.poa != "No") estScore += 15
        if (est.trust != "No") estScore += 20
        estScore = clamp(estScore, 0, 100)

        // 6. Tax Score
        var taxScore = 40
        if (selectedAssetIds.contains("eq_mf")) taxScore += 10
        if (ret.hasEPF == "Yes" || ret.hasPPF == "Yes") taxScore += 20
        if (ret.hasNPS == "Yes") taxScore += 20
        if (prot.hasTerm == "Yes" || prot.hasHealth == "Yes") taxScore += 10
        taxScore = clamp(taxScore, 0, 100)

        // 7. Debt Score
        val emi = inc.emi.toDoubleOrNull() ?: 0.0
        val emiRatio = if (annualIncome > 0) (emi * 12 / annualIncome) * 100 else 50.0
        val debtScore = clamp((100 - emiRatio * 2).toInt(), 10, 100)

        // Overall Score
        val overall = (divScore + insScore + efScore + retScore + estScore + taxScore + debtScore) / 7

        // Total Portfolio
        val totalPortfolio = _assetValues.value
            .filter { _assets.value[it.key] == true }
            .mapNotNull { it.value.toDoubleOrNull() }
            .sum()

        return ScoreResults(
            divScore = divScore,
            insScore = insScore,
            efScore = efScore,
            retScore = retScore,
            estScore = estScore,
            taxScore = taxScore,
            debtScore = debtScore,
            overall = overall,
            efTarget = efTarget,
            efPct = efPct,
            monthlyExp = monthlyExp,
            annualIncome = annualIncome,
            totalPortfolio = totalPortfolio
        )
    }

    private fun clamp(v: Int, mn: Int, mx: Int): Int {
        return max(mn, min(mx, v))
    }

    // Currency Formatter Helper
    fun formatCurrency(amount: Double): String {
        return when {
            amount >= 10000000 -> "₹${String.format("%.2f", amount / 10000000)}Cr"
            amount >= 100000 -> "₹${String.format("%.2f", amount / 100000)}L"
            amount >= 1000 -> "₹${String.format("%.1f", amount / 1000)}K"
            amount > 0 -> "₹${String.format("%.0f", amount)}"
            else -> "—"
        }
    }

    fun generateReport() {
        if (_generating.value) return
        _generating.value = true
        _report.value = ""
        _currentStep.value = "results"

        viewModelScope.launch {
            val scores = computeScores()
            val prompt = buildPromptText(scores)
            
            val aiResponse = GeminiApiClient.generateReport(prompt)
            if (aiResponse == "API_KEY_MISSING") {
                // Return a beautifully detailed, locally customized fallback report so it is robust!
                _report.value = buildLocalFallbackReport(scores)
            } else if (aiResponse.startsWith("Error:")) {
                _report.value = "${aiResponse}\n\n" + buildLocalFallbackReport(scores)
            } else {
                _report.value = aiResponse
            }
            _generating.value = false
        }
    }

    private fun buildPromptText(scores: ScoreResults): String {
        val prof = _profile.value
        val inc = _income.value
        val prot = _protection.value
        val ret = _retirement.value
        val est = _estate.value
        val goalsVal = _goals.value
        val adv = _advisor.value

        val selectedAssetList = _assets.value.filter { it.value }.keys.map { id ->
            val cat = assetCategories[id] ?: "Unknown"
            val stateVal = _assetValues.value[id] ?: "Unspecified"
            val parsedVal = stateVal.toDoubleOrNull()?.let { formatCurrency(it) } ?: stateVal
            "$cat: $id ($parsedVal)"
        }.joinToString("\n")

        val age = prof.age.toIntOrNull() ?: 35
        val retAge = ret.retireAge.toIntOrNull() ?: 60
        val yearsToRet = max(1, retAge - age)
        val desiredRetIncome = ret.retireMonthly.toDoubleOrNull() ?: 0.0
        val futureMonthly = desiredRetIncome * 1.07.pow(yearsToRet)
        val corpusNeeded = (futureMonthly * 12) / 0.04
        val sipNeeded = corpusNeeded / (((1.10.pow(yearsToRet) - 1) / 0.10) * 12)

        return """
You are WealthCanvas — India's most sophisticated personal finance intelligence platform. Speak directly to the CLIENT in warm, clear, jargon-free English. Be like a trusted friend who happens to be India's best financial advisor. Warm but authoritative. Reference specific numbers and give actionable, region-specific details for India.

CLIENT: ${prof.name.ifEmpty { "Client" }}, Age ${prof.age}, ${prof.residency}, ${prof.occupation}
City: ${prof.city}
Risk Profile Comfort: ${getRiskLabel(prof.riskScore)} — ${getRiskDesc(prof.riskScore)}
${if (prof.maritalStatus == "Married") "Spouse: ${prof.spouseName}, Age ${prof.spouseAge}, Occupation: ${prof.spouseOccupation}" else "Single"}
Dependents: ${prof.dependents.ifEmpty { "0" }}

FINANCES:
Annual Income: ${formatCurrency(scores.annualIncome)} ${if (prof.maritalStatus == "Married" && inc.spouseAnnual.isNotEmpty()) " | Spouse: " + formatCurrency(inc.spouseAnnual.toDoubleOrNull() ?: 0.0) else ""}
Monthly Expenses: ${formatCurrency(scores.monthlyExp)} | Monthly Savings: ${formatCurrency(inc.savings.toDoubleOrNull() ?: 0.0)}
Total Debt: ${formatCurrency(inc.totalDebt.toDoubleOrNull() ?: 0.0)} | Monthly EMI: ${formatCurrency(inc.emi.toDoubleOrNull() ?: 0.0)}
Emergency Fund Details: ${formatCurrency(inc.emergencyCorpus.toDoubleOrNull() ?: 0.0)} (parking details: ${inc.savingsWhere}, target target: ${inc.efMonths} months = ${formatCurrency(scores.efTarget)})

PORTFOLIO (Total ≈ ${formatCurrency(scores.totalPortfolio)}):
${if (selectedAssetList.isNotEmpty()) selectedAssetList else "No assets currently declared"}

PROTECTION PLAN:
Term Life: ${if (prot.hasTerm == "Yes") formatCurrency(prot.termSA.toDoubleOrNull() ?: 0.0) + " sum assured" else "NONE — CRITICAL GAP"}
Health Insurance: ${if (prot.hasHealth == "Yes") formatCurrency(prot.healthSA.toDoubleOrNull() ?: 0.0) + " (" + prot.healthType + ")" else "NONE — CRITICAL GAP"}
Critical Illness: ${if (prot.hasCI == "Yes") formatCurrency(prot.ciSA.toDoubleOrNull() ?: 0.0) else "Not covered"}
Personal Accident: ${if (prot.hasAccident == "Yes") formatCurrency(prot.accidentSA.toDoubleOrNull() ?: 0.0) else "Not covered"}

RETIREMENT:
Target Retirement Age: $retAge | Desired monthly retirement budget: ${formatCurrency(desiredRetIncome)} (adjusted for inflation is ${formatCurrency(futureMonthly)}/month)
Required Corpus calculated: ${formatCurrency(corpusNeeded)} | Required monthly SIP: ${formatCurrency(sipNeeded)}
NPS: ${if (ret.hasNPS == "Yes") "Corpus: " + formatCurrency(ret.npsCorpus.toDoubleOrNull() ?: 0.0) + ", monthly: " + formatCurrency(ret.npsMonthly.toDoubleOrNull() ?: 0.0) else "No"}
EPF: ${if (ret.hasEPF == "Yes") "Balance: " + formatCurrency(ret.epfBalance.toDoubleOrNull() ?: 0.0) else "No"}
PPF: ${if (ret.hasPPF == "Yes") "Balance: " + formatCurrency(ret.ppfBalance.toDoubleOrNull() ?: 0.0) else "No"}

ESTATE AND LEGALITY:
Will Status: ${est.willStatus}
Nominations updated: ${est.nominations} (missing in: ${est.nominationGaps})
Private Trust: ${est.trust} | Hindu Undivided Family Status: ${est.huf} | Power of Attorney: ${est.poa} (holder: ${est.poaHolder})

GOALS: ${if (goalsVal.selected.isNotEmpty()) goalsVal.selected.joinToString(", ") else "Not specified"}
${if (goalsVal.notes.isNotEmpty()) "Goal notes: " + goalsVal.notes else ""}

WEALTHCANVAS SCORES: Portfolio ${scores.divScore}/100 | Protection ${scores.insScore}/100 | Emergency ${scores.efScore}/100 | Retirement ${scores.retScore}/100 | Estate ${scores.estScore}/100 | Tax ${scores.taxScore}/100 | Debt ${scores.debtScore}/100 | OVERALL ${scores.overall}/100

${if (prof.residency != "Indian Resident") "NRI SPECIFIC CONTEXT: Make sure to advise on FEMA guidelines, NRE/NRO/FCNR accounts, tax liabilities under Indian DTAA, and repatriation constraints." else ""}

Please generate an exceptionally detailed and formatted text document using Markdown headers:

## 🎯 YOUR FINANCIAL HEALTH SNAPSHOT
Create 2-3 warm, inviting but firm paragraphs summarizing their current situation. Use and highlight their name and total assets on the canvas.

## ✅ WHAT YOU'RE DOING WELL
Write 3-4 bullet points styled with "✦" specifying exact, commendable behaviors matching their profile.

## ⚠️ YOUR TOP PRIORITY GAPS
List 4-5 core vulnerabilities by rank of urgency. For each gap specify:
**[Gap Title]** — what it is, *Why it matters*, *Advisor Recommended Action*, *Target Timeline*.

## 📊 SECTION-BY-SECTION DEEP DIVE

### 1. Portfolio & Investments
Analyze current asset allocation. Show where they have concentration risk (e.g. overly reliant on property, gold, or pure equity).

### 2. Protection & Insurance
Provide calculations on ideal coverage (e.g. 10-15x annual income for term life), evaluate the gaps and suggest products.

### 3. Emergency Fund Log
Assess if their contingencies are well parked. Show targets.

### 4. Retirement Planning
Compare current savings rate to what is mathematically required ($corpusNeeded corpus needed, calling for a $sipNeeded SIP). Highlight optimal retirement tools.

### 5. Tax Optimisation in India
Evaluate how to claim full exclusions under Section 80C, 80D, 80CCD(1B) for NPS, and LTCG harvesting.

### 6. Estate & Legal Legacy
Map out next steps for making a Will, updating nominees, and using Power of Attorney.

## 🗓️ YOUR 90-DAY ACTION PLAN
Provide a numbered checklist of 6-8 practical, highly specific legal and financial next steps.

## 💡 ANNUAL REVIEW CHECKLIST
List 5 essential check-ins to perform annually.

Closing sentence: End with a warm, energetic vote of confidence welcoming the client to contact advisor ${adv.name} from ${adv.firm} to turn this roadmap into reality.
        """.trimIndent()
    }

    private fun buildLocalFallbackReport(scores: ScoreResults): String {
        val prof = _profile.value
        val inc = _income.value
        val prot = _protection.value
        val ret = _retirement.value
        val est = _estate.value
        val goalsVal = _goals.value
        val adv = _advisor.value

        val clientName = prof.name.ifEmpty { "Valued Client" }
        val age = prof.age.toIntOrNull() ?: 35
        val retAge = ret.retireAge.toIntOrNull() ?: 60
        val yearsToRet = max(1, retAge - age)
        val desiredRetIncome = ret.retireMonthly.toDoubleOrNull() ?: 0.0
        val futureMonthly = desiredRetIncome * 1.07.pow(yearsToRet)
        val corpusNeeded = (futureMonthly * 12) / 0.04
        val sipNeeded = corpusNeeded / (((1.10.pow(yearsToRet) - 1) / 0.10) * 12)

        return """
## 🎯 YOUR FINANCIAL HEALTH SNAPSHOT
Welcome to your personalised WealthCanvas roadmap, **$clientName**. Having analyzed all 7 core dimensions of your financial health, you currently stand at an overall **WealthCanvas score of ${scores.overall}/100**. This reflects a solid initial foundation, but with some crucial gaps that, when filled, will transform your family's safety and long-term prosperity.

Your total declared assets on this canvas compute to **${formatCurrency(scores.totalPortfolio)}** against an annual income of **${formatCurrency(scores.annualIncome)}**. Your monthly lifestyle expenses stand at **${formatCurrency(scores.monthlyExp)}**, meaning your current structural savings rate is **${if (scores.annualIncome > 0) String.format("%.0f", (inc.savings.toDoubleOrNull() ?: 0.0) * 12 / scores.annualIncome * 100) else "0"}%**. Our objective is to streamline these money flows to build an bulletproof financial shield.

## ✅ WHAT YOU'RE DOING WELL
✦ **Savings Mindset:** You are actively saving ${formatCurrency(inc.savings.toDoubleOrNull() ?: 0.0)} per month towards your goals, which represents a healthy habit.
✦ **Controlled Leverage:** Your EMIs stand at ${formatCurrency(inc.emi.toDoubleOrNull() ?: 0.0)} per month. Maintaining this ratio within healthy debt parameters is crucial.
✦ **Goal Clarity:** You have outlined concrete targets like ${if (goalsVal.selected.isNotEmpty()) goalsVal.selected.joinToString(", ") else "Wealth Creation"} as major milestones.

## ⚠️ YOUR TOP PRIORITY GAPS
1. **Inadequate Life Protection** — *Why it matters:* Without a designated term cover of at least 10–12x your annual income (${formatCurrency(scores.annualIncome * 12)}), your dependents are vulnerable to immediate financial distress if a crisis occurs. *Recommended action:* Procure a pure Term Life policy with a sum assured of at least ${formatCurrency(scores.annualIncome * 12)}. *Target:* 15 Days.
2. **Medical Cost Exposure** — *Why it matters:* Hospitalisation costs are growing at 14% inflation annually; a single serious medical emergency can force liquidation of your long-term assets. *Recommended action:* Get an independent Health cover of ₹10L to ₹15L with restoration benefits. *Target:* 30 Days.
3. **Contingency Fund Shortfall** — *Current:* ${formatCurrency(inc.emergencyCorpus.toDoubleOrNull() ?: 0.0)} / *Target:* ${formatCurrency(scores.efTarget)}. *Why it matters:* Liquid cash is necessary to cover 6 months of expenses to avoid distress sale of investments. *Recommended action:* Park the remaining deficit in a sweep-in FD or Liquid Fund. *Target:* 45 Days.
4. **Estate Nominee Loose-ends** — *Why it matters:* Simply holding assets in mutual funds or bank deposits without explicit 'Complete' nomination status means complex legal inheritance challenges for heirs. *Recommended action:* Audit all folios and register complete nominees immediately. *Target:* 60 Days.

## 📊 SECTION-BY-SECTION DEEP DIVE

### 1. Portfolio & Investments
*   **Asset Allocation Analysis:** Your current assets total ${formatCurrency(scores.totalPortfolio)}. To meet your risk preference of *${getRiskLabel(prof.riskScore)}*, we recommend balancing your equity and fixed income allocations. Avoid concentration in illiquid plots or high-risk crypto.
*   **Recommendation:** Align 60% with growth assets (Mutual Funds, Equity) and 40% with high-quality debt (PPF, SGBs, high-safety corporate bonds) depending on tax brackets.

### 2. Protection & Insurance
*   **Term Cover Need:** Your recommended term sum assured is **${formatCurrency(scores.annualIncome * 15)}**. If you are using standard LIC endowment policies, discontinue or convert to paid-up and switch to low-cost pure Term plans.
*   **Medical Shield:** Secure a family-floater policy with unlimited restoration, no co-payment clauses, and zero room-rent sub-limits.

### 3. Emergency Fund
*   **Goal:** Maintain **${formatCurrency(scores.efTarget)}** strictly intact.
*   **Placement Strategy:** Keep ₹1,00,000 in your primary savings account and the rest in a sweep-in deposit earning higher interest with instantaneous recall convenience.

### 4. Retirement Planning
*   **Sizing the Need:** Desired index retirement budget is ${formatCurrency(desiredRetIncome)} in today's money. To maintain equivalent lifestyle in $yearsToRet years (accounting for 7% inflation), you'll need **${formatCurrency(futureMonthly)} every month**.
*   **Corpus Requirement:** This translates to a corpus of **${formatCurrency(corpusNeeded)}** at retirement.
*   **SIP Plan:** A monthly investment program of **${formatCurrency(sipNeeded)}** at 10% average compounded returns is required. Leverage EPF, PPF, and NPS to bridge this systematically.

### 5. Tax Optimisation
*   **Exclusion Maximisation:** Ensure you are utilising Section 80C up to ₹1.5L (using PPF or ELSS mutual funds).
*   **NPS Tax Boost:** Allocate ₹50,000 to NPS Tier-I to unlock additional tax savings under Section 80CCD(1B).
*   **Health Insurance Premium:** Maximise Section 80D deductions for self and parents.

### 6. Estate & Legacy
*   **Immediate Need:** Draft a simple registered Will. This eliminates future legal friction and ensures assets flow only as you intend.
*   **Nomination Checks:** Review and enforce complete nominee alignments on all bank accounts, demat accounts, and insurance forms.

## 🗓️ YOUR 90-DAY ACTION PLAN
1. **Week 1-2:** Request quotes for ₹1.5Cr pure Term plan and ₹15L medical family floater.
2. **Week 3-4:** Top-up your emergency liquid account to reach at least ${formatCurrency(scores.efTarget / 2)} of your target.
3. **Month 2:** Complete nominee declarations across all mutual fund holdings and register secondary nominees.
4. **Month 3:** Open an NPS account online via eNPS to claim Section 80CCD(1B) savings of ₹50k.
5. **Month 3:** Enquire about writing a simple, clean, legally enforceable Will.

## 💡 ANNUAL REVIEW CHECKLIST
✦ Re-align your term policy limits if your income jumps by 15% or more.
✦ Perform Portfolio rebalancing to shift surplus cash from equity back to fixed income.
✦ Verify all bank records and address verification for NRI status details.
✦ Check if any tax law adjustments demand shifting between Old to New Tax Regimes.

---
**Advisor Note:** Turning a roadmap into reality is a continuous partnership. Connect with **$adv.name** at **$adv.firm** (Call: **$adv.phone** or Email: **$adv.email**) to review these findings and establish your permanent financial prosperity plan!
        """.trimIndent()
    }

    private fun getRiskLabel(score: Int): String = when (score) {
        0 -> "Very Conservative"
        1 -> "Conservative"
        2 -> "Moderate"
        3 -> "Growth-oriented"
        else -> "Aggressive"
    }

    private fun getRiskDesc(score: Int): String = when (score) {
        0 -> "Capital safety over returns"
        1 -> "Steady income, minimal risk"
        2 -> "Balanced growth and safety"
        3 -> "Higher growth, tolerate some volatility"
        else -> "Maximum growth, high risk tolerance"
    }
}
