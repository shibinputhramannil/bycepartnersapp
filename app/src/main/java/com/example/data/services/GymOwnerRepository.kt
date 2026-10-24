package com.example.data.services

import com.example.R
import com.example.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Clean API Service & Repository Layer prepared for Django REST Framework + JWT
 * Base Endpoint Contract: /api/v1/
 */
object GymOwnerRepository {

    // Mock Gym Owner profile
    private var currentOwner = GymOwnerUser(
        id = "own_01",
        name = "Kishore Kumar",
        email = "kishore@ironhousefitness.com",
        phone = "+91 98470 12345",
        role = "Gym Owner",
        profileImage = null
    )

    // Mock Gym details (Iron House Fitness, Kozhikode, Kerala)
    private var currentGym = GymProfile(
        id = "gym_iron_house",
        name = "Iron House Fitness",
        description = "Premium high-performance training facility with modern strength and cardio zones.",
        phone = "+91 495 276 5432",
        email = "contact@ironhousefitness.com",
        website = "www.ironhousefitness.in",
        address = "4th Floor, Emerald Plaza, Mavoor Road",
        city = "Kozhikode",
        district = "Kozhikode",
        state = "Kerala",
        pincode = "673004",
        openingTime = "05:30 AM",
        closingTime = "10:30 PM",
        latitude = 11.2588,
        longitude = 75.7804,
        logoRes = R.drawable.byce_logo
    )

    private val samplePlans = mutableListOf(
        MembershipPlanItem(
            id = "plan_elite_1",
            name = "All-Access Elite",
            description = "Unlimited access to premium group classes (Dance, Boxing, Strength, Yoga), all premium gyms city-wide & digital home content.",
            duration = 1,
            durationUnit = "Month",
            price = 1999.0,
            discountPrice = null,
            features = listOf(
                "Unlimited Elite group classes (Boxing, Dance, Yoga, Strength)",
                "Access to all premium & partner gyms city-wide",
                "Full Digital Suite (Live + On-Demand + Meditation)",
                "2 Free Guest Passes / month",
                "Up to 45 days membership pause allowance",
                "Priority 4-day advance slot booking"
            ),
            isActive = true,
            tierLevel = "Tier 1: All-Access Elite",
            accessScope = "All City-Wide Elite Centers & Gyms",
            groupClassesQuota = "Unlimited",
            pauseDaysAllowed = 45,
            isPopular = true,
            monthlyPrice = 1999.0,
            quarterlyPrice = 4999.0,
            annualPrice = 14999.0
        ),
        MembershipPlanItem(
            id = "plan_pro_2",
            name = "Standard Pro",
            description = "Unlimited standard partner gyms, at-home digital content, and 2 monthly visits to Elite group classes.",
            duration = 1,
            durationUnit = "Month",
            price = 1299.0,
            discountPrice = null,
            features = listOf(
                "Unlimited standard partner gym floor access",
                "2 Elite group class visits / month included",
                "Full Digital & At-Home workout library",
                "1 Free Guest Pass / month",
                "Up to 30 days membership pause allowance"
            ),
            isActive = true,
            tierLevel = "Tier 2: Standard Pro",
            accessScope = "Unlimited Partner Gyms + 2 Elite/mo",
            groupClassesQuota = "2 visits / month",
            pauseDaysAllowed = 30,
            isPopular = false,
            monthlyPrice = 1299.0,
            quarterlyPrice = 3299.0,
            annualPrice = 9999.0
        ),
        MembershipPlanItem(
            id = "plan_select_3",
            name = "Single-Center Select",
            description = "Unlimited access to one specific physical gym or center of choice at high value.",
            duration = 1,
            durationUnit = "Month",
            price = 899.0,
            discountPrice = null,
            features = listOf(
                "Unlimited access to Iron House Fitness (Home Gym)",
                "Full strength & cardio floor access",
                "Locker & shower facilities included",
                "Up to 20 days membership pause allowance"
            ),
            isActive = true,
            tierLevel = "Tier 3: Single-Center Select",
            accessScope = "Single Home Gym (Iron House Fitness)",
            groupClassesQuota = "Add-on session passes available",
            pauseDaysAllowed = 20,
            isPopular = false,
            monthlyPrice = 899.0,
            quarterlyPrice = 2399.0,
            annualPrice = 6999.0
        ),
        MembershipPlanItem(
            id = "plan_digital_4",
            name = "Digital / At-Home",
            description = "Purely digital subscription with unlimited live-streamed sessions, on-demand videos, and mental wellness & meditation.",
            duration = 1,
            durationUnit = "Month",
            price = 399.0,
            discountPrice = null,
            features = listOf(
                "Unlimited daily live interactive workout streams",
                "500+ on-demand yoga, HIIT, and strength routines",
                "Guided mental wellness & sleep meditation library",
                "Apple Health & Smartwatch calorie sync",
                "Up to 15 days membership pause allowance"
            ),
            isActive = true,
            tierLevel = "Tier 4: Digital At-Home",
            accessScope = "Anywhere (Mobile / Smart TV)",
            groupClassesQuota = "Unlimited Live Streams",
            pauseDaysAllowed = 15,
            isPopular = false,
            monthlyPrice = 399.0,
            quarterlyPrice = 999.0,
            annualPrice = 2999.0
        ),
        MembershipPlanItem(
            id = "plan_coaching_5",
            name = "Coaching & Nutrition",
            description = "Dedicated 1-on-1 certified online coach, personalized meal planning, and regular progress check-ins.",
            duration = 1,
            durationUnit = "Month",
            price = 1499.0,
            discountPrice = null,
            features = listOf(
                "Dedicated certified fitness & nutrition coach",
                "Customized weekly macro & Indian meal plans",
                "Weekly 1-on-1 video accountability check-in",
                "WhatsApp direct coach chat support",
                "Up to 14 days pause allowance"
            ),
            isActive = true,
            tierLevel = "Tier 5: Coaching & Nutrition",
            accessScope = "1-on-1 Dedicated Coach & App",
            groupClassesQuota = "Included Workout Guidance",
            pauseDaysAllowed = 14,
            isPopular = false,
            monthlyPrice = 1499.0,
            quarterlyPrice = 3899.0,
            annualPrice = 11999.0
        )
    )

    private val sampleCustomers = mutableListOf(
        CustomerItem("c1", "Rahul Menon", "rahul.menon@gmail.com", "+91 98471 22334", "Yearly", "10 Jan 2026", "09 Jan 2027", "Active", "10 Jan 2025", "RM"),
        CustomerItem("c2", "Ananya Nair", "ananya.nair@outlook.com", "+91 97455 33445", "Monthly", "01 Sep 2026", "30 Sep 2026", "Active", "15 Jun 2026", "AN"),
        CustomerItem("c3", "Mohammed Faizal", "faizal.m@gmail.com", "+91 99951 44556", "Half Yearly", "15 Jul 2026", "14 Jan 2027", "Active", "15 Jul 2026", "MF"),
        CustomerItem("c4", "Deepak Varma", "deepak.v@yahoo.com", "+91 94471 55667", "Quarterly", "12 Aug 2026", "11 Nov 2026", "Active", "12 Feb 2026", "DV"),
        CustomerItem("c5", "Sneha Joseph", "sneha.j@gmail.com", "+91 96331 66778", "Monthly", "18 Aug 2026", "17 Sep 2026", "Expired", "18 May 2026", "SJ"),
        CustomerItem("c6", "Arjun Prasad", "arjun.p@gmail.com", "+91 98951 77889", "Yearly", "05 Sep 2026", "04 Sep 2027", "Pending", "05 Sep 2026", "AP"),
        CustomerItem("c7", "Pooja Krishnan", "pooja.k@gmail.com", "+91 97461 88990", "Quarterly", "20 Jun 2026", "19 Sep 2026", "Expired", "20 Dec 2025", "PK"),
        CustomerItem("c8", "Vivek Raj", "vivek.raj@gmail.com", "+91 95671 99001", "Half Yearly", "01 Aug 2026", "31 Jan 2027", "Active", "01 Aug 2026", "VR")
    )

