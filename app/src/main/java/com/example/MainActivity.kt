package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ScoreResults
import com.example.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    private val viewModel: WealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SlateDark
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        WealthApp(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun WealthApp(viewModel: WealthViewModel) {
    val currentStep by viewModel.currentStep.collectAsState()
    val scrollState = rememberScrollState()

    // Scroll to top on step changes automatically!
    LaunchedEffect(currentStep) {
        scrollState.animateScrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        // App Header Bar
        HeaderBar(viewModel)

        // Step Navigation Pills (hidden on Welcome and Results screens)
        if (currentStep != "welcome" && currentStep != "results") {
            StepNavigationBar(currentStep = currentStep, steps = viewModel.steps) { targetStep ->
                viewModel.navigateTo(targetStep)
            }
        }

        // Screen Body with animated fade transitions
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (currentStep) {
                "welcome" -> WelcomeScreen(viewModel)
                "profile" -> ProfileScreen(viewModel)
                "income" -> IncomeScreen(viewModel)
                "assets" -> AssetsScreen(viewModel)
                "protection" -> ProtectionScreen(viewModel)
                "retirement" -> RetirementScreen(viewModel)
                "estate" -> EstateScreen(viewModel)
                "goals" -> GoalsScreen(viewModel)
                "results" -> ResultsScreen(viewModel)
            }
        }
    }
}

@Composable
fun HeaderBar(viewModel: WealthViewModel) {
    val currentStep by viewModel.currentStep.collectAsState()
    val steps = viewModel.steps
    val stepIdx = steps.indexOf(currentStep)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(12.dp))
            .background(SlateSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drawn clean logo to replicate base64 logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldAccent)
                    .padding(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "FV India",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "WealthCanvas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldAccent,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "by",
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 8.sp)
                    )
                    Text(
                        text = "FV India",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
                Text(
                    text = "FINANCIAL HEALTH ASSESSMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontSize = 8.sp,
                        letterSpacing = 0.1.em
                    )
                )
            }
        }

        if (currentStep != "welcome" && currentStep != "results") {
            Text(
                text = "Step $stepIdx of ${steps.size - 2}",
                style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun StepNavigationBar(
    currentStep: String,
    steps: List<String>,
    onStepClick: (String) -> Unit
) {
    val cleanSteps = steps.filter { it != "welcome" && it != "results" }
    val currentIdx = cleanSteps.indexOf(currentStep)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cleanSteps.forEachIndexed { index, step ->
            val active = step == currentStep
            val done = index < currentIdx
            val labelToken = when (step) {
                "profile" -> "Profile"
                "income" -> "Income"
                "assets" -> "Assets"
                "protection" -> "Security"
                "retirement" -> "Retire"
                "estate" -> "Legacy"
                "goals" -> "Goals"
                else -> ""
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(if (active) 2.2f else 1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (active) GoldAccent else if (done) GoldAccent.copy(alpha = 0.5f) else SlateSurfaceVariant,
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        if (active) GoldAccent.copy(alpha = 0.15f)
                        else if (done) TealAccent.copy(alpha = 0.4f)
                        else SlateSurface
                    )
                    .clickable { onStepClick(step) }
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "0${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (active) GoldAccent else if (done) GoldAccent else MutedText,
                            fontSize = 9.sp
                        )
                    )
                    if (active) {
                        Text(
                            text = labelToken,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    if (done && !active) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = GoldAccent,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── CUSTOM REUSABLE COMPOSE CONTROLS ───────────────────────────────

@Composable
fun CardWrapper(
    modifier: Modifier = Modifier,
    borderAccent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(
            1.dp,
            if (borderAccent) GoldAccent.copy(alpha = 0.25f) else SlateSurfaceVariant
        ),
        content = content
    )
}

@Composable
fun PremiumField(
    labelText: String,
    tipText: String = "",
    inputField: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = labelText.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MutedText,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.05.em
                )
            )
            if (tipText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                TooltipWrapper(tipText)
            }
        }
        inputField()
    }
}

@Composable
fun TooltipWrapper(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(14.dp)
            .border(1.dp, MutedText, CircleShape)
    ) {
        Text(
            text = "?",
            color = MutedText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { /* Trivial tap response for mobile tooltips */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MutedText.copy(alpha = 0.6f), fontSize = 13.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, autoCorrectEnabled = false),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CreamText),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = SlateSurfaceVariant,
            focusedTextColor = CreamText,
            unfocusedTextColor = CreamText,
            focusedContainerColor = SlateDark,
            unfocusedContainerColor = SlateDark
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDropdown(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SlateDark,
                contentColor = CreamText
            ),
            border = BorderStroke(1.dp, SlateSurfaceVariant),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, color = CreamText, fontSize = 13.sp)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select option",
                    tint = MutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(SlateSurface)
                .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(6.dp))
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = CreamText, fontSize = 13.sp) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    rows: Int = 3
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MutedText.copy(alpha = 0.5f), fontSize = 13.sp) },
        singleLine = false,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CreamText),
        minLines = rows,
        maxLines = rows + 2,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = SlateSurfaceVariant,
            focusedTextColor = CreamText,
            focusedContainerColor = SlateDark,
            unfocusedContainerColor = SlateDark
        )
    )
}

@Composable
fun CustomGoalBadge(
    goal: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (selected) GoldAccent else SlateSurfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .background(if (selected) GoldAccent.copy(alpha = 0.15f) else SlateDark)
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Selected",
                    tint = GoldAccent,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = goal,
                color = if (selected) GoldAccent else MutedText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
            )
        }
    }
}

