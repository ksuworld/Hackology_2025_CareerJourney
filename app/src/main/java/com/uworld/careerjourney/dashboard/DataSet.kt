package com.uworld.careerjourney.dashboard

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

enum class CareerJourneyType(
    val id: String,
    val journeyName: String,
    val description: String
) {
    // --- Pre-College Journey Types (High School) ---
    HIGH_SCHOOL_PREP(
        id = "JNY-001",
        journeyName = "High School Exploration & Preparation",
        description = "Focuses on interest assessment, foundational skill building, and academic alignment for college readiness (Grades 9-10)."
    ),
    COLLEGE_APP(
        id = "JNY-002",
        journeyName = "College Selection & Application",
        description = "Covers standardized testing, college list finalization, application submission, and financial aid planning (Grades 11-12)."
    ),

    // --- In-College Journey Types (Undergraduate Years) ---
    FRESHMAN_DISCOVERY(
        id = "JNY-003",
        journeyName = "Freshman Year: Discovery & Foundation",
        description = "Milestones related to major declaration, initial career advising, and establishing a professional profile (Year 1)."
    ),
    SOPHOMORE_NETWORKING(
        id = "JNY-004",
        journeyName = "Sophomore Year: Exploration & Networking",
        description = "Activities centered on early professional experiences, networking, and refining career interests (Year 2)."
    ),
    JUNIOR_IMMERSION(
        id = "JNY-005",
        journeyName = "Junior Year: Immersion & Application",
        description = "The critical phase for securing and completing high-impact internships and mastering interviewing skills (Year 3)."
    ),
    SENIOR_LAUNCH(
        id = "JNY-006",
        journeyName = "Senior Year: Execution & Transition",
        description = "The final push for job searching, grad school applications, offer acceptance, and transition-to-work readiness (Year 4)."
    ),

    // --- After-College Journey Types (Post-Graduation) ---
    POST_GRAD_TRANSITION(
        id = "JNY-007",
        journeyName = "Post-Graduation: Transition to Work",
        description = "Focuses on successfully starting the first full-time role, workplace integration, and initial skill certification (Year 1)."
    ),
    EARLY_CAREER_ADVANCEMENT(
        id = "JNY-008",
        journeyName = "Early Career Growth & Advancement",
        description = "Milestones related to performance reviews, first promotions, and setting mid-term career/advanced degree goals (Years 2-3)."
    );

    // Companion object for utility functions
    companion object {
        /**
         * Finds a CareerJourneyType by its unique ID.
         */
        fun getById(id: String): CareerJourneyType? = entries.find { it.id == id }
    }
}

// Data Class Structure (for reference)
data class CareerMilestone(
    val milestoneId: String,
    val journeyTypeId: String, // Links to CareerJourneyType (e.g., "JNY-004")
    val title: String,
    val description: String,
    val targetYear: Int, // Year 1-4 for College, 5+ for Post-Grad
    val status: StageStatus = StageStatus.UPCOMING
)