    private val sampleMemberships = mutableListOf(
        MembershipItem("m1", "Rahul Menon", "rahul.menon@gmail.com", "+91 98471 22334", "Yearly", "Iron House Fitness", "10 Jan 2026", "09 Jan 2027", 11999.0, "Active"),
        MembershipItem("m2", "Ananya Nair", "ananya.nair@outlook.com", "+91 97455 33445", "Monthly", "Iron House Fitness", "01 Sep 2026", "30 Sep 2026", 1499.0, "Active"),
        MembershipItem("m3", "Mohammed Faizal", "faizal.m@gmail.com", "+91 99951 44556", "Half Yearly", "Iron House Fitness", "15 Jul 2026", "14 Jan 2027", 6999.0, "Active"),
        MembershipItem("m4", "Deepak Varma", "deepak.v@yahoo.com", "+91 94471 55667", "Quarterly", "Iron House Fitness", "12 Aug 2026", "11 Nov 2026", 3999.0, "Active"),
        MembershipItem("m5", "Sneha Joseph", "sneha.j@gmail.com", "+91 96331 66778", "Monthly", "Iron House Fitness", "18 Aug 2026", "17 Sep 2026", 1499.0, "Expired"),
        MembershipItem("m6", "Arjun Prasad", "arjun.p@gmail.com", "+91 98951 77889", "Yearly", "Iron House Fitness", "05 Sep 2026", "04 Sep 2027", 11999.0, "Pending"),
        MembershipItem("m7", "Rohan Pillai", "rohan.p@gmail.com", "+91 94470 11223", "Monthly", "Iron House Fitness", "10 Aug 2026", "09 Sep 2026", 1499.0, "Cancelled")
    )

    private val samplePayments = mutableListOf(
        PaymentItem("TXN-984210", "Rahul Menon", "Yearly", 11999.0, "Razorpay UPI", "Successful", "Today, 10:24 AM", orderId = "ORD_88421", paymentId = "PAY_77341", paidAt = "2026-09-24 10:24:12"),
        PaymentItem("TXN-984209", "Ananya Nair", "Monthly", 1499.0, "Google Pay", "Successful", "Today, 09:15 AM", orderId = "ORD_88420", paymentId = "PAY_77340", paidAt = "2026-09-24 09:15:45"),
        PaymentItem("TXN-984208", "Arjun Prasad", "Yearly", 11999.0, "Card / Razorpay", "Pending", "Yesterday, 07:40 PM", orderId = "ORD_88419", paymentId = "PAY_77339", paidAt = "Pending"),
        PaymentItem("TXN-984207", "Mohammed Faizal", "Half Yearly", 6999.0, "PayTM UPI", "Successful", "22 Sep 2026", orderId = "ORD_88418", paymentId = "PAY_77338", paidAt = "2026-09-22 18:22:04"),
        PaymentItem("TXN-984206", "Karthik Suresh", "Quarterly", 3999.0, "Net Banking", "Failed", "21 Sep 2026", orderId = "ORD_88417", paymentId = "PAY_77337", paidAt = "Failed - Bank Timeout"),
        PaymentItem("TXN-984205", "Vivek Raj", "Half Yearly", 6999.0, "PhonePe", "Successful", "20 Sep 2026", orderId = "ORD_88416", paymentId = "PAY_77336", paidAt = "2026-09-20 14:10:00")
    )

    private val sampleBookings = mutableListOf(
        BookingItem(
            id = "b1",
            customerName = "Rahul Menon",
            customerPhone = "+91 98471 22334",
            customerEmail = "rahul.menon92@gmail.com",
            membershipPlan = "Yearly Plan",
            date = "Today",
            time = "06:00 PM - 07:30 PM",
            shift = "Evening Shift",
            status = "Upcoming",
            arrivalStatus = "Confirmed",
            reminderMinutesBefore = 30,
            reminderScheduled = true,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Not sent"
        ),
        BookingItem(
            id = "b2",
            customerName = "Ananya Nair",
            customerPhone = "+91 97455 33445",
            customerEmail = "ananya.nair@gmail.com",
            membershipPlan = "Monthly Plan",
            date = "Today",
            time = "08:00 PM - 09:30 PM",
            shift = "Night Shift",
            status = "Upcoming",
            arrivalStatus = "Confirmed",
            reminderMinutesBefore = 30,
            reminderScheduled = true,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Not sent"
        ),
        BookingItem(
            id = "b3",
            customerName = "Mohammed Faizal",
            customerPhone = "+91 99951 44556",
            customerEmail = "faizal.mohd@gmail.com",
            membershipPlan = "Half Yearly",
            date = "Today",
            time = "06:00 AM - 07:30 AM",
            shift = "Morning Shift",
            status = "Completed",
            arrivalStatus = "Checked In",
            reminderMinutesBefore = 30,
            reminderScheduled = true,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Not sent"
        ),
        BookingItem(
            id = "b4",
            customerName = "Deepak Varma",
            customerPhone = "+91 94471 55667",
            customerEmail = "deepak.varma@gmail.com",
            membershipPlan = "Quarterly Plan",
            date = "Today",
            time = "08:00 AM - 09:30 AM",
            shift = "Morning Shift",
            status = "Completed",
            arrivalStatus = "Checked In",
            reminderMinutesBefore = 30,
            reminderScheduled = true,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Not sent"
        ),
        BookingItem(
            id = "b5",
            customerName = "Sneha Joseph",
            customerPhone = "+91 96331 66778",
            customerEmail = "sneha.joseph@gmail.com",
            membershipPlan = "Monthly Plan",
            date = "Tomorrow",
            time = "06:30 AM - 08:00 AM",
            shift = "Morning Shift",
            status = "Upcoming",
            arrivalStatus = "Confirmed",
            reminderMinutesBefore = 30,
            reminderScheduled = true,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Not sent"
        ),
        BookingItem(
            id = "b6",
            customerName = "Gautham Das",
            customerPhone = "+91 98950 11447",
            customerEmail = "gautham.das@gmail.com",
            membershipPlan = "Day Pass",
            date = "Today",
            time = "08:30 PM - 10:00 PM",
            shift = "Night Shift",
            status = "Cancelled",
            arrivalStatus = "Not Arrived",
            reminderMinutesBefore = 30,
            reminderScheduled = false,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Cancelled"
        ),
        // Walk-in day pass with no member profile
        BookingItem(
            id = "b7",
            customerName = "Nikhil Thomas",
            customerPhone = "+91 97440 21567",
            customerEmail = "nikhil.thomas@gmail.com",
            membershipPlan = "Day Pass",
            date = "Today",
            time = "06:30 PM - 08:00 PM",
            shift = "Evening Shift",
            status = "Upcoming",
            arrivalStatus = "Confirmed",
            reminderMinutesBefore = 30,
            reminderScheduled = false,
            reminderDeliveryChannel = "Gmail",
            reminderStatus = "Not sent"
        )
    )

    private val sampleStaff = mutableListOf(
        StaffMember("st1", "Sanjay Nambiar", "sanjay@ironhousefitness.com", "+91 98472 00112", "Gym Admin", "Active", "12 Jan 2025"),
        StaffMember("st2", "Kavya Sreedharan", "kavya@ironhousefitness.com", "+91 97451 11223", "Staff", "Active", "15 Mar 2025"),
        StaffMember("st3", "Manoj K.V.", "manoj@ironhousefitness.com", "+91 99953 22334", "Staff", "Active", "01 Jun 2025"),
        StaffMember("st4", "Akhil George", "akhil@ironhousefitness.com", "+91 94474 33445", "Staff", "Inactive", "10 Sep 2025")
    )