@Composable
fun FooterNavigationButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextLabel: String = "Continue →",
    generating: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBack,
            border = BorderStroke(1.dp, SlateSurfaceVariant),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedText)
        ) {
            Text("← Back", fontSize = 13.sp)
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (generating) SlateSurfaceVariant else GoldAccent,
                contentColor = if (generating) MutedText else SlateDark
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            enabled = !generating
        ) {
            Text(
                text = if (generating) "Generating..." else nextLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ScoreCircularArc(
    score: Int,
    size: Dp = 124.dp,
    label: String = "",
    sublabel: String = ""
) {
    val grade = getGrade(score)
    val sweepAngle = (score / 100f) * 180f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(size + 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .size(size)
                .padding(vertical = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 7.dp.toPx()
                val radius = (this.size.width - strokeWidth) / 2
                val topCenterOffset = Offset(strokeWidth / 2, strokeWidth / 2)

                // Background grey track
                drawArc(
                    color = SlateSurfaceVariant,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = topCenterOffset
                )

                // Foreground active arc
                drawArc(
                    color = grade.color,
                    startAngle = 180f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = topCenterOffset
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = size / 3f)
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = grade.color,
                        fontSize = (size.value / 6f).sp
                    )
                )
                Text(
                    text = grade.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = grade.color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (size.value / 14f).sp,
                        letterSpacing = 0.05.em
                    )
                )
            }
        }

        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = CreamText,
                    fontSize = 11.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (sublabel.isNotEmpty()) {
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 8.sp,
                    color = MutedText
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

data class Grade(val label: String, val color: Color)

fun getGrade(score: Int): Grade {
    return when {
        score >= 85 -> Grade("Excellent", GreenSuccess)
        score >= 70 -> Grade("Good", GoldAccent)
        score >= 50 -> Grade("Fair", Color(0xFFF2994A))
        else -> Grade("Alert", RedError)
    }
}

// ── SCREEN IMPLEMENTATIONS ─────────────────────────────────────────

@Composable
fun WelcomeScreen(viewModel: WealthViewModel) {
    val advisor by viewModel.advisor.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GoldAccent)
                .padding(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "WealthCanvas Logo",
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Text(
            text = "FV India presents",
            color = GoldAccent,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.2.em,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )

        Text(
            text = "WealthCanvas",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                color = GoldAccent,
                fontSize = 38.sp,
                letterSpacing = (-1.5).sp
            ),
            modifier = Modifier.padding(top = 2.dp)
        )

        Text(
            text = "Know where you stand. Know where you're headed.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = MutedText
            ),
            textAlign = TextAlign.Center
        )

        Divider(
            modifier = Modifier
                .width(60.dp)
                .padding(vertical = 12.dp),
            color = GoldAccent.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Text(
            text = "India's most comprehensive personal financial health assessment. Takes 8–10 minutes. No financial jargon. No sales pitch. Just an honest picture of your financial life — and a clear roadmap to make it better.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CreamText,
                lineHeight = 1.7.em
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Snapshot Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val grid = listOf(
                "7" to "Dimensions of Health",
                "100" to "Point Scoring Unit",
                "90" to "Day Clear Checklist",
                "0" to "Unsecured Jargon"
            )
            grid.forEach { (num, text) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                        .background(SlateSurface)
                        .padding(12.dp, 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = num,
                            color = GoldAccent,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = text,
                            color = MutedText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                lineHeight = 1.3.em
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Advisor parameters card
        CardWrapper(borderAccent = true) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Contact",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = advisor.firm,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = CreamText,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "\"${advisor.tagline}\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }

                Text(
                    text = "CONFIRM ADVISOR CONTACT DETAILS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PremiumField("Advisor Name") {
                    PremiumInput(
                        value = advisor.name,
                        onValueChange = { viewModel.updateAdvisor { a -> a.copy(name = it) } },
                        placeholder = "Advisor Name"
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Phone") {
                            PremiumInput(
                                value = advisor.phone,
                                onValueChange = { viewModel.updateAdvisor { a -> a.copy(phone = it) } },
                                placeholder = "Ph Number",
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Email") {
                            PremiumInput(
                                value = advisor.email,
                                onValueChange = { viewModel.updateAdvisor { a -> a.copy(email = it) } },
                                placeholder = "Email Addr"
                            )
                        }
                    }
                }

                PremiumField("Advisor Tagline / Motto") {
                    PremiumInput(
                        value = advisor.tagline,
                        onValueChange = { viewModel.updateAdvisor { a -> a.copy(tagline = it) } },
                        placeholder = "e.g. Trust. Transparency. Prosperity."
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.navigateTo("profile") },
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = SlateDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("begin_assessment_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Begin My WealthCanvas Assessment →", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(
            text = "Your inputs stay strictly in your device cache. Nothing is stored externally or shared.",
            color = MutedText,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun ProfileScreen(viewModel: WealthViewModel) {
    val prof by viewModel.profile.collectAsState()
    val isMarried = prof.maritalStatus == "Married"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "About You",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "Let's formulate the basics — who you are and where you stay shapes everything about your fiscal journey.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PremiumField("Full Name") {
            PremiumInput(
                value = prof.name,
                onValueChange = { viewModel.updateProfile { p -> p.copy(name = it) } },
                placeholder = "As you'd like to be addressed",
                testTag = "username_input"
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Your Age") {
                    PremiumInput(
                        value = prof.age,
                        onValueChange = { viewModel.updateProfile { p -> p.copy(age = it) } },
                        placeholder = "Years",
                        keyboardType = KeyboardType.Number
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("City") {
                    PremiumInput(
                        value = prof.city,
                        onValueChange = { viewModel.updateProfile { p -> p.copy(city = it) } },
                        placeholder = "e.g. Mumbai, New Delhi"
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1.3f)) {
                PremiumField("Residency Status") {
                    PremiumDropdown(
                        value = prof.residency,
                        options = listOf(
                            "Indian Resident",
                            "NRI – USA",
                            "NRI – UAE",
                            "NRI – UK",
                            "NRI – Singapore",
                            "NRI – Canada",
                            "NRI – Australia",
                            "NRI – Other"
                        ),
                        onSelect = { viewModel.updateProfile { p -> p.copy(residency = it) } }
                    )
                }
            }
            Box(modifier = Modifier.weight(1.1f)) {
                PremiumField("Marital Status") {
                    PremiumDropdown(
                        value = prof.maritalStatus,
                        options = listOf("Single", "Married", "Divorced", "Widowed"),
                        onSelect = { viewModel.updateProfile { p -> p.copy(maritalStatus = it) } }
                    )
                }
            }
        }

        PremiumField("Your Occupation") {
            PremiumDropdown(
                value = prof.occupation,
                options = listOf(
                    "Salaried – Private Sector",
                    "Salaried – Government / PSU",
                    "Self-Employed / Business Owner",
                    "Professional (Doctor / CA / Lawyer)",
                    "NRI – Employed Abroad",
                    "Homemaker",
                    "Retired",
                    "Investor / HNI",
                    "Freelancer / Consultant"
                ),
                onSelect = { viewModel.updateProfile { p -> p.copy(occupation = it) } }
            )
        }

        if (isMarried) {
            Divider(color = SlateSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = "Spouse & Family Details",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    color = GoldAccent
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1.5f)) {
                    PremiumField("Spouse Name") {
                        PremiumInput(
                            value = prof.spouseName,
                            onValueChange = { viewModel.updateProfile { p -> p.copy(spouseName = it) } },
                            placeholder = "Partner's Name"
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    PremiumField("Spouse Age") {
                        PremiumInput(
                            value = prof.spouseAge,
                            onValueChange = { viewModel.updateProfile { p -> p.copy(spouseAge = it) } },
                            placeholder = "Years",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1.3f)) {
                    PremiumField("Spouse Occupation") {
                        PremiumDropdown(
                            value = prof.spouseOccupation.ifEmpty { "Select" },
                            options = listOf(
                                "Select",
                                "Salaried – Private Sector",
                                "Salaried – Government / PSU",
                                "Self-Employed / Business Owner",
                                "Professional (Doctor / CA / Lawyer)",
                                "NRI – Employed Abroad",
                                "Homemaker",
                                "Retired"
                            ),
                            onSelect = { viewModel.updateProfile { p -> p.copy(spouseOccupation = it) } }
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    PremiumField("Num of Dependents", "Children, elderly parents under protection") {
                        PremiumInput(
                            value = prof.dependents,
                            onValueChange = { viewModel.updateProfile { p -> p.copy(dependents = it) } },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        } else {
            PremiumField("Number of Dependents") {
                PremiumInput(
                    value = prof.dependents,
                    onValueChange = { viewModel.updateProfile { p -> p.copy(dependents = it) } },
                    placeholder = "0",
                    keyboardType = KeyboardType.Number
                )
            }
        }

        Divider(color = SlateSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

        // Risk Appetite Selector
        CardWrapper {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "HOW COMFORTABLE ARE YOU WITH INVESTMENT RISK?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val riskOptions = listOf("Very Conservative", "Conservative", "Moderate", "Growth-oriented", "Aggressive")
                val riskDescs = listOf(
                    "Capital safety over returns",
                    "Steady income, minimal risk",
                    "Balanced growth and safety",
                    "Higher growth, tolerate some volatility",
                    "Maximum growth, high risk tolerance"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    riskOptions.forEachIndexed { idx, label ->
                        val active = prof.riskScore == idx
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (active) GoldAccent else SlateSurfaceVariant, RoundedCornerShape(6.dp))
                                .background(if (active) GoldAccent.copy(alpha = 0.15f) else SlateDark)
                                .clickable { viewModel.updateProfile { p -> p.copy(riskScore = idx) } }
                                .padding(12.dp, 8.dp)
                        ) {
                            RadioButton(
                                selected = active,
                                onClick = { viewModel.updateProfile { p -> p.copy(riskScore = idx) } },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GoldAccent,
                                    unselectedColor = MutedText
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(label, color = if (active) GoldAccent else CreamText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(riskDescs[idx], color = MutedText, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("welcome") },
            onNext = { viewModel.navigateNext() }
        )
    }
}

@Composable
fun IncomeScreen(viewModel: WealthViewModel) {
    val inc by viewModel.income.collectAsState()
    val prof by viewModel.profile.collectAsState()
    val isMarried = prof.maritalStatus == "Married"

    // Scores for UI visual feedback
    val scores = viewModel.computeScores()
    val annualIncomeVal = inc.annual.toDoubleOrNull() ?: 0.0
    val savingsVal = inc.savings.toDoubleOrNull() ?: 0.0
    val emiVal = inc.emi.toDoubleOrNull() ?: 0.0

    val savRate = if (annualIncomeVal > 0) ((savingsVal * 12) / annualIncomeVal * 100).toInt() else 0
    val emiRatio = if (annualIncomeVal > 0) ((emiVal * 12) / annualIncomeVal * 100).toInt() else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Your Money Flow",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "Understanding your income, spending, and monthly savings tells us how much cash surplus you generate.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Annual Income (Gross)", "Yearly direct earnings before taxes") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = inc.annual,
                            onValueChange = { viewModel.updateIncome { i -> i.copy(annual = it) } },
                            placeholder = "e.g. 1500000",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
            if (isMarried) {
                Box(modifier = Modifier.weight(1f)) {
                    PremiumField("Spouse Annual Income", "Gross partner earnings") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹ ", color = MutedText, fontSize = 14.sp)
                            PremiumInput(
                                value = inc.spouseAnnual,
                                onValueChange = { viewModel.updateIncome { i -> i.copy(spouseAnnual = it) } },
                                placeholder = "0 if homemaker",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Monthly Expenditures", "Total regular household spending") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = inc.monthlyExp,
                            onValueChange = { viewModel.updateIncome { i -> i.copy(monthlyExp = it) } },
                            placeholder = "e.g. 80000",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Monthly Savings Rate", "Standard recurring investment amount") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = inc.savings,
                            onValueChange = { viewModel.updateIncome { i -> i.copy(savings = it) } },
                            placeholder = "e.g. 25000",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Total Outstanding Debt", "Sum of home, auto, and personal loans") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = inc.totalDebt,
                            onValueChange = { viewModel.updateIncome { i -> i.copy(totalDebt = it) } },
                            placeholder = "0 if none",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Monthly EMIs combined", "Total loan installment outflows") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = inc.emi,
                            onValueChange = { viewModel.updateIncome { i -> i.copy(emi = it) } },
                            placeholder = "0 if none",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }

        // Live Income Quick Metrics
        if (annualIncomeVal > 0) {
            CardWrapper {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "YOUR CASH FLOW METRICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.em
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Monthly Disposable
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDark, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("EST. MONTHLY VALUE", color = MutedText, fontSize = 8.sp)
                                Text(
                                    text = viewModel.formatCurrency(annualIncomeVal / 12),
                                    color = CreamText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Before taxes/deductions", color = MutedText, fontSize = 9.sp)
                            }
                        }

                        // Metric 2: Savings Rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDark, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("SAVINGS RATE", color = MutedText, fontSize = 8.sp)
                                Text(
                                    text = "$savRate%",
                                    color = if (savRate >= 20) GreenSuccess else if (savRate >= 10) GoldAccent else RedError,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (savRate >= 20) "✓ Healthy rate" else "⚠ Focus to improve",
                                    color = if (savRate >= 20) GreenSuccess else MutedText,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Metric 3: Debt-to-income
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDark, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("EMI RATIO", color = MutedText, fontSize = 8.sp)
                                Text(
                                    text = "$emiRatio%",
                                    color = if (emiRatio <= 35) GreenSuccess else if (emiRatio <= 50) GoldAccent else RedError,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (emiRatio <= 35) "✓ Highly secure" else "⚠ High liability",
                                    color = MutedText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(color = SlateSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

        // Emergency Fund Card
        CardWrapper {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "EMERGENCY FUND / CONTINGENCY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                PremiumField("Emergency Corpus Amount", "Backup liquid funds stored separate from equity") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = inc.emergencyCorpus,
                            onValueChange = { viewModel.updateIncome { i -> i.copy(emergencyCorpus = it) } },
                            placeholder = "e.g. 300000",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Target Duration (Months)") {
                            PremiumDropdown(
                                value = "${inc.efMonths} Months",
                                options = listOf("3 Months", "6 Months", "9 Months", "12 Months"),
                                onSelect = {
                                    val cleaned = it.replace(" Months", "")
                                    viewModel.updateIncome { i -> i.copy(efMonths = cleaned) }
                                }
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1.3f)) {
                        PremiumField("Asset Parking Location") {
                            PremiumDropdown(
                                value = inc.savingsWhere,
                                options = listOf(
                                    "Savings Account",
                                    "Sweep-in FD",
                                    "Liquid Mutual Fund",
                                    "Not set aside yet",
                                    "Spread across multiple bank accounts"
                                ),
                                onSelect = { viewModel.updateIncome { i -> i.copy(savingsWhere = it) } }
                            )
                        }
                    }
                }

                // Contingency funding progress bar
                if (scores.monthlyExp > 0 && inc.emergencyCorpus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Emergency fund coverage: ${viewModel.formatCurrency(scores.efTarget)} target",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${scores.efPct}% funded",
                                color = if (scores.efPct >= 100) GreenSuccess else if (scores.efPct >= 50) GoldAccent else RedError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = (scores.efPct / 100f).coerceAtMost(1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (scores.efPct >= 100) GreenSuccess else if (scores.efPct >= 50) GoldAccent else RedError,
                            trackColor = SlateDark
                        )
                    }
                }
            }
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("profile") },
            onNext = { viewModel.navigateNext() }
        )
    }
}

// Data holder representing Asset checklist
data class AssetCategoryData(
    val title: String,
    val color: Color,
    val nriOnly: Boolean = false,
    val items: List<AssetItemData>
)

data class AssetItemData(
    val id: String,
    val label: String,
    val tip: String,
    val nriOk: Boolean
)

@Composable
fun AssetsScreen(viewModel: WealthViewModel) {
    val currentAssets by viewModel.assets.collectAsState()
    val assetValues by viewModel.assetValues.collectAsState()
    val prof by viewModel.profile.collectAsState()
    val isNRI = prof.residency != "Indian Resident"

    val scores = viewModel.computeScores()
    val selectedCount = currentAssets.filter { it.value }.size

    // Static Asset list compiled to match JS constants
    val categories = listOf(
        AssetCategoryData(
            "Equity & Growth", GoldAccent, items = listOf(
                AssetItemData("direct_eq", "Direct Stocks", "Shares listed on NSE/BSE and held in Demat", true),
                AssetItemData("eq_mf", "Equity Mutual Funds", "Active and passive mutual funds", true),
                AssetItemData("etf", "ETFs & Index Funds", "Index funds/Exchange Traded Funds", true),
                AssetItemData("pms", "PMS (Portfolio Management)", "Professional high-ticket portfolio (min ₹50L)", true),
                AssetItemData("aif", "AIF / Alternatives", "Alternate Asset Funds (min ₹1Cr)", true),
                AssetItemData("esop", "ESOPs / RSUs", "Vested or unvested company stocks", true),
                AssetItemData("unlisted", "Pre-IPO / Private Shares", "Investment in startups or unlisted shares", false),
                AssetItemData("intl_eq", "International Equities", "US/foreign listed shares", false)
            )
        ),
        AssetCategoryData(
            "Fixed Income & Debt", Color(0xFF3F51B5), items = listOf(
                AssetItemData("bank_fd", "Bank Deposits / FDs", "Regular or sweep FDs. NRE/NRO FDs for NRIs", true),
                AssetItemData("corp_bonds", "Corporate Bonds / NCDs", "High yield debentures, secure bonds", true),
                AssetItemData("debt_mf", "Debt Mutual Funds", "Liquid or target maturity debt funds", true),
                AssetItemData("govt_sec", "Govt Securities / G-Secs", "T-Bills, sovereign debts via RBI retail", false),
                AssetItemData("rbi_bonds", "RBI Floating Rate Bonds", "Safe floating yield sovereign bonds", false),
                AssetItemData("ppf_fi", "PPF", "15-year tax-free lock-in, open to residents only", false),
                AssetItemData("nsc_kvp", "Postal Schemes", "NSC, Senior citizens investments", false),
                AssetItemData("sgb", "Sovereign Gold Bonds", "Government-backed gold bonds", false)
            )
        ),
        AssetCategoryData(
            "Real Estate", Color(0xFF915822), items = listOf(
                AssetItemData("res_prop", "Residential Property", "Value of personal or rental apartments", true),
                AssetItemData("com_prop", "Commercial Real Estate", "Shops, workspace, warehouses owned", true),
                AssetItemData("reit", "REITs / InvITs", "Real estate or infrastructure trust shares", true),
                AssetItemData("plot", "Plots / Land", "Plots of residential lands owned", true),
                AssetItemData("agri", "Agricultural Land", "Non-repatriatable farmland assets", false)
            )
        ),
        AssetCategoryData(
            "Gold & Commodities", Color(0xFF9E7000), items = listOf(
                AssetItemData("phys_gold", "Physical Gold / Coins", "Jewellery, bars, ornaments held", true),
                AssetItemData("gold_etf", "Gold ETFs / Digital Gold", "Gold tracked in secure digital units", true),
                AssetItemData("silver", "Silver & Metal Commodities", "Silver bars, ETFs", true)
            )
        ),
        AssetCategoryData(
            "NRI-Specific Assets", Color(0xFF007791), nriOnly = true, items = listOf(
                AssetItemData("nre_acc", "NRE Interest Deposits", "完全可汇入、利息免税外汇帐户", true),
                AssetItemData("nro_acc", "NRO Interest Accounts", "For managing domestic Indian rental income", true),
                AssetItemData("fcnr", "FCNR(B) Foreign Deposits", "Currency fluctuation risk protection deposits", true),
                AssetItemData("overseas_eq", "Overseas 401k / Portfolios", "Assets settled/held abroad", true)
            )
        ),
        AssetCategoryData(
            "Retirement Portals", Color(0xFFB22E2E), items = listOf(
                AssetItemData("nps_a", "NPS Pension Funds", "Government scheme under corporate or self-tier", true),
                AssetItemData("epf_a", "EPF / Provident Fund", "Standard corporate retirement balances", false),
                AssetItemData("pension_plan", "Private Annuity Plans", "Pension guarantees with insurers", true)
            )
        ),
        AssetCategoryData(
            "Alternative & Digital", Color(0xFF512DA8), items = listOf(
                AssetItemData("crypto", "Digital Crypto Coins", "Bitcoins, stablecoins holdings", true),
                AssetItemData("p2p", "P2P Credit Platforms", "Yield lending via regulated platforms", false),
                AssetItemData("startup", "Angel Startups", "Equity stake in early-stage startups", false),
                AssetItemData("art", "Fine Arts & Collectibles", "Luxury tangible alternative items", false)
            )
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Assets & Investments",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "Check everything you currently own or invest in. Put approximate valuations — estimates work fine for now.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isNRI) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldAccent.copy(alpha = 0.12f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "✦ NRI profile enabled — restricted investments under FEMA represent grey indicators. NRI-eligible instruments are fully noted.",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    lineHeight = 1.5.em
                )
            }
        }

        // Checklist statistics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SlateSurface)
                .padding(14.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$selectedCount asset types accounted", color = MutedText, fontSize = 12.sp)
            Text(
                text = "Total Assets: ${viewModel.formatCurrency(scores.totalPortfolio)}",
                color = GoldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }

        // Loop through categories
        categories.forEach { cat ->
            if (cat.nriOnly && !isNRI) return@forEach

            CardWrapper {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Category title indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cat.color)
                        )
                        Text(
                            text = cat.title,
                            color = cat.color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.03.em
                        )
                    }

                    // Asset Checklist items
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cat.items.forEach { item ->
                            val restricted = isNRI && !item.nriOk
                            val checked = currentAssets[item.id] == true

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(7.dp))
                                    .border(
                                        1.dp,
                                        if (checked) GoldAccent.copy(alpha = 0.4f) else SlateSurfaceVariant,
                                        RoundedCornerShape(7.dp)
                                    )
                                    .background(if (checked) GoldAccent.copy(alpha = 0.05f) else SlateDark)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            if (!restricted) {
                                                viewModel.toggleAsset(item.id, isChecked)
                                            }
                                        },
                                        enabled = !restricted,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GoldAccent,
                                            uncheckedColor = MutedText
                                        )
                                    )

                                    Column(modifier = Modifier.fillMaxWidth(if (checked) 0.55f else 0.95f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.label,
                                                color = if (restricted) MutedText.copy(alpha = 0.5f) else CreamText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (restricted) {
                                                Text(
                                                    text = "Restricted",
                                                    color = RedError,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .background(RedError.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            if (!restricted && isNRI && item.nriOk) {
                                                Text(
                                                    text = "NRI Ok",
                                                    color = GreenSuccess,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .background(GreenSuccess.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = item.tip,
                                            color = MutedText,
                                            fontSize = 10.sp,
                                            lineHeight = 1.3.em
                                        )
                                    }
                                }

                                if (checked) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.width(110.dp)
                                    ) {
                                        Text("₹", color = MutedText, fontSize = 12.sp)
                                        OutlinedTextField(
                                            value = assetValues[item.id] ?: "",
                                            onValueChange = { viewModel.updateAssetValue(item.id, it) },
                                            placeholder = { Text("Approx Value", color = MutedText.copy(alpha = 0.4f), fontSize = 11.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall.copy(color = CreamText, fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GoldAccent,
                                                unfocusedBorderColor = SlateSurfaceVariant,
                                                focusedTextColor = CreamText,
                                                unfocusedTextColor = CreamText,
                                                focusedContainerColor = SlateDark,
                                                unfocusedContainerColor = SlateDark
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("income") },
            onNext = { viewModel.navigateNext() }
        )
    }
}

// Data holder facilitating the Insurance plan selectors
data class InsuranceUIData(
    val hasKey: String,
    val saKey: String,
    val label: String,
    val rIcon: String,
    val ideal: String,
    val tip: String
)

@Composable
fun ProtectionScreen(viewModel: WealthViewModel) {
    val prot by viewModel.protection.collectAsState()
    val inc by viewModel.income.collectAsState()
    val annualIncomeVal = inc.annual.toDoubleOrNull() ?: 0.0

    // List mapping matching JS array
    val insurances = listOf(
        InsuranceUIData("hasTerm", "termSA", "Term Life Insurance", "🛡️", "10–15× annual income", "Pure financial payload protection for your direct dependents."),
        InsuranceUIData("hasHealth", "healthSA", "Health / Mediclaim policy", "🏥", "₹10L–₹50L per family", "Covers unplanned extreme hospitalisation costs."),
        InsuranceUIData("hasCI", "ciSA", "Critical Illness Cover", "💊", "₹20L–₹50L minimum", "Pays single lump sum on diagnosis of cancer, strokes, etc."),
        InsuranceUIData("hasAccident", "accidentSA", "Personal Accident policy", "🚑", "10× annual income", "Affordable cover against permanent/partial disabilities."),
        InsuranceUIData("hasProperty", "", "Home / Property Cover", "🏠", "Full replacement valuation", "Protects physical structures from fire, floods, or thefts.")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Protection & Insurance",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "Insurance isn't a wealth accumulation device — it's structural shield. Let's inspect if your family is medically and financially guarded.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        insurances.forEach { ins ->
            val hasIt = when (ins.hasKey) {
                "hasTerm" -> prot.hasTerm == "Yes"
                "hasHealth" -> prot.hasHealth == "Yes"
                "hasCI" -> prot.hasCI == "Yes"
                "hasAccident" -> prot.hasAccident == "Yes"
                else -> prot.hasProperty == "Yes"
            }

            val curSA = when (ins.saKey) {
                "termSA" -> prot.termSA
                "healthSA" -> prot.healthSA
                "ciSA" -> prot.ciSA
                "accidentSA" -> prot.accidentSA
                else -> ""
            }

            CardWrapper(borderAccent = hasIt) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(ins.rIcon, fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ins.label,
                                color = CreamText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Yes No trigger buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("No", "Yes").forEach { choice ->
                                    val choiceActive = when (ins.hasKey) {
                                        "hasTerm" -> prot.hasTerm == choice
                                        "hasHealth" -> prot.hasHealth == choice
                                        "hasCI" -> prot.hasCI == choice
                                        "hasAccident" -> prot.hasAccident == choice
                                        else -> prot.hasProperty == choice
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(
                                                1.dp,
                                                if (choiceActive) (if (choice == "Yes") GoldAccent else RedError) else SlateSurfaceVariant,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .background(
                                                if (choiceActive) (if (choice == "Yes") GoldAccent.copy(alpha = 0.15f) else RedError.copy(alpha = 0.15f))
                                                else SlateDark
                                            )
                                            .clickable {
                                                viewModel.updateProtection { p ->
                                                    when (ins.hasKey) {
                                                        "hasTerm" -> p.copy(hasTerm = choice)
                                                        "hasHealth" -> p.copy(hasHealth = choice)
                                                        "hasCI" -> p.copy(hasCI = choice)
                                                        "hasAccident" -> p.copy(hasAccident = choice)
                                                        else -> p.copy(hasProperty = choice)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(choice, color = if (choiceActive) (if (choice == "Yes") GoldAccent else RedError) else MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Text(
                            text = ins.tip,
                            color = MutedText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        val idealLabelText = if (annualIncomeVal > 0 && ins.hasKey == "hasTerm") {
                            "Recommended: ${ins.ideal} (e.g. at least ${viewModel.formatCurrency(annualIncomeVal * 12)})"
                        } else {
                            "Recommended: ${ins.ideal}"
                        }
                        Text(
                            text = idealLabelText,
                            color = GoldAccent.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // If user has it, and it wants Sum Assured value
                        if (hasIt && ins.saKey.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            PremiumField("Sum Insured") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹ ", color = MutedText, fontSize = 13.sp)
                                    PremiumInput(
                                        value = curSA,
                                        onValueChange = { inputVal ->
                                            viewModel.updateProtection { p ->
                                                when (ins.saKey) {
                                                    "termSA" -> p.copy(termSA = inputVal)
                                                    "healthSA" -> p.copy(healthSA = inputVal)
                                                    "ciSA" -> p.copy(ciSA = inputVal)
                                                    else -> p.copy(accidentSA = inputVal)
                                                }
                                            }
                                        },
                                        placeholder = "Sum Assured Value",
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                            }

                            // Dynamic check
                            val parsedSA = curSA.toDoubleOrNull() ?: 0.0
                            if (ins.hasKey == "hasTerm" && annualIncomeVal > 0 && parsedSA > 0) {
                                val enoughCover = parsedSA >= annualIncomeVal * 10
                                Text(
                                    text = if (enoughCover) "✓ Adequate protection level registered" else "⚠ Sum Insured lower than recommended bounds (min ₹${viewModel.formatCurrency(annualIncomeVal * 12)})",
                                    color = if (enoughCover) GreenSuccess else RedError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("assets") },
            onNext = { viewModel.navigateNext() }
        )
    }
}

@Composable
fun RetirementScreen(viewModel: WealthViewModel) {
    val ret by viewModel.retirement.collectAsState()
    val prof by viewModel.profile.collectAsState()

    val age = prof.age.toIntOrNull() ?: 35
    val targetRetireAge = ret.retireAge.toIntOrNull() ?: 60
    val yearsToRetire = max(1, targetRetireAge - age)

    val desiredRetireMonthlyVal = ret.retireMonthly.toDoubleOrNull() ?: 0.0
    // Live calculations reflecting JS code
    val futureMonthly = desiredRetireMonthlyVal * 1.07.pow(yearsToRetire)
    val corpusNeeded = (futureMonthly * 12) / 0.04
    val sipNeeded = corpusNeeded / (((1.10.pow(yearsToRetire) - 1) / 0.10) * 12)

    val retInstruments = listOf(
        Triple("hasNPS", "npsCorpus", "National Pension System (NPS)"),
        Triple("hasEPF", "epfBalance", "Employee Provident Fund (EPF)"),
        Triple("hasPPF", "ppfBalance", "Public Provident Fund (PPF)"),
        Triple("hasGratuity", "gratuityEst", "Accrued Gratuity expectations")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Retirement Planning",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "Your future self is fully reliant on decisions you formulate today. Let's run retirement projections.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Retirement Goal Age") {
                    PremiumDropdown(
                        value = "${ret.retireAge} Years",
                        options = listOf("45 Years", "50 Years", "55 Years", "58 Years", "60 Years", "65 Years", "70 Years"),
                        onSelect = {
                            val parsed = it.replace(" Years", "")
                            viewModel.updateRetirement { r -> r.copy(retireAge = parsed) }
                        }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                PremiumField("Years left to work") {
                    OutlinedTextField(
                        value = "$yearsToRetire Years",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = GoldAccent, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SlateSurfaceVariant,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        PremiumField("Desired Monthly Pension Buffer", "In today's value — we automatically compound it for 7% local inflation") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹ ", color = MutedText, fontSize = 14.sp)
                PremiumInput(
                    value = ret.retireMonthly,
                    onValueChange = { viewModel.updateRetirement { r -> r.copy(retireMonthly = it) } },
                    placeholder = "e.g. 100000",
                    keyboardType = KeyboardType.Number
                )
            }
        }

        // Calculations visual card
        if (desiredRetireMonthlyVal > 0) {
            CardWrapper(borderAccent = true) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MATHEMATICAL RETIREMENT CORNER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.em
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Monthly Future Need
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDark, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("FUTURE MO. BUDGET", color = MutedText, fontSize = 8.sp)
                                Text(
                                    text = viewModel.formatCurrency(futureMonthly),
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("At 7% inflation rate", color = MutedText, fontSize = 8.sp)
                            }
                        }

                        // Metric 2: Total corpus
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDark, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("REQUIRED TARGET CORPUS", color = MutedText, fontSize = 8.sp)
                                Text(
                                    text = viewModel.formatCurrency(corpusNeeded),
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("At 4% secure SWR", color = MutedText, fontSize = 8.sp)
                            }
                        }

                        // Metric 3: SIP needed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDark, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("ESTIMATED MONTHLY SIP", color = MutedText, fontSize = 8.sp)
                                Text(
                                    text = viewModel.formatCurrency(sipNeeded),
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Compounded at 10% p.a.", color = MutedText, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }

        // Retirement Channels
        CardWrapper {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "YOUR RETIREMENT SAVING CHANNELS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    )
                )

                retInstruments.forEach { (hasKey, corpusKey, labelText) ->
                    val hasIt = when (hasKey) {
                        "hasNPS" -> ret.hasNPS == "Yes"
                        "hasEPF" -> ret.hasEPF == "Yes"
                        "hasPPF" -> ret.hasPPF == "Yes"
                        else -> ret.hasGratuity == "Yes"
                    }

                    val balanceVal = when (corpusKey) {
                        "npsCorpus" -> ret.npsCorpus
                        "epfBalance" -> ret.epfBalance
                        "ppfBalance" -> ret.ppfBalance
                        else -> ret.gratuityEst
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(labelText, color = CreamText, fontSize = 12.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("No", "Yes").forEach { choice ->
                                    val active = when (hasKey) {
                                        "hasNPS" -> ret.hasNPS == choice
                                        "hasEPF" -> ret.hasEPF == choice
                                        "hasPPF" -> ret.hasPPF == choice
                                        else -> ret.hasGratuity == choice
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .border(
                                                1.dp,
                                                if (active) GoldAccent else SlateSurfaceVariant,
                                                RoundedCornerShape(3.dp)
                                            )
                                            .background(if (active) GoldAccent.copy(alpha = 0.15f) else SlateDark)
                                            .clickable {
                                                viewModel.updateRetirement { r ->
                                                    when (hasKey) {
                                                        "hasNPS" -> r.copy(hasNPS = choice)
                                                        "hasEPF" -> r.copy(hasEPF = choice)
                                                        "hasPPF" -> r.copy(hasPPF = choice)
                                                        else -> r.copy(hasGratuity = choice)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(choice, color = if (active) GoldAccent else MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (hasIt) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Balance:", color = MutedText, fontSize = 11.sp, modifier = Modifier.width(54.dp))
                                Text("₹", color = MutedText, fontSize = 13.sp)
                                PremiumInput(
                                    value = balanceVal,
                                    onValueChange = { inputVal ->
                                        viewModel.updateRetirement { r ->
                                            when (corpusKey) {
                                                "npsCorpus" -> r.copy(npsCorpus = inputVal)
                                                "epfBalance" -> r.copy(epfBalance = inputVal)
                                                "ppfBalance" -> r.copy(ppfBalance = inputVal)
                                                else -> r.copy(gratuityEst = inputVal)
                                            }
                                        }
                                    },
                                    placeholder = "Current corpus amount",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }
                    }
                }
            }
        }

        PremiumField("Retirement Allocation Notes") {
            PremiumTextarea(
                value = ret.retireNotes,
                onValueChange = { viewModel.updateRetirement { r -> r.copy(retireNotes = it) } },
                placeholder = "Add notes e.g., plan to liquidate property, expecting legacy assets, etc."
            )
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("protection") },
            onNext = { viewModel.navigateNext() }
        )
    }
}

@Composable
fun EstateScreen(viewModel: WealthViewModel) {
    val est by viewModel.estate.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Estate & Legacy",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "The most loving financial act represents protecting your heirs from complex legal loops. Succession planning checks out nominees.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        CardWrapper {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "WILL & REGISTRY SUCCESSION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                PremiumField("Do you currently hold a Will?", "A legally enforceable Will outlines your asset division.") {
                    PremiumDropdown(
                        value = est.willStatus,
                        options = listOf(
                            "No Will",
                            "Will in progress",
                            "Will executed (registered)",
                            "Will executed (unregistered)",
                            "Trust created in lieu"
                        ),
                        onSelect = { viewModel.updateEstate { e -> e.copy(willStatus = it) } }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Nominations Checklist", "Verify if nominees are registered on all accounts") {
                            PremiumDropdown(
                                value = est.nominations,
                                options = listOf("None", "Partial", "Complete"),
                                onSelect = { viewModel.updateEstate { e -> e.copy(nominations = it) } }
                            )
                        }
                    }

                    if (est.nominations == "Partial") {
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumField("Where are nominees missing?") {
                                PremiumInput(
                                    value = est.nominationGaps,
                                    onValueChange = { viewModel.updateEstate { e -> e.copy(nominationGaps = it) } },
                                    placeholder = "e.g., Demat A/C, FD"
                                )
                            }
                        }
                    }
                }
            }
        }

        CardWrapper {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "STRUCTURES AND DOCUMENTS Check",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Power of Attorney (PoA)") {
                            PremiumDropdown(
                                value = est.poa,
                                options = listOf("No", "Yes – General PoA", "Yes – Specific PoA", "Yes – Durable PoA"),
                                onSelect = { viewModel.updateEstate { e -> e.copy(poa = it) } }
                            )
                        }
                    }
                    if (est.poa != "No") {
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumField("PoA Holder Name") {
                                PremiumInput(
                                    value = est.poaHolder,
                                    onValueChange = { viewModel.updateEstate { e -> e.copy(poaHolder = it) } },
                                    placeholder = "Full Name"
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Private Trusted Setup?") {
                            PremiumDropdown(
                                value = est.trust,
                                options = listOf("No", "Yes – Private Trust", "Yes – Irrevocable Trust"),
                                onSelect = { viewModel.updateEstate { e -> e.copy(trust = it) } }
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("HUF Registered?") {
                            PremiumDropdown(
                                value = est.huf,
                                options = listOf("No", "Yes – Active", "Yes – Dissolved", "Not Applicable"),
                                onSelect = { viewModel.updateEstate { e -> e.copy(huf = it) } }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1.3f)) {
                        PremiumField("Family Business Succession") {
                            PremiumDropdown(
                                value = est.familyBusiness,
                                options = listOf("No", "Yes – Plan exists", "Yes – No succession plan"),
                                onSelect = { viewModel.updateEstate { e -> e.copy(familyBusiness = it) } }
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumField("Bank Safety Locker?") {
                            PremiumDropdown(
                                value = est.locker,
                                options = listOf("No", "Yes"),
                                onSelect = { viewModel.updateEstate { e -> e.copy(locker = it) } }
                            )
                        }
                    }
                }

                PremiumField("Liabilities Outstanding", "Total debt values to solve for via term asset") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹ ", color = MutedText, fontSize = 14.sp)
                        PremiumInput(
                            value = est.liabilitiesAmt,
                            onValueChange = { viewModel.updateEstate { e -> e.copy(liabilitiesAmt = it) } },
                            placeholder = "0 if none",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                PremiumField("Digital assets accounted for?", "Secure notes on cryptos, drives, cloud data lockers") {
                    PremiumTextarea(
                        value = est.digitalAssets,
                        onValueChange = { viewModel.updateEstate { e -> e.copy(digitalAssets = it) } },
                        placeholder = "e.g., Ledger cryptos, important secure document vaults details"
                    )
                }

                PremiumField("Estate Planning Notes") {
                    PremiumTextarea(
                        value = est.estateNotes,
                        onValueChange = { viewModel.updateEstate { e -> e.copy(estateNotes = it) } },
                        placeholder = "Special circumstances, overseas asset holdings, guardianship arrangements etc."
                    )
                }
            }
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("retirement") },
            onNext = { viewModel.navigateNext() }
        )
    }
}

@Composable
fun GoalsScreen(viewModel: WealthViewModel) {
    val goalsVal by viewModel.goals.collectAsState()
    val generating by viewModel.generating.collectAsState()

    val goalsList = listOf(
        "Children's Education", "Child's Marriage", "Buying a Home", "Buying a Car",
        "International Vacation", "Starting a Business", "Early Retirement", "Passive Income",
        "Wealth Creation", "Charitable / Philanthropy", "Parent's Healthcare", "Debt Freedom",
        "NRI Return to India Plan", "Second Home", "Legacy / Inheritance"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Your Goals & Dreams",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "What exactly are you deploying your saved capital for? Select everything that aligns with your timeline.",
            style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Matrix Checkbox badge flow grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val chunked = goalsList.chunked(3)
            chunked.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { goalName ->
                        val selected = goalsVal.selected.contains(goalName)
                        Box(modifier = Modifier.weight(1f)) {
                            CustomGoalBadge(
                                goal = goalName,
                                selected = selected,
                                onToggle = {
                                    val list = if (selected) {
                                        goalsVal.selected.filter { it != goalName }
                                    } else {
                                        goalsVal.selected + goalName
                                    }
                                    viewModel.updateGoals { g -> g.copy(selected = list) }
                                }
                            )
                        }
                    }
                    // pad empty spaces
                    if (row.size < 3) {
                        for (i in 0 until (3 - row.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        CardWrapper {
            Column(modifier = Modifier.padding(14.dp)) {
                PremiumField("Target Draw Horizon", "Aprox. duration before capital withdrawals trigger") {
                    PremiumDropdown(
                        value = goalsVal.horizon,
                        options = listOf(
                            "Under 3 years",
                            "3–5 years",
                            "5–10 years",
                            "10–15 years",
                            "15–20 years",
                            "20+ years"
                        ),
                        onSelect = { viewModel.updateGoals { g -> g.copy(horizon = it) } }
                    )
                }

                PremiumField("Additional Context / Queries to solve inside Report") {
                    PremiumTextarea(
                        value = goalsVal.notes,
                        onValueChange = { viewModel.updateGoals { g -> g.copy(notes = it) } },
                        placeholder = "e.g., First generation wealth builder setup, planning UK education cost hedges etc."
                    )
                }
            }
        }

        // Trigger CTA Card block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GoldAccent.copy(alpha = 0.1f))
                .border(BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Ready to generate your WealthCanvas?",
                    color = GoldAccent,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Our Gemini AI engine analyzes all 7 core dimensions of your inputs and formats a fully bespoke roadmap report.",
                    color = MutedText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.4.em
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.generateReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = SlateDark),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    enabled = !generating,
                    modifier = Modifier.testTag("generate_report_button")
                ) {
                    Text("✦  Generate WealthCanvas Report  →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        FooterNavigationButtons(
            onBack = { viewModel.navigateTo("estate") },
            onNext = { viewModel.generateReport() },
            nextLabel = "Generate Report",
            generating = generating
        )
    }
}

@Composable
fun ResultsScreen(viewModel: WealthViewModel) {
    val scores = viewModel.computeScores()
    val prof by viewModel.profile.collectAsState()
    val advisor by viewModel.advisor.collectAsState()
    val reportContent by viewModel.report.collectAsState()
    val generating by viewModel.generating.collectAsState()
    val context = LocalContext.current

    val scoreItems = listOf(
        Triple("divScore", "Portfolio", "Diversification"),
        Triple("insScore", "Protection", "Security"),
        Triple("efScore", "Emergency", "Contingency"),
        Triple("retScore", "Retirement", "Planning"),
        Triple("estScore", "Legacy", "Estate"),
        Triple("taxScore", "Tax", "Savings"),
        Triple("debtScore", "Debt", "Liabilities")
    )

    val overallGrade = getGrade(scores.overall)
    val overallDesc = when {
        scores.overall >= 85 -> "Outstanding financial posture. Priority focuses on asset optimization and legal estate legacy protections."
        scores.overall >= 70 -> "Commanding score with important gaps remaining to fix. Addressing small protection locks creates outsized buffers."
        scores.overall >= 50 -> "Financial health asks for immediate structured focus. Thankfully, solutions are clear and highly accessible."
        else -> "Significant gaps detected across multiple pillars. The roadmap below charts a direct path to restore confidence."
    }

    val hasCriticalGaps = protTermMissing(viewModel) || protHealthMissing(viewModel) || scores.efScore < 40
    val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("results_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Results Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified Report",
                    tint = GoldAccent,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = "WealthCanvas Report",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GoldAccent,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Text(
                        text = "Prepared exclusively for ${prof.name.ifEmpty { "Client" }} on $formattedDate",
                        color = MutedText,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Overall Score Card
        CardWrapper(borderAccent = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "YOUR OVERALL SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.15.em
                    )
                )

                // Master big Score Arc drawing
                ScoreCircularArc(score = scores.overall, size = 150.dp)

                Text(
                    text = overallDesc,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CreamText,
                        lineHeight = 1.6.em
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // 7 Dimensions Score Bar Row
        Text(
            text = "DASHBOARD PILLARS breakdown",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MutedText,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.05.em
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            scoreItems.forEach { (key, title, subtitle) ->
                val subScore = when (key) {
                    "divScore" -> scores.divScore
                    "insScore" -> scores.insScore
                    "efScore" -> scores.efScore
                    "retScore" -> scores.retScore
                    "estScore" -> scores.estScore
                    "taxScore" -> scores.taxScore
                    else -> scores.debtScore
                }

                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSurface)
                        .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ScoreCircularArc(score = subScore, size = 68.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(title, color = CreamText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(subtitle, color = MutedText, fontSize = 9.sp)
                    }
                }
            }
        }

        // Critical Vulnerability Alerts
        if (hasCriticalGaps) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RedError.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, RedError.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🚨 CRITICAL GAPS DEMANDING ACTION",
                        color = RedError,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (protTermMissing(viewModel)) {
                        Text("• No Term Life Cover — Your families structural cashflow lacks security should unexpected death occur.", color = CreamText, fontSize = 12.sp)
                    }
                    if (protHealthMissing(viewModel)) {
                        Text("• No Independent Medical Shield — Unplanned major hospitalization events will directly cannibalize networth lines.", color = CreamText, fontSize = 12.sp)
                    }
                    if (scores.efScore < 40) {
                        Text("• Unfunded Emergency Bucket — Less than 40% target reserves are parsed. High threat matrix in case of job losses.", color = CreamText, fontSize = 12.sp)
                    }
                }
            }
        }

        // AI Generated / Structured report panel
        CardWrapper {
            Column(modifier = Modifier.padding(16.dp)) {
                if (generating) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = GoldAccent)
                        Text(
                            text = "Analyzing your inputs...",
                            color = GoldAccent,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif)
                        )
                        Text(
                            text = "Configuring scores across all 7 pillars of your dashboard",
                            color = MutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (reportContent.isNotEmpty()) {
                    MarkdownText(text = reportContent)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { viewModel.generateReport() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = SlateDark),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Retry Report Generation", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Advisor branding and CTA contact panel
        CardWrapper(borderAccent = true) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Advisor Avt",
                            tint = GoldAccent,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Column {
                        Text(
                            text = advisor.firm,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CreamText,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = advisor.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (advisor.tagline.isNotEmpty()) {
                            Text(
                                text = "\"${advisor.tagline}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MutedText,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "This report lists initial indicators based on user parameters. To activate these roadmap steps into concrete actions, directly contact your advisor Jitender Chaudhary:",
                    color = CreamText,
                    fontSize = 12.sp,
                    lineHeight = 1.5.em
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Call Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${advisor.phone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error triggering call dialer", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent.copy(alpha = 0.15f),
                            contentColor = GoldAccent
                        ),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                            Text("Call Advisor", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Email Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${advisor.email}")
                                    putExtra(Intent.EXTRA_SUBJECT, "Query regarding WealthCanvas Assessment")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error triggering email agent", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent.copy(alpha = 0.15f),
                            contentColor = GoldAccent
                        ),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                            Text("Email Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = "Disclaimer: This WealthCanvas assessment is compiled under algorithmic indicator formulas. It does not replace regulated SEBI financial consultancy. Seek registered planning before capital deployment.",
                    color = MutedText,
                    fontSize = 9.sp,
                    lineHeight = 1.3.em,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // New Assessment start
        Button(
            onClick = { viewModel.resetAssessment() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedText),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, SlateSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text("← Start a New Personal Assessment", fontWeight = FontWeight.Bold)
        }
    }
}

// Inline Markdown Renderer for dynamic styling
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val lines = text.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                continue
            }
            when {
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GoldAccent,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CreamText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("✦") -> {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✦", color = GoldAccent, fontSize = 13.sp)
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(1).trim()),
                            color = CreamText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 1.6.em)
                        )
                    }
                }
                trimmed.firstOrNull()?.isDigit() == true && trimmed.contains(".") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed),
                        color = CreamText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 1.6.em),
                        modifier = Modifier.padding(start = 10.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed),
                        color = CreamText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 1.6.em)
                    )
                }
            }
        }
    }
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentText = text
        val parts = currentText.split("**")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = CreamText)) {
                    append(parts[i])
                }
            } else {
                val subParts = parts[i].split("*")
                for (j in subParts.indices) {
                    if (j % 2 == 1) {
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = MutedText)) {
                            append(subParts[j])
                        }
                    } else {
                        append(subParts[j])
                    }
                }
            }
        }
    }
}

// Verification properties
fun protTermMissing(viewModel: WealthViewModel): Boolean {
    return viewModel.protection.value.hasTerm == "No"
}

fun protHealthMissing(viewModel: WealthViewModel): Boolean {
    return viewModel.protection.value.hasHealth == "No"
}