// The Complete Dataset Map
val careerMilestoneData: Map<CareerJourneyType, List<CareerMilestone>> = mapOf(

    // 1. PRE-COLLEGE JOURNEY: EXPLORATION & PREPARATION (HS Grades 9-10)
    CareerJourneyType.HIGH_SCHOOL_PREP to listOf(
        CareerMilestone(
            milestoneId = "MS-101",
            journeyTypeId = "JNY-001",
            title = "Complete Career/Interest Assessment",
            description = "Utilize guidance resources to identify core interests and potential career paths.",
            targetYear = 9
        ),
        CareerMilestone(
            milestoneId = "MS-102",
            journeyTypeId = "JNY-001",
            title = "Secure Leadership Role",
            description = "Achieve or maintain a leadership position in a club, sport, or community organization.",
            targetYear = 10
        ),
        CareerMilestone(
            milestoneId = "MS-103",
            journeyTypeId = "JNY-001",
            title = "Establish Academic Path",
            description = "Select challenging and relevant high school courses (Honors/AP/IB) that align with college goals.",
            targetYear = 10
        )
    ),

    // 2. PRE-COLLEGE JOURNEY: SELECTION & APPLICATION (HS Grades 11-12)
    CareerJourneyType.COLLEGE_APP to listOf(
        CareerMilestone(
            milestoneId = "MS-104",
            journeyTypeId = "JNY-002",
            title = "Complete Standardized Testing",
            description = "Take the SAT or ACT and complete any necessary re-tests for competitive applications.",
            targetYear = 11
        ),
        CareerMilestone(
            milestoneId = "MS-105",
            journeyTypeId = "JNY-002",
            title = "Finalize College List",
            description = "Curate a balanced list of 'safety,' 'target,' and 'reach' schools (10-15 institutions).",
            targetYear = 12
        ),
        CareerMilestone(
            milestoneId = "MS-106",
            journeyTypeId = "JNY-002",
            title = "Submit Applications & Essays",
            description = "Complete and submit all college applications, including polished personal essays.",
            targetYear = 12
        ),
        CareerMilestone(
            milestoneId = "MS-107",
            journeyTypeId = "JNY-002",
            title = "Complete Financial Aid Forms",
            description = "Submit the FAFSA and/or CSS Profile to qualify for need-based and merit aid.",
            targetYear = 12
        ),
        CareerMilestone(
            milestoneId = "MS-108",
            journeyTypeId = "JNY-002",
            title = "Commit to a College",
            description = "Accept the offer of admission and pay the deposit by National College Decision Day (May 1st).",
            targetYear = 12
        )
    ),

    // 3. IN-COLLEGE JOURNEY: FRESHMAN YEAR
    CareerJourneyType.FRESHMAN_DISCOVERY to listOf(
        CareerMilestone(
            milestoneId = "MS-201",
            journeyTypeId = "JNY-003",
            title = "Meet Career Advisor",
            description = "Schedule your initial one-on-one session to discuss career goals and resources.",
            targetYear = 1
        ),
        CareerMilestone(
            milestoneId = "MS-202",
            journeyTypeId = "JNY-003",
            title = "Declare Major/Minor",
            description = "Officially declare your major or complete all prerequisites for program entry.",
            targetYear = 1
        ),
        CareerMilestone(
            milestoneId = "MS-203",
            journeyTypeId = "JNY-003",
            title = "Create Professional Brand",
            description = "Develop a polished professional resume and a complete, review-ready LinkedIn profile.",
            targetYear = 1
        )
    ),

    // 4. IN-COLLEGE JOURNEY: SOPHOMORE YEAR
    CareerJourneyType.SOPHOMORE_NETWORKING to listOf(
        CareerMilestone(
            milestoneId = "MS-204",
            journeyTypeId = "JNY-004",
            title = "First Targeted Experience",
            description = "Secure a relevant on-campus job, volunteer role, or early research assistant position.",
            targetYear = 2
        ),
        CareerMilestone(
            milestoneId = "MS-205",
            journeyTypeId = "JNY-004",
            title = "Conduct Informational Interviews",
            description = "Schedule and complete at least three informational interviews with industry professionals.",
            targetYear = 2
        ),
        CareerMilestone(
            milestoneId = "MS-206",
            journeyTypeId = "JNY-004",
            title = "Join Professional Organization",
            description = "Become an active member in an academic or professional club for networking and development.",
            targetYear = 2
        ),
        CareerMilestone(
            milestoneId = "MS-207",
            journeyTypeId = "JNY-004",
            title = "Attend a Career Fair",
            description = "Attend a university career fair to practice interacting with employers and collecting contacts.",
            targetYear = 2
        )
    ),

    // 5. IN-COLLEGE JOURNEY: JUNIOR YEAR
    CareerJourneyType.JUNIOR_IMMERSION to listOf(
        CareerMilestone(
            milestoneId = "MS-301",
            journeyTypeId = "JNY-005",
            title = "Secure Summer Internship",
            description = "Receive and accept an offer for a full-time, field-related internship or co-op.",
            targetYear = 3
        ),
        CareerMilestone(
            milestoneId = "MS-302",
            journeyTypeId = "JNY-005",
            title = "Complete High-Impact Internship",
            description = "Successfully complete the summer experience and secure a positive supervisor evaluation.",
            targetYear = 3
        ),
        CareerMilestone(
            milestoneId = "MS-303",
            journeyTypeId = "JNY-005",
            title = "Master Interview Skills",
            description = "Complete mock interviews (behavioral and technical/case) to master interview techniques.",
            targetYear = 3
        ),
        CareerMilestone(
            milestoneId = "MS-304",
            journeyTypeId = "JNY-005",
            title = "Draft Post-Graduation Plan",
            description = "Formalize the decision between a full-time job search or graduate/professional school applications.",
            targetYear = 3
        )
    ),

    // 6. IN-COLLEGE JOURNEY: SENIOR YEAR
    CareerJourneyType.SENIOR_LAUNCH to listOf(
        CareerMilestone(
            milestoneId = "MS-401",
            journeyTypeId = "JNY-006",
            title = "Launch Job/Grad School Search",
            description = "Begin applying to full-time jobs or submitting graduate school applications (including required exams).",
            targetYear = 4
        ),
        CareerMilestone(
            milestoneId = "MS-402",
            journeyTypeId = "JNY-006",
            title = "Secure Offer(s)",
            description = "Receive a final job offer or a graduate school admission offer.",
            targetYear = 4
        ),
        CareerMilestone(
            milestoneId = "MS-403",
            journeyTypeId = "JNY-006",
            title = "Negotiate & Accept Role",
            description = "Negotiate salary/terms and formally accept your post-graduation role/program.",
            targetYear = 4
        ),
        CareerMilestone(
            milestoneId = "MS-404",
            journeyTypeId = "JNY-006",
            title = "Graduate from College",
            description = "Successfully earn the bachelor's degree.",
            targetYear = 4
        )
    ),

    // 7. AFTER-COLLEGE JOURNEY: TRANSITION TO WORK
    CareerJourneyType.POST_GRAD_TRANSITION to listOf(
        CareerMilestone(
            milestoneId = "MS-501",
            journeyTypeId = "JNY-007",
            title = "Start First Professional Role",
            description = "Successfully begin your first full-time professional job.",
            targetYear = 5 // Post-Grad Year 1
        ),
        CareerMilestone(
            milestoneId = "MS-502",
            journeyTypeId = "JNY-007",
            title = "Complete Initial Training",
            description = "Pass the initial probationary period and complete all company/industry-specific training.",
            targetYear = 5
        ),
        CareerMilestone(
            milestoneId = "MS-503",
            journeyTypeId = "JNY-007",
            title = "Acquire Industry Certification",
            description = "Earn a relevant professional certification or license required for advancement in your field.",
            targetYear = 5
        ),
        CareerMilestone(
            milestoneId = "MS-504",
            journeyTypeId = "JNY-007",
            title = "Build Internal Network",
            description = "Establish working relationships with key colleagues and a direct supervisor for guidance.",
            targetYear = 5
        )
    ),

    // 8. AFTER-COLLEGE JOURNEY: EARLY CAREER ADVANCEMENT
    CareerJourneyType.EARLY_CAREER_ADVANCEMENT to listOf(
        CareerMilestone(
            milestoneId = "MS-601",
            journeyTypeId = "JNY-008",
            title = "Achieve Positive Performance Review",
            description = "Complete your first formal annual review with positive results and a clear development plan.",
            targetYear = 6 // Post-Grad Year 2
        ),
        CareerMilestone(
            milestoneId = "MS-602",
            journeyTypeId = "JNY-008",
            title = "Secure Professional Mentor",
            description = "Establish a relationship with an experienced formal or informal mentor in your industry.",
            targetYear = 6
        ),
        CareerMilestone(
            milestoneId = "MS-603",
            journeyTypeId = "JNY-008",
            title = "Attain First Advancement",
            description = "Receive your first promotion or significant salary increase (typically 1-3 years in the role).",
            targetYear = 7 // Post-Grad Year 3
        ),
        CareerMilestone(
            milestoneId = "MS-604",
            journeyTypeId = "JNY-008",
            title = "Define Mid-Term Goal",
            description = "Formalize a plan for the next major career step (e.g., advanced degree, specialization change).",
            targetYear = 7
        )
    )
)