    private val sampleNotifications = mutableListOf(
        NotificationItem("n1", "Payment Received", "Rahul Menon renewed Yearly Membership for ₹11,999 via Razorpay UPI.", "Payment", "10 mins ago", false),
        NotificationItem("n2", "New Check-in Booking", "Ananya Nair booked a workout slot for 07:00 PM today.", "Booking", "35 mins ago", false),
        NotificationItem("n3", "Membership Expiring Soon", "Sneha Joseph's Monthly plan expires in 2 days.", "Membership", "2 hours ago", false),
        NotificationItem("n4", "System Maintenance", "Scheduled BYCE cloud synchronization at 02:00 AM IST.", "System", "1 day ago", true),
        NotificationItem("n5", "Payment Failed", "Transaction TXN-984206 from Karthik Suresh timed out.", "Payment", "3 days ago", true)
    )

    // GET /api/v1/admin/dashboard/
    fun getDashboardKpis(): DashboardKpis = DashboardKpis(
        totalCustomers = 1248,
        totalCustomersGrowth = 12.0,
        activeMemberships = 986,
        activeMembershipsGrowth = 8.0,
        monthlyRevenue = 482500.0,
        monthlyRevenueGrowth = 15.4,
        upcomingBookings = 42
    )

    fun getRevenueChartData(filter: String = "30 Days"): List<RevenueDataPoint> {
        return when (filter) {
            "7 Days" -> listOf(
                RevenueDataPoint("Mon", 14200.0),
                RevenueDataPoint("Tue", 18500.0),
                RevenueDataPoint("Wed", 16800.0),
                RevenueDataPoint("Thu", 21400.0),
                RevenueDataPoint("Fri", 25900.0),
                RevenueDataPoint("Sat", 32100.0),
                RevenueDataPoint("Sun", 28400.0)
            )
            "3 Months" -> listOf(
                RevenueDataPoint("Jul", 420000.0),
                RevenueDataPoint("Aug", 456000.0),
                RevenueDataPoint("Sep", 482500.0)
            )
            "1 Year" -> listOf(
                RevenueDataPoint("Q1", 1180000.0),
                RevenueDataPoint("Q2", 1340000.0),
                RevenueDataPoint("Q3", 1420000.0),
                RevenueDataPoint("Q4", 1580000.0)
            )
            else -> listOf( // 30 Days
                RevenueDataPoint("Week 1", 108000.0),
                RevenueDataPoint("Week 2", 119500.0),
                RevenueDataPoint("Week 3", 124200.0),
                RevenueDataPoint("Week 4", 130800.0)
            )
        }
    }

    fun getMembershipOverview(): MembershipOverview = MembershipOverview(
        active = 986,
        pending = 45,
        expired = 172,
        cancelled = 45
    )

    // GET /api/v1/auth/me/
    fun getOwnerProfile(): GymOwnerUser = currentOwner

    fun updateOwnerProfile(name: String, email: String, phone: String) {
        currentOwner = currentOwner.copy(name = name, email = email, phone = phone)
    }

    // GET /api/v1/gyms/{id}/
    fun getGymProfile(): GymProfile = currentGym

    fun updateGymProfile(updated: GymProfile) {
        currentGym = updated
    }

    // GET /api/v1/admin/customers/
    fun getCustomers(): List<CustomerItem> = sampleCustomers

    fun addCustomer(customer: CustomerItem) {
        sampleCustomers.add(0, customer)
    }

    // GET /api/v1/admin/memberships/
    fun getMemberships(): List<MembershipItem> = sampleMemberships

    // GET /api/v1/gyms/{gym_id}/membership-plans/
    fun getMembershipPlans(): List<MembershipPlanItem> = samplePlans

    fun addPlan(plan: MembershipPlanItem) {
        samplePlans.add(plan)
    }

    fun togglePlanStatus(planId: String) {
        val index = samplePlans.indexOfFirst { it.id == planId }
        if (index != -1) {
            val p = samplePlans[index]
            samplePlans[index] = p.copy(isActive = !p.isActive)
        }
    }

    private val sampleFreezes = mutableListOf(
        MembershipFreezeRecord(
            id = "frz_1",
            planName = "All-Access Elite",
            startDate = "15 Oct 2026",
            endDate = "25 Oct 2026",
            pauseDaysUsed = 10,
            pauseDaysRemaining = 35,
            reason = "Work Travel / Vacation",
            status = "Scheduled"
        )
    )

    fun getMembershipFreezes(): List<MembershipFreezeRecord> = sampleFreezes

    fun requestMembershipFreeze(planName: String, startDate: String, endDate: String, days: Int, reason: String) {
        val freeze = MembershipFreezeRecord(
            id = "frz_${System.currentTimeMillis()}",
            planName = planName,
            startDate = startDate,
            endDate = endDate,
            pauseDaysUsed = days,
            pauseDaysRemaining = (45 - days).coerceAtLeast(0),
            reason = reason,
            status = "Active"
        )
        sampleFreezes.add(0, freeze)
        logAdminAction("Kishore Kumar", "Approved Membership freeze ($days days) from $startDate to $endDate for $planName", "Memberships")
    }

    // ----------------------------------------------------
    // 1. MANUAL ATTENDANCE OVERRIDE ENGINE
    // ----------------------------------------------------
    private val sampleOverrides = mutableListOf<AttendanceOverrideRecord>()

    fun getAttendanceOverrides(): List<AttendanceOverrideRecord> = sampleOverrides

    // The override reason also satisfies the expired/pending/frozen reason rule;
    // unknown members and duplicate check-ins are still rejected.
    fun performAttendanceOverride(
        customerId: String,
        reason: String,
        staffName: String = "Kishore Kumar (Owner)"
    ): CheckInOutcome {
        val outcome = checkIn(customerId = customerId, reason = reason, source = "manual override", staffName = staffName)
        if (outcome is CheckInOutcome.Success) {
            val visit = outcome.record
            val record = AttendanceOverrideRecord(
                id = "ovr_${System.currentTimeMillis()}",
                customerId = visit.customerId,
                customerName = visit.customerName,
                membershipPlan = visit.membershipPlan,
                reason = reason,
                overrideStaff = staffName,
                originalStatus = "Manual Override",
                updatedStatus = "Attended",
                timestamp = "Just now"
            )
            sampleOverrides.add(0, record)
            logAdminAction(staffName, "Forced attendance override for ${visit.customerName} (${visit.membershipPlan}). Reason: $reason", "Attendance")
            addNotification(
                NotificationItem(
                    id = "notif_ovr_${System.currentTimeMillis()}",
                    title = "Manual Override Check-In",
                    message = "Forced check-in status to 'Attended' for ${visit.customerName}. Reason: $reason",
                    type = "Booking",
                    timestamp = "Just now",
                    isRead = false
                )
            )
        }
        return outcome
    }