// Main function to retrieve and display milestones
fun getMilestonesForJourney(journeyType: CareerJourneyType) {
    // 1. Look up the list of milestones using the CareerJourneyType as the key
    val milestones = careerMilestoneData[journeyType]

    if (milestones.isNullOrEmpty()) {
        println("No milestones found for ${journeyType.journeyName}.")
        return
    }

    println("--- Milestones for ${journeyType.journeyName} (${journeyType.id}) ---")
    println("Description: ${journeyType.description}\n")

    // 2. Iterate through the list and display each milestone
    milestones.forEach { milestone ->
        println("✅ **${milestone.title}** (Year ${milestone.targetYear})")
        println("   - Details: ${milestone.description}")
        println("   - ID: ${milestone.milestoneId}\n")
    }
}

// --- Demonstration ---

// Example 1: Get milestones for the Junior Immersion phase
//println("--- DEMO: Junior Immersion (JNY-005) ---")
//getMilestonesForJourney(CareerJourneyType.JUNIOR_IMMERSION)

// Example 2: Get milestones for the Early Career phase
//println("\n--- DEMO: Early Career Growth (JNY-008) ---")
//getMilestonesForJourney(CareerJourneyType.EARLY_CAREER_ADVANCEMENT)

//--- DEMO: Junior Immersion (JNY-005) ---
//--- Milestones for Junior Year: Immersion & Application (JNY-005) ---
//Description: The critical phase for securing and completing high-impact internships and mastering interviewing skills (Year 3).
//
//✅ **Secure Summer Internship** (Year 3)
//- Details: Receive and accept an offer for a full-time, field-related internship or co-op.
//- ID: MS-301
//
//✅ **Complete High-Impact Internship** (Year 3)
//- Details: Successfully complete the summer experience and secure a positive supervisor evaluation.
//- ID: MS-302
// ... (and so on)