    // ----------------------------------------------------
    // 2. MEMBERSHIP DEFERMENT & AUTO-RECALCULATED FREEZES
    // ----------------------------------------------------
    fun deferMembership(membershipId: String, freezeDays: Int, reason: String, staffName: String = "Kishore Kumar"): Boolean {
        val index = sampleMemberships.indexOfFirst { it.id == membershipId }
        if (index != -1) {
            val m = sampleMemberships[index]
            val updatedEndDate = "${m.endDate} (+${freezeDays}d deferred)"
            sampleMemberships[index] = m.copy(
                status = MemberStatus.FROZEN,
                endDate = updatedEndDate
            )
            setCustomerStatusFor(m, MemberStatus.FROZEN)
            requestMembershipFreeze(
                planName = m.planName,
                startDate = "Today",
                endDate = updatedEndDate,
                days = freezeDays,
                reason = reason
            )
            logAdminAction(staffName, "Placed membership for ${m.customerName} on hold ($freezeDays days deferred). Reason: $reason", "Memberships")
            return true
        }
        return false
    }

    // Ends a freeze early. The deferred end date is kept.
    fun resumeMembership(membershipId: String, staffName: String = "Kishore Kumar"): Boolean {
        val index = sampleMemberships.indexOfFirst { it.id == membershipId }
        if (index == -1 || sampleMemberships[index].status != MemberStatus.FROZEN) return false
        val m = sampleMemberships[index]
        sampleMemberships[index] = m.copy(status = MemberStatus.ACTIVE)
        setCustomerStatusFor(m, MemberStatus.ACTIVE)
        logAdminAction(staffName, "Unfroze membership for ${m.customerName}", "Memberships")
        return true
    }

    // Keeps the member record's status in sync with their membership
    private fun setCustomerStatusFor(membership: MembershipItem, status: String) {
        val index = sampleCustomers.indexOfFirst {
            it.email.equals(membership.customerEmail, ignoreCase = true) ||
                it.name.equals(membership.customerName, ignoreCase = true)
        }
        if (index != -1) {
            sampleCustomers[index] = sampleCustomers[index].copy(status = status)
        }
    }

    // ----------------------------------------------------
    // 3. TRAINER & SESSION SCHEDULING
    // ----------------------------------------------------
    private val sampleWorkoutSessions = mutableListOf(
        GroupWorkoutSession("ws_1", "HRX High-Intensity Training", "HRX", "Coach Vicky", "In-House", "Studio 1 (Main Arena)", "06:30 AM - 07:20 AM", 50, 25, 22, "Extreme", "Scheduled"),
        GroupWorkoutSession("ws_2", "Boxing Conditioning & Sparring", "Boxing", "Coach Ramesh", "In-House", "Studio 2 (Combat Zone)", "07:30 AM - 08:20 AM", 50, 20, 18, "High", "Scheduled"),
        GroupWorkoutSession("ws_3", "Power Flow & Mobility Yoga", "Yoga", "Coach Priya", "Freelance Certified", "Studio 3 (Zen Studio)", "09:00 AM - 09:50 AM", 50, 20, 14, "Medium", "Scheduled"),
        GroupWorkoutSession("ws_4", "Zumba Dance Cardio Party", "Dance", "Coach Anjali", "Freelance Certified", "Studio 1 (Main Arena)", "05:30 PM - 06:20 PM", 50, 30, 28, "High", "Scheduled"),
        GroupWorkoutSession("ws_5", "S&C Compound Strength", "Strength", "Coach Sanjay", "In-House", "Strength Floor Zone A", "07:00 PM - 07:50 PM", 50, 18, 16, "Extreme", "Scheduled")
    )

    fun getWorkoutSessions(): List<GroupWorkoutSession> = sampleWorkoutSessions

    fun assignProxyTrainer(sessionId: String, substituteCoach: String): Boolean {
        val index = sampleWorkoutSessions.indexOfFirst { it.id == sessionId }
        if (index != -1) {
            val s = sampleWorkoutSessions[index]
            sampleWorkoutSessions[index] = s.copy(
                coachName = substituteCoach,
                status = "Substitute Assigned",
                assignedProxyCoach = substituteCoach
            )
            logAdminAction("Kishore Kumar", "Confirmed substitute trainer '$substituteCoach' for session '${s.title}'", "Staff")
            return true
        }
        return false
    }

    // ----------------------------------------------------
    // 5. FIRST-TIME TRIAL ARRIVAL ALERT (visual only)
    // ----------------------------------------------------
    private var activeTrialAlert: TrialCheckInBuzzerAlert? = TrialCheckInBuzzerAlert(
        id = "alert_trial_01",
        memberName = "Sneha Joseph (First-Timer)",
        phone = "+91 96331 66778",
        sessionType = "Boxing & Cardio Kick",
        trialPassType = "2-Session Free Trial",
        centerName = "Iron House Fitness",
        arrivalTime = "Just now (Front desk)",
        isFirstTimeTrialist = true,
        isAudioAlertPlaying = true,
        isAcknowledged = false
    )

    fun getActiveTrialBuzzer(): TrialCheckInBuzzerAlert? = activeTrialAlert

    fun triggerTrialBuzzer(alert: TrialCheckInBuzzerAlert) {
        activeTrialAlert = alert
    }

    fun acknowledgeTrialAlert(alertId: String, greeterStaff: String = "Staff Sanjay") {
        val alert = activeTrialAlert
        if (alert != null && alert.id == alertId) {
            activeTrialAlert = alert.copy(
                isAudioAlertPlaying = false,
                isAcknowledged = true,
                assignedGreeter = greeterStaff
            )
            logAdminAction(greeterStaff, "Acknowledged first-time trial check-in buzzer for ${alert.memberName}. Assigned 5-min facility tour.", "Attendance")
        }
    }

    fun dismissTrialBuzzer() {
        activeTrialAlert = null
    }

    // ----------------------------------------------------
    // 8. POINT OF SALE (POS) & BILLING TRANSACTIONS
    // ----------------------------------------------------
    private val samplePosInvoices = mutableListOf(
        PosInvoiceItem("inv_1", "POS-2026-0891", "Rahul Menon", "+91 98471 22334", "Membership Renewal", "All-Access Elite (Annual)", 14999.0, 2699.82, 17698.82, "Razorpay POS Terminal", "Kishore Kumar", "Paid", "Today, 11:20 AM"),
        PosInvoiceItem("inv_2", "POS-2026-0890", "Deepak Varma", "+91 94471 55667", "On-Site Upgrade", "Upgrade to Standard Pro (3-Mo)", 3299.0, 593.82, 3892.82, "Dynamic UPI QR", "Staff Kavya", "Paid", "Today, 10:45 AM"),
        PosInvoiceItem("inv_3", "POS-2026-0889", "Ananya Nair", "+91 97455 33445", "Merch Purchase", "BYCE High-Performance Gym Kit", 1299.0, 233.82, 1532.82, "Card Swipe", "Kishore Kumar", "Paid", "Yesterday, 06:15 PM"),
        PosInvoiceItem("inv_4", "POS-2026-0888", "Rohan Pillai", "+91 94470 11223", "Billing Refund", "Unused Session Package Refund", -1499.0, -269.82, -1768.82, "Original Payment Mode", "Kishore Kumar", "Refunded", "24 Sep 2026")
    )

    fun getPosInvoices(): List<PosInvoiceItem> = samplePosInvoices

    fun createPosInvoice(
        customerName: String,
        customerPhone: String,
        type: String,
        itemName: String,
        amount: Double,
        paymentMode: String
    ): PosInvoiceItem {
        val gst = amount * 0.18
        val total = amount + gst
        val inv = PosInvoiceItem(
            id = "inv_${System.currentTimeMillis()}",
            invoiceNumber = "POS-2026-${(1000..9999).random()}",
            customerName = customerName,
            customerPhone = customerPhone,
            transactionType = type,
            itemName = itemName,
            baseAmount = amount,
            gstTax = gst,
            totalAmount = total,
            paymentMode = paymentMode,
            billingStaff = "Kishore Kumar",
            status = "Paid",
            timestamp = "Just now"
        )
        samplePosInvoices.add(0, inv)
        samplePayments.add(
            0,
            PaymentItem(
                transactionId = inv.invoiceNumber,
                customerName = customerName,
                planName = itemName,
                amount = total,
                provider = paymentMode,
                status = "Successful",
                date = "Today",
                orderId = inv.id,
                paymentId = "POS_APP",
                paidAt = "Just now"
            )
        )
        logAdminAction("Kishore Kumar", "Generated POS Invoice ${inv.invoiceNumber} for $customerName: ₹${total.toInt()} via $paymentMode", "Payments")
        return inv
    }