@Serializable
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isTyping: Boolean = false
)

// 1. Enum to define the status of a stage card in the UI
enum class StageStatus {
    COMPLETED,      // Stage is 100% done
    CURRENT_FOCUS,  // User should be working on this stage (Progress < 100%)
    UPCOMING        // Not yet started (Progress is 0%)
}

// 2. Data Class for an Individual Stage Card (e.g., "Sophomore Year")
data class CareerStage(
    val journeyType: CareerJourneyType,      // Links to the enum for name/ID
    val progressPercent: Int,                // Controls the progress bar UI
    val status: StageStatus,                 // Controls card highlighting/buttons
    val color: Color,
    val milestones: List<CareerMilestone>    // The specific actions for this card
)

// 3. Data Class for the High-Level Journey Banner (The Chapter Title)
data class JourneySection(
    val name: String,         // E.g., "IN-COLLEGE JOURNEY"
    val color: Color,
    val stages: List<CareerStage>
)

// Defining the colors based on the visual concept
val PreCollegeColor = Color(0xFF800080) // Purple
val InCollegeColor = Color(0xFF008000) // Green
val AfterCollegeColor = Color(0xFFFFA500) // Orange

// Corrected function to generate simple placeholder milestones with IDs and Years
fun generateMilestones(type: CareerJourneyType, year: Int, count: Int) = (1..count).map {
    CareerMilestone(
        milestoneId = "${type.id}-MS-$it",
        title = "Action Item $it",
        description = "Description for action item $it",
        journeyTypeId = type.id, // Corrected
        targetYear = year // Corrected
    )
}

val sampleTimelineData: List<JourneySection> = listOf(
    JourneySection(
        name = "PRE-COLLEGE JOURNEY",
        color = PreCollegeColor,
        stages = listOf(
            CareerStage(
                journeyType = CareerJourneyType.HIGH_SCHOOL_PREP,
                progressPercent = 100,
                status = StageStatus.COMPLETED,
                color = PreCollegeColor,
                milestones = generateMilestones(CareerJourneyType.HIGH_SCHOOL_PREP, 9, 3)
            ),
            CareerStage(
                journeyType = CareerJourneyType.COLLEGE_APP,
                progressPercent = 100,
                status = StageStatus.COMPLETED,
                color = PreCollegeColor,
                milestones = generateMilestones(CareerJourneyType.COLLEGE_APP, 12, 5)
            )
        )
    ),

    JourneySection(
        name = "IN-COLLEGE JOURNEY",
        color = InCollegeColor,
        stages = listOf(
            CareerStage(
                journeyType = CareerJourneyType.FRESHMAN_DISCOVERY,
                progressPercent = 100,
                status = StageStatus.COMPLETED,
                color = InCollegeColor,
                milestones = generateMilestones(CareerJourneyType.FRESHMAN_DISCOVERY, 1, 4)
            ),
            CareerStage(
                journeyType = CareerJourneyType.SOPHOMORE_NETWORKING,
                progressPercent = 75,
                status = StageStatus.CURRENT_FOCUS,
                color = InCollegeColor, // <-- VALUE PASSED HERE
                milestones = generateMilestones(CareerJourneyType.SOPHOMORE_NETWORKING, 2, 6)
            ),
            CareerStage(
                journeyType = CareerJourneyType.JUNIOR_IMMERSION,
                progressPercent = 0,
                status = StageStatus.UPCOMING,
                color = InCollegeColor,
                milestones = generateMilestones(CareerJourneyType.JUNIOR_IMMERSION, 3, 5)
            ),
            CareerStage(
                journeyType = CareerJourneyType.SENIOR_LAUNCH,
                progressPercent = 0,
                status = StageStatus.UPCOMING,
                color = InCollegeColor,
                milestones = generateMilestones(CareerJourneyType.SENIOR_LAUNCH, 4, 7)
            )
        )
    ),

    JourneySection(
        name = "AFTER-COLLEGE JOURNEY",
        color = AfterCollegeColor,
        stages = listOf(
            CareerStage(
                journeyType = CareerJourneyType.POST_GRAD_TRANSITION,
                progressPercent = 0,
                status = StageStatus.UPCOMING,
                color = AfterCollegeColor,
                milestones = generateMilestones(CareerJourneyType.POST_GRAD_TRANSITION, 5, 4)
            ),
            CareerStage(
                journeyType = CareerJourneyType.EARLY_CAREER_ADVANCEMENT,
                progressPercent = 0,
                status = StageStatus.UPCOMING,
                color = AfterCollegeColor,
                milestones = generateMilestones(CareerJourneyType.EARLY_CAREER_ADVANCEMENT, 7, 3)
            )
        )
    )
)