    // GET /api/v1/admin/payments/
    fun getPayments(): List<PaymentItem> = samplePayments

    // GET /api/v1/admin/bookings/
    fun getBookings(): List<BookingItem> = sampleBookings

    fun cancelBooking(bookingId: String) {
        val index = sampleBookings.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            sampleBookings[index] = sampleBookings[index].copy(status = "Cancelled", arrivalStatus = "Not Arrived")
            logAdminAction("Kishore Kumar", "Cancelled booking $bookingId", "Bookings")
        }
    }

    fun markBookingArrived(bookingId: String, reason: String? = null, adminName: String = "Kishore Kumar"): CheckInOutcome {
        val outcome = checkIn(bookingId = bookingId, reason = reason, source = "bookings", staffName = adminName)
        if (outcome is CheckInOutcome.Success) {
            val booking = sampleBookings.first { it.id == bookingId }
            logAdminAction(adminName, "Marked ${booking.customerName} as arrived / checked in for slot ${booking.time}", "Bookings")
        }
        return outcome
    }

    // Shift & 30-Minute Gmail Reminder Engine
    fun getShiftReminderSchedules(): List<ShiftReminderSchedule> {
        val morningCount = sampleBookings.count { it.shift.contains("Morning", ignoreCase = true) && it.status != "Cancelled" }
        val eveningCount = sampleBookings.count { it.shift.contains("Evening", ignoreCase = true) && it.status != "Cancelled" }
        val nightCount = sampleBookings.count { it.shift.contains("Night", ignoreCase = true) && it.status != "Cancelled" }

        return listOf(
            ShiftReminderSchedule("Morning Shift", "06:00 AM", "10:30 AM", "05:30 AM (30m before start)", morningCount, "Gmail"),
            ShiftReminderSchedule("Evening Shift", "04:00 PM", "07:30 PM", "03:30 PM (30m before start)", eveningCount, "Gmail"),
            ShiftReminderSchedule("Night Shift", "08:00 PM", "10:30 PM", "07:30 PM (30m before start)", nightCount, "Gmail")
        )
    }

    fun updateBookingReminderSchedule(bookingId: String, minutesBefore: Int, shift: String) {
        val index = sampleBookings.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            val b = sampleBookings[index]
            sampleBookings[index] = b.copy(
                shift = shift,
                reminderMinutesBefore = minutesBefore
            )
            logAdminAction(
                "Kishore Kumar",
                "Moved ${b.customerName}'s booking to $shift",
                "Bookings"
            )
        }
    }

    fun addNotification(item: NotificationItem) {
        sampleNotifications.add(0, item)
    }

    // Staff
    fun getStaff(): List<StaffMember> = sampleStaff

    fun addStaff(staff: StaffMember) {
        sampleStaff.add(0, staff)
    }

    fun toggleStaffStatus(staffId: String) {
        val index = sampleStaff.indexOfFirst { it.id == staffId }
        if (index != -1) {
            val s = sampleStaff[index]
            val newStatus = if (s.status == "Active") "Inactive" else "Active"
            sampleStaff[index] = s.copy(status = newStatus)
        }
    }

    // Notifications
    fun getNotifications(): List<NotificationItem> = sampleNotifications

    fun markNotificationAsRead(id: String) {
        val index = sampleNotifications.indexOfFirst { it.id == id }
        if (index != -1) {
            sampleNotifications[index] = sampleNotifications[index].copy(isRead = true)
        }
    }

    fun markAllNotificationsAsRead() {
        for (i in sampleNotifications.indices) {
            sampleNotifications[i] = sampleNotifications[i].copy(isRead = true)
        }
    }

    // ----------------------------------------------------
    // FEATURE 1: TODAY'S GYM OPERATIONS
    // ----------------------------------------------------
    fun getTodayOperations(): TodayOperationsSummary = TodayOperationsSummary(
        todayCheckins = 86,
        todayBookings = 42,
        activeMembersInside = 28,
        newMembershipsToday = 7,
        expiringSoonCount = 18,
        todayRevenue = 24500.0,
        pendingActionsCount = 5
    )

    // ----------------------------------------------------
    // FEATURE 2: ATTENDANCE / CHECK-IN RECORDS
    // ----------------------------------------------------
    private val sampleAttendance = mutableListOf(
        AttendanceItem("att_1", "c1", "Rahul Menon", "10:04 AM", "11:35 AM", "Yearly", "Completed", "Today", "06:00 PM Slot"),
        AttendanceItem("att_2", "c2", "Ananya Nair", "09:12 AM", null, "Monthly", "Checked In", "Today", "07:00 PM Slot"),
        AttendanceItem("att_3", "c3", "Mohammed Faizal", "08:15 AM", "09:45 AM", "Half Yearly", "Completed", "Today", "08:00 AM Slot"),
        AttendanceItem("att_4", "c4", "Deepak Varma", "09:32 AM", null, "Quarterly", "Checked In", "Today", "09:30 AM Slot"),
        AttendanceItem("att_5", "c8", "Vivek Raj", "07:05 AM", "08:30 AM", "Half Yearly", "Completed", "Today", null),
        AttendanceItem("att_6", "c6", "Arjun Prasad", "06:40 AM", "08:10 AM", "Yearly", "Completed", "Today", null),
        AttendanceItem("att_7", "c7", "Pooja Krishnan", "05:15 PM", "06:45 PM", "Quarterly", "Completed", "Yesterday", null),
        AttendanceItem("att_8", "c5", "Sneha Joseph", "06:20 PM", "07:35 PM", "Monthly", "Completed", "Yesterday", null),
        AttendanceItem("att_9", "c9", "Karthik Suresh", "10:15 AM", null, "Quarterly Pass", "Checked In", "Today", "10:00 AM Slot"),
        AttendanceItem("att_10", "c10", "Meera Nambiar", "10:28 AM", null, "Monthly Pass", "Checked In", "Today", null)
    )

    fun getAttendanceRecords(): List<AttendanceItem> = sampleAttendance

    // Attendance is stored newest first
    fun getLastCheckIn(customerId: String): AttendanceItem? =
        sampleAttendance.firstOrNull { it.customerId == customerId }

    fun getAttendanceSummary(): AttendanceSummary = AttendanceSummary(
        todayCheckins = 86,
        currentlyInside = 28,
        completedVisits = 58,
        avgDailyVisits = 112
    )

    // Writes the attendance record only; every caller must go through checkIn() first
    private fun recordCheckIn(customerId: String, name: String, plan: String = "Active Plan"): AttendanceItem {
        val newRecord = AttendanceItem(
            id = "att_${System.currentTimeMillis()}",
            customerId = customerId,
            customerName = name,
            checkInTime = "Just now",
            checkOutTime = null,
            membershipPlan = plan,
            status = "Checked In",
            date = "Today"
        )
        sampleAttendance.add(0, newRecord)
        logAdminAction("Kishore Kumar", "Recorded manual check-in for $name", "Attendance")
        return newRecord
    }

    fun recordCheckOut(attendanceId: String) {
        val index = sampleAttendance.indexOfFirst { it.id == attendanceId }
        if (index != -1) {
            val record = sampleAttendance[index]
            sampleAttendance[index] = record.copy(checkOutTime = "Just now", status = "Completed")
            logAdminAction("Kishore Kumar", "Recorded check-out for ${record.customerName}", "Attendance")
        }
    }

    // ----------------------------------------------------
    // CHECK-IN ELIGIBILITY (shared by scanner, manual check-in, override, bookings)
    // ----------------------------------------------------
    fun findCustomer(customerId: String): CustomerItem? = sampleCustomers.firstOrNull { it.id == customerId }

    fun searchCustomers(query: String, limit: Int = 6): List<CustomerItem> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val qDigits = q.filter { it.isDigit() }
        return sampleCustomers.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.id.equals(q, ignoreCase = true) ||
                it.email.contains(q, ignoreCase = true) ||
                (qDigits.length >= 4 && it.phone.filter { c -> c.isDigit() }.contains(qDigits))
        }.take(limit)
    }

    private fun isCheckedInToday(customerId: String): Boolean =
        sampleAttendance.any { it.customerId == customerId && it.date == "Today" && it.checkOutTime == null }

    fun evaluateMemberCheckIn(customerId: String): CheckInCheck {
        val customer = findCustomer(customerId)
            ?: return CheckInCheck(CheckInEligibility.BLOCKED, "No matching member found.")
        if (isCheckedInToday(customer.id)) {
            return CheckInCheck(CheckInEligibility.BLOCKED, "${customer.name} is already checked in.", customer)
        }
        return when (customer.status) {
            MemberStatus.ACTIVE -> CheckInCheck(CheckInEligibility.ALLOWED, "Active membership.", customer)
            MemberStatus.EXPIRED -> CheckInCheck(
                CheckInEligibility.NEEDS_REASON,
                "Membership expired. Check in only with a reason; it will be logged.",
                customer
            )
            MemberStatus.PENDING -> CheckInCheck(
                CheckInEligibility.NEEDS_REASON,
                "Membership payment is pending. Check in only with a reason; it will be logged.",
                customer
            )
            MemberStatus.FROZEN -> CheckInCheck(
                CheckInEligibility.BLOCKED_OVERRIDABLE,
                "Membership is paused (frozen). Check-in is blocked unless staff override with a reason.",
                customer
            )
            // Unrecognized statuses are never overridable
            else -> CheckInCheck(
                CheckInEligibility.BLOCKED,
                "Membership status \"${customer.status}\" isn't recognized. Check-in is blocked; update the member's status first.",
                customer
            )
        }
    }

    fun evaluateBookingCheckIn(bookingId: String): CheckInCheck {
        val booking = sampleBookings.firstOrNull { it.id == bookingId }
            ?: return CheckInCheck(CheckInEligibility.BLOCKED, "No matching booking found.")
        if (booking.status.equals("Cancelled", ignoreCase = true)) {
            return CheckInCheck(CheckInEligibility.BLOCKED, "This booking was cancelled.", booking = booking)
        }
        val alreadyArrived = booking.arrivalStatus in listOf("Checked In", "Attended") ||
            booking.status.equals("Completed", ignoreCase = true)
        if (alreadyArrived) {
            return CheckInCheck(CheckInEligibility.BLOCKED, "Already checked in for this booking.", booking = booking)
        }
        val customer = sampleCustomers.firstOrNull { it.name.equals(booking.customerName, ignoreCase = true) }
        // A booking without a member record (e.g. a day pass) is its own entitlement
        return if (customer != null) {
            evaluateMemberCheckIn(customer.id).copy(booking = booking)
        } else {
            CheckInCheck(CheckInEligibility.ALLOWED, "Booking pass without a member record.", booking = booking)
        }
    }

    fun checkIn(
        customerId: String? = null,
        bookingId: String? = null,
        reason: String? = null,
        source: String,
        staffName: String = "Kishore Kumar"
    ): CheckInOutcome {
        val check = when {
            bookingId != null -> evaluateBookingCheckIn(bookingId)
            customerId != null -> evaluateMemberCheckIn(customerId)
            else -> return CheckInOutcome.Rejected("No member or booking selected.")
        }
        when (check.eligibility) {
            CheckInEligibility.BLOCKED -> return CheckInOutcome.Rejected(check.message)
            CheckInEligibility.NEEDS_REASON, CheckInEligibility.BLOCKED_OVERRIDABLE ->
                if (reason.isNullOrBlank()) return CheckInOutcome.Rejected("A reason is required. ${check.message}")
            CheckInEligibility.ALLOWED -> Unit
        }

        val customer = check.customer
        val booking = check.booking
        val name = customer?.name ?: booking!!.customerName
        val record = recordCheckIn(
            customerId = customer?.id ?: booking!!.id,
            name = name,
            plan = customer?.membershipPlan ?: booking!!.membershipPlan
        )
        if (booking != null) {
            val index = sampleBookings.indexOfFirst { it.id == booking.id }
            sampleBookings[index] = booking.copy(arrivalStatus = "Checked In", status = "Completed")
        }
        if (check.eligibility != CheckInEligibility.ALLOWED) {
            logAdminAction(
                staffName,
                "Check-in override for $name (${customer?.status ?: "no member record"}) via $source. Reason: $reason",
                "Attendance"
            )
        }
        return CheckInOutcome.Success(record)
    }

    // ----------------------------------------------------
    // FEATURE 3: GYM QR CHECK-IN MANAGEMENT
    // ----------------------------------------------------
    private var gymQrPayload = "BYCE-GYM-IRON-HOUSE-KOZHIKODE-673004-AUTHENTICATED"

    fun getGymQrPayload(): String = gymQrPayload

    fun regenerateGymQrPayload(): String {
        gymQrPayload = "BYCE-GYM-IRON-HOUSE-KOZHIKODE-${System.currentTimeMillis()}"
        logAdminAction("Kishore Kumar", "Regenerated Gym Check-in QR Payload", "Gym QR")
        return gymQrPayload
    }

    // ----------------------------------------------------
    // FEATURE 4: MEMBERSHIPS EXPIRING SOON
    // ----------------------------------------------------
    private val sampleExpiringMemberships = listOf(
        ExpiringMembershipItem("c5", "Sneha Joseph", "Monthly Plan", "17 Sep 2026", 2, "Expires in 2 days"),
        ExpiringMembershipItem("c2", "Ananya Nair", "Monthly Plan", "30 Sep 2026", 6, "Expires in 6 days"),
        ExpiringMembershipItem("c7", "Pooja Krishnan", "Quarterly Plan", "08 Oct 2026", 14, "Expires in 14 days"),
        ExpiringMembershipItem("c4", "Deepak Varma", "Quarterly Plan", "11 Nov 2026", 28, "Expires in 28 days")
    )

    fun getExpiringMemberships(filterDays: Int = 30): List<ExpiringMembershipItem> {
        return sampleExpiringMemberships.filter { it.daysRemaining <= filterDays }
    }

    // ----------------------------------------------------
    // FEATURE 5: ABSENT / INACTIVE CUSTOMERS
    // ----------------------------------------------------
    private val sampleInactiveCustomers = listOf(
        InactiveCustomerItem("c5", "Sneha Joseph", "17 Sep 2026", 7, "Monthly Plan", "17 Sep 2026", "Inactive"),
        InactiveCustomerItem("c7", "Pooja Krishnan", "10 Sep 2026", 14, "Quarterly Plan", "19 Sep 2026", "Expired"),
        InactiveCustomerItem("c9", "Rohan Pillai", "24 Aug 2026", 31, "Monthly Plan", "09 Sep 2026", "Cancelled"),
        InactiveCustomerItem("c10", "Fahad Fazil", "12 Aug 2026", 43, "Half Yearly", "15 Feb 2027", "At Risk")
    )

    fun getInactiveCustomers(filterDays: Int = 30): List<InactiveCustomerItem> {
        return sampleInactiveCustomers.filter { it.daysSinceVisit >= filterDays }
    }

    // ----------------------------------------------------
    // FEATURE 6: CUSTOMER CRM NOTES
    // ----------------------------------------------------
    private val sampleCustomerNotes = mutableMapOf(
        "c1" to mutableListOf(
            CustomerCrmNote("n1", "c1", "Customer requested membership renewal information.", "Kishore Kumar", "21 Sep 2026, 11:30 AM"),
            CustomerCrmNote("n2", "c1", "Prefers early morning slots (06:00 AM). Provided locker key #42.", "Sanjay Nambiar", "10 Jan 2026, 08:15 AM")
        ),
        "c2" to mutableListOf(
            CustomerCrmNote("n3", "c2", "Customer visited reception regarding payment settlement.", "Kavya Sreedharan", "Today, 09:30 AM")
        ),
        "c4" to mutableListOf(
            CustomerCrmNote("n4", "c4", "Requested receipt copy for company reimbursement.", "Sanjay Nambiar", "18 Aug 2026, 04:00 PM")
        )
    )

    fun getCustomerNotes(customerId: String): List<CustomerCrmNote> {
        return sampleCustomerNotes[customerId] ?: emptyList()
    }

    fun addCustomerNote(customerId: String, note: String, author: String = "Kishore Kumar") {
        val list = sampleCustomerNotes.getOrPut(customerId) { mutableListOf() }
        list.add(0, CustomerCrmNote(
            id = "note_${System.currentTimeMillis()}",
            customerId = customerId,
            note = note,
            createdBy = author,
            createdAt = "Today, just now"
        ))
        logAdminAction(author, "Added CRM note for customer ID $customerId", "CRM Notes")
    }

    fun deleteCustomerNote(customerId: String, noteId: String) {
        sampleCustomerNotes[customerId]?.removeAll { it.id == noteId }
        logAdminAction("Kishore Kumar", "Removed CRM note $noteId", "CRM Notes")
    }

    // ----------------------------------------------------
    // FEATURE 7: CUSTOMER ACTIVITY TIMELINE
    // ----------------------------------------------------
    private val sampleCustomerActivities = mapOf(
        "c1" to listOf(
            CustomerActivityEvent("act1", "c1", "Gym Check-in", "Checked in at reception scanner", "24 Sep", "10:04 AM", "CheckIn"),
            CustomerActivityEvent("act2", "c1", "Booking Confirmed", "Reserved workout slot 06:00 PM", "24 Sep", "08:30 AM", "Booking"),
            CustomerActivityEvent("act3", "c1", "Admin Note Added", "Customer requested membership renewal info", "21 Sep", "11:30 AM", "Note"),
            CustomerActivityEvent("act4", "c1", "Payment Successful", "Renewed Yearly Plan - ₹11,999", "18 Sep", "10:24 AM", "Payment"),
            CustomerActivityEvent("act5", "c1", "Membership Purchased", "Annual VIP Membership activated", "10 Jan", "09:00 AM", "Membership")
        ),
        "c2" to listOf(
            CustomerActivityEvent("act6", "c2", "Gym Check-in", "Checked in at front desk scanner", "24 Sep", "09:12 AM", "CheckIn"),
            CustomerActivityEvent("act7", "c2", "Payment Successful", "Monthly Plan Renewed - ₹1,499", "24 Sep", "09:15 AM", "Payment"),
            CustomerActivityEvent("act8", "c2", "Booking Created", "Workout slot booked 07:00 PM", "23 Sep", "07:30 PM", "Booking"),
            CustomerActivityEvent("act9", "c2", "Membership Activated", "Monthly Membership started", "01 Sep", "10:00 AM", "Membership")
        )
    )

    // Members without recorded events get an empty timeline, not invented history
    fun getCustomerActivityTimeline(customerId: String): List<CustomerActivityEvent> {
        return sampleCustomerActivities[customerId] ?: emptyList()
    }

    // ----------------------------------------------------
    // FEATURE 9: OUTSTANDING / PENDING PAYMENTS
    // ----------------------------------------------------
    private val samplePendingPayments = mutableListOf(
        PendingPaymentItem("p1", "Arjun Prasad", "Yearly Plan", 11999.0, "Due in 2 days", "Pending", "+91 98471 22345"),
        PendingPaymentItem("p2", "Sneha Joseph", "Monthly Plan Renewal", 1499.0, "Due Yesterday", "Overdue", "+91 94462 88123"),
        PendingPaymentItem("p3", "Karthik Suresh", "Quarterly Pass", 3999.0, "Due 21 Sep 2026", "Failed / Due", "+91 97455 19283"),
        PendingPaymentItem("p4", "Rohit Nambiar", "Gym Locker Fee", 500.0, "Due in 5 days", "Pending", "+91 96330 45678"),
        PendingPaymentItem("p5", "Meera Menon", "Monthly Pass", 1499.0, "Due in 3 days", "Pending", "+91 99951 84729")
    )

    fun getPendingPayments(): List<PendingPaymentItem> = samplePendingPayments

    fun recordPendingPaymentPaid(paymentId: String, recordedBy: String = "Kishore Kumar") {
        val index = samplePendingPayments.indexOfFirst { it.id == paymentId }
        if (index != -1) {
            val item = samplePendingPayments.removeAt(index)
            logAdminAction(recordedBy, "Recorded manual payment for ${item.customerName} (₹${item.amount.toInt()})", "Payments")
        }
    }

    // ----------------------------------------------------
    // FEATURE 11: GYM CAPACITY & LIVE OCCUPANCY
    // ----------------------------------------------------
    private var gymOccupancy = GymOccupancy(
        currentInside = 42,
        capacity = 100,
        percentage = 42,
        status = "Normal",
        peakPeriod = "06:00 PM - 08:30 PM",
        dailyVisits = 148
    )

    fun getGymOccupancy(): GymOccupancy = gymOccupancy

    // ----------------------------------------------------
    // FEATURE 12 & 15: GYM OPERATING STATUS & VISIBILITY
    // ----------------------------------------------------
    private var gymOperatingStatus = GymOperatingStatus(
        status = "OPEN",
        openingTime = "05:30 AM",
        closingTime = "10:30 PM",
        visibilityStatus = "ACTIVE"
    )

    fun getGymOperatingStatus(): GymOperatingStatus = gymOperatingStatus

    fun updateGymOperatingStatus(newStatus: String) {
        gymOperatingStatus = gymOperatingStatus.copy(status = newStatus)
        logAdminAction("Kishore Kumar", "Changed gym operating status to $newStatus", "Gym Operations")
    }

    // ----------------------------------------------------
    // FEATURE 13: GYM PROFILE COMPLETENESS
    // ----------------------------------------------------
    fun getGymProfileCompleteness(): GymProfileCompleteness = GymProfileCompleteness(
        percentage = 85,
        missingFields = listOf("Gym Cover Photo", "Alternate Support Phone", "Instagram URL")
    )

    // ----------------------------------------------------
    // FEATURE 16: OPERATIONAL ALERTS
    // ----------------------------------------------------
    fun getOperationalAlerts(): List<OperationalAlert> = listOf(
        OperationalAlert("al_1", "18 memberships expire within the next 7 days.", "Membership", "High"),
        OperationalAlert("al_2", "5 pending payments require follow-up settlement.", "Payment", "High"),
        OperationalAlert("al_3", "3 workout reservations cancelled today.", "Booking", "Medium"),
        OperationalAlert("al_4", "Gym profile is 85% complete. 2 fields require attention.", "Profile", "Info")
    )

    // ----------------------------------------------------
    // FEATURE 20: ADMIN AUDIT / ACTIVITY LOG
    // ----------------------------------------------------
    private val sampleActivityLogs = mutableListOf(
        AdminActivityLogItem("log_1", "Kishore Kumar", "Regenerated Gym Check-in QR Payload", "Gym QR", "Today, 10:14 AM"),
        AdminActivityLogItem("log_2", "Sanjay Nambiar", "Recorded manual check-in for Rahul Menon", "Attendance", "Today, 10:04 AM"),
        AdminActivityLogItem("log_3", "Kishore Kumar", "Updated Quarterly Membership Plan pricing to ₹3,999", "Plans", "Today, 09:30 AM"),
        AdminActivityLogItem("log_4", "Kavya Sreedharan", "Added CRM note for Ananya Nair", "Customers", "Today, 09:16 AM"),
        AdminActivityLogItem("log_5", "Kishore Kumar", "Verified bank settlement for TXN-984210", "Payments", "Yesterday, 06:45 PM"),
        AdminActivityLogItem("log_6", "Sanjay Nambiar", "Changed operating status to OPEN", "Gym Status", "Yesterday, 05:30 AM")    )

    fun getAdminActivityLogs(): List<AdminActivityLogItem> = sampleActivityLogs

    fun logAdminAction(adminName: String, action: String, module: String) {
        sampleActivityLogs.add(0, AdminActivityLogItem(
            id = "log_${System.currentTimeMillis()}",
            adminName = adminName,
            action = action,
            module = module,
            timestamp = "Today, just now"
        ))
    }

    // ----------------------------------------------------
    // UNIVERSAL QR & BARCODE SCANNER ENGINE
    // ----------------------------------------------------
    fun processScannedQrPayload(payload: String, formatName: String = "QR Code"): ScannedQrResult {
        val trimmed = payload.trim()
        // IDs and emails must match a whole token, so "abc1" does not match customer "c1"
        val tokens = trimmed.split(Regex("[^A-Za-z0-9@._+]+")).filter { it.isNotEmpty() }
        val digitsOnly = trimmed.filter { it.isDigit() }
        fun hasToken(value: String) = tokens.any { it.equals(value, ignoreCase = true) }

        // 1. Gym Facility / Locker QR
        if (trimmed.startsWith("BYCE-GYM-", ignoreCase = true)) {
            return ScannedQrResult(
                rawPayload = trimmed,
                type = ScannedQrType.GYM_FACILITY,
                title = "Iron House Fitness Facility Pass",
                subtitle = "Kozhikode, Kerala · Access Gate / Reception Verified",
                badgeText = "🏋️ Gym Facility Token",
                formatName = formatName,
                isRecognizedGymEntity = true
            )
        }

        // 2. Booking QR Pass
        val matchedBooking = if (trimmed.startsWith("BYCE-BOOKING-", ignoreCase = true)) {
            sampleBookings.firstOrNull { hasToken(it.id) }
                ?: sampleBookings.firstOrNull { trimmed.contains(it.customerName, ignoreCase = true) }
        } else {
            null
        }
        if (matchedBooking != null) {
            val bookingCustomer = sampleCustomers.firstOrNull { it.name.equals(matchedBooking.customerName, ignoreCase = true) }
            return ScannedQrResult(
                rawPayload = trimmed,
                type = ScannedQrType.BOOKING_PASS,
                title = matchedBooking.customerName,
                subtitle = "${matchedBooking.shift} (${matchedBooking.time})",
                badgeText = if (matchedBooking.status.equals("Cancelled", ignoreCase = true)) "⚠️ Booking Cancelled" else "📅 Slot Reservation Found",
                customerId = bookingCustomer?.id,
                bookingId = matchedBooking.id,
                customerName = matchedBooking.customerName,
                membershipPlan = matchedBooking.membershipPlan,
                status = matchedBooking.status,
                shift = matchedBooking.shift,
                time = matchedBooking.time,
                formatName = formatName,
                isRecognizedGymEntity = true
            )
        }

        // 3. Member Check-in QR Pass / Digital ID
        val matchedCustomer = sampleCustomers.firstOrNull { customer ->
            val phoneDigits = customer.phone.filter { it.isDigit() }
            trimmed.contains(customer.name, ignoreCase = true) ||
            hasToken(customer.id) ||
            hasToken(customer.email) ||
            (phoneDigits.length >= 10 && digitsOnly.contains(phoneDigits.takeLast(10)))
        }

        if (matchedCustomer != null) {
            val cust = matchedCustomer
            val activeShift = when {
                trimmed.contains("Evening", ignoreCase = true) -> "Evening Shift"
                trimmed.contains("Night", ignoreCase = true) -> "Night Shift"
                else -> "Morning Shift"
            }

            return ScannedQrResult(
                rawPayload = trimmed,
                type = ScannedQrType.MEMBER_CHECKIN,
                title = cust.name,
                subtitle = "ID: ${cust.id.uppercase()} · ${cust.membershipPlan}",
                badgeText = if (cust.status == "Active") "✓ Active Member Pass" else "⚠️ Member ${cust.status}",
                customerId = cust.id,
                customerName = cust.name,
                membershipPlan = cust.membershipPlan,
                status = cust.status,
                shift = activeShift,
                time = "Today, Just now",
                formatName = formatName,
                isRecognizedGymEntity = true
            )
        }

        // 4. Unrecognized code: barcode, URL, UPI, or a pass that matches no member or booking
        val isUrl = trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)
        return ScannedQrResult(
            rawPayload = trimmed,
            type = ScannedQrType.UNIVERSAL_CODE,
            title = if (isUrl) "Web Link / URL Code" else "Unrecognized Code",
            subtitle = if (trimmed.length > 50) trimmed.take(47) + "..." else trimmed,
            badgeText = "⚠️ No matching member or booking",
            formatName = formatName,
            isRecognizedGymEntity = false
        )
    }

    // Eligibility for a scan result; unrecognized scans are always blocked
    fun evaluateScanCheckIn(result: ScannedQrResult): CheckInCheck {
        if (!result.isRecognizedGymEntity) {
            return CheckInCheck(CheckInEligibility.BLOCKED, "This code doesn't match any member or booking.")
        }
        return when {
            result.type == ScannedQrType.BOOKING_PASS && result.bookingId != null -> evaluateBookingCheckIn(result.bookingId)
            result.type == ScannedQrType.MEMBER_CHECKIN && result.customerId != null -> evaluateMemberCheckIn(result.customerId)
            else -> CheckInCheck(CheckInEligibility.BLOCKED, "This code isn't a member or booking pass.")
        }
    }

    fun confirmQrCheckIn(result: ScannedQrResult, reason: String? = null): CheckInOutcome {
        val check = evaluateScanCheckIn(result)
        if (check.customer == null && check.booking == null) {
            return CheckInOutcome.Rejected(check.message)
        }
        val outcome = checkIn(
            customerId = check.customer?.id,
            bookingId = check.booking?.id,
            reason = reason,
            source = "QR scanner"
        )
        if (outcome is CheckInOutcome.Success) {
            val visit = outcome.record
            addNotification(
                NotificationItem(
                    id = "notif_scan_${System.currentTimeMillis()}",
                    title = "QR Scanner Check-in",
                    message = "${visit.customerName} (${result.shift ?: "Shift"}) checked in successfully via Camera QR Scanner.",
                    type = "Attendance",
                    timestamp = "Just now",
                    isRead = false
                )
            )
            logAdminAction("Kishore Kumar", "Scanner check-in recorded for ${visit.customerName} (${visit.membershipPlan})", "QR Scanner")
        }
        return outcome
    }
}
