package com.whereduck.app.ui.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.ui.theme.DuckTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Privacy Policy",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            LegalSectionTitle("Last updated: July 2026")

            Spacer(modifier = Modifier.height(16.dp))

            LegalBody(
                "WhereTheDuck (\"the App\") is developed by Incolor Where Available. " +
                "This Privacy Policy explains how we collect, use, and protect your information."
            )

            LegalSection("1. Information We Collect") {
                LegalBullet("Account information", "When you sign in with Google, we receive your name, email address, and profile picture. This is used solely for authentication and personalization.")
                LegalBullet("Contacts", "The contacts you add within the App (by email invitation) are stored in our cloud database (Google Firebase) to enable sending and receiving Duck notifications.")
                LegalBullet("Notification history", "We store a history of Duck notifications sent and received (timestamps, sender, receiver, alert level, response) to provide you with statistics and activity history.")
                LegalBullet("Profile data", "Your display name, profile picture, and motto are stored in Firebase and visible to your contacts.")
                LegalBullet("Device information", "We collect your Firebase Cloud Messaging (FCM) token to deliver push notifications. We may also collect anonymous device information for crash reporting.")
            }

            LegalSection("2. How We Use Your Information") {
                LegalBody("We use your information to:")
                LegalBulletSimple("Deliver Duck notifications to your contacts and receive them")
                LegalBulletSimple("Display your profile to your contacts")
                LegalBulletSimple("Provide notification history and statistics")
                LegalBulletSimple("Enable group creation and management")
                LegalBulletSimple("Display relevant advertisements (Free users)")
                LegalBulletSimple("Improve the App through crash reporting and analytics")
            }

            LegalSection("3. Third-Party Services") {
                LegalBody("The App uses the following third-party services:")
                LegalBullet("Google Firebase", "Authentication, cloud storage (Firestore), push notifications (FCM), file storage (Cloud Storage), and Cloud Functions.")
                LegalBullet("Google AdMob", "Advertising for Free tier users. AdMob may collect device identifiers and usage data for ad personalization. See Google's Privacy Policy for details.")
            }

            LegalSection("4. Data Storage and Security") {
                LegalBody(
                    "Your data is stored in Google Firebase servers. " +
                    "We implement industry-standard security measures including Firebase Security Rules to protect your data. " +
                    "However, no method of electronic storage is 100% secure."
                )
            }

            LegalSection("5. Data Sharing") {
                LegalBody(
                    "We do not sell your personal information. Your data may be shared only in the following cases:"
                )
                LegalBulletSimple("With users you explicitly add as contacts (they can see your name, photo, motto, and notification history between you)")
                LegalBulletSimple("With third-party service providers as described above")
                LegalBulletSimple("When required by law or to protect our rights")
            }

            LegalSection("6. Your Rights") {
                LegalBody("You have the right to:")
                LegalBulletSimple("Access your data at any time through the App")
                LegalBulletSimple("Delete your account and all associated data from the Settings screen")
                LegalBulletSimple("Remove contacts to stop receiving notifications from them")
                LegalBulletSimple("Opt out of personalized advertising through your device settings")
                LegalBulletSimple("Withdraw consent at any time by deleting the App")
            }

            LegalSection("7. Children's Privacy") {
                LegalBody(
                    "The App is not intended for children under 13. We do not knowingly collect personal information from children under 13. " +
                    "If you believe we have collected such information, please contact us immediately."
                )
            }

            LegalSection("8. Changes to This Policy") {
                LegalBody(
                    "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new policy in the App. " +
                    "Continued use of the App after changes constitutes acceptance of the updated policy."
                )
            }

            LegalSection("9. Contact Us") {
                LegalBody(
                    "If you have questions about this Privacy Policy, please contact us at:\n\nincolorwhereavailable@gmail.com"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Terms of Service",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            LegalSectionTitle("Last updated: July 2026")

            Spacer(modifier = Modifier.height(16.dp))

            LegalBody(
                "Please read these Terms of Service (\"Terms\") carefully before using WhereTheDuck (\"the App\"). " +
                "By using the App, you agree to be bound by these Terms."
            )

            LegalSection("1. Description of Service") {
                LegalBody(
                    "WhereTheDuck is a social notification application that allows users to send loud push notifications " +
                    "to their contacts. These notifications are designed to bypass the device's silent mode and Do Not Disturb settings. " +
                    "The App includes a \"Zen Mode\" that allows users to temporarily block incoming notifications."
                )
            }

            LegalSection("2. User Accounts") {
                LegalBody(
                    "You must sign in with a Google account to use the App. " +
                    "You are responsible for maintaining the security of your account credentials. " +
                    "You must provide accurate information when creating an account."
                )
            }

            LegalSection("3. Acceptable Use") {
                LegalBody("You agree not to:")
                LegalBulletSimple("Use the App to harass, bully, or intimidate others")
                LegalBulletSimple("Send notifications with the intent to cause harm or distress")
                LegalBulletSimple("Use the App for any unlawful purpose")
                LegalBulletSimple("Attempt to gain unauthorized access to the App's systems")
                LegalBulletSimple("Interfere with or disrupt the App's functionality")
                LegalBulletSimple("Reverse engineer, decompile, or disassemble the App")
            }

            LegalSection("4. Notification Disclaimer") {
                LegalBody(
                    "By using the App, you acknowledge and accept that you may receive loud notifications that bypass " +
                    "your device's silent mode and Do Not Disturb settings. You can use the \"Zen Mode\" feature or the " +
                    "mute function to temporarily stop receiving notifications. " +
                    "By adding someone as a contact, you consent to receiving Duck notifications from them."
                )
            }

            LegalSection("5. Subscriptions and Payments") {
                LegalBody(
                    "Premium features may require a one-time purchase or subscription through Google Play. " +
                    "Payments are processed by Google Play and are subject to Google Play's terms. " +
                    "Refunds are handled according to Google Play's refund policy."
                )
            }

            LegalSection("6. Intellectual Property") {
                LegalBody(
                    "The App, including its design, code, graphics, sounds, and content, is owned by Incolor Where Available and protected by copyright laws. " +
                    "You are granted a limited, non-exclusive license to use the App for personal, non-commercial purposes."
                )
            }

            LegalSection("7. Limitation of Liability") {
                LegalBody(
                    "TO THE MAXIMUM EXTENT PERMITTED BY LAW, INCOLOR WHERE AVAILABLE SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, " +
                    "SPECIAL, OR CONSEQUENTIAL DAMAGES ARISING FROM YOUR USE OF THE APP. " +
                    "THIS INCLUDES BUT IS NOT LIMITED TO DISRUPTIONS CAUSED BY NOTIFICATIONS, DAMAGE TO PERSONAL RELATIONSHIPS, " +
                    "OR ANY CONSEQUENCES OF RECEIVING LOUD NOTIFICATIONS IN INAPPROPRIATE SETTINGS. " +
                    "USE THE APP AT YOUR OWN RISK."
                )
            }

            LegalSection("8. Termination") {
                LegalBody(
                    "We may suspend or terminate your access to the App at any time, with or without cause. " +
                    "You may stop using the App at any time by uninstalling it. " +
                    "Upon termination, your right to use the App ceases immediately."
                )
            }

            LegalSection("9. Changes to Terms") {
                LegalBody(
                    "We reserve the right to modify these Terms at any time. " +
                    "We will notify you of significant changes through the App. " +
                    "Continued use after changes constitutes acceptance of the new Terms."
                )
            }

            LegalSection("10. Governing Law") {
                LegalBody(
                    "These Terms are governed by the laws of Italy. " +
                    "Any disputes shall be resolved in the courts of Italy."
                )
            }

            LegalSection("11. Contact Us") {
                LegalBody(
                    "If you have questions about these Terms, please contact us at:\n\nincolorwhereavailable@gmail.com"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LegalSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = DuckTheme.colors.textSecondary
    )
}

@Composable
private fun LegalSection(title: String, content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DuckTheme.colors.textPrimary
    )
    Spacer(modifier = Modifier.height(8.dp))
    content()
}

@Composable
private fun LegalBody(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = DuckTheme.colors.textPrimary.copy(alpha = 0.8f),
        lineHeight = 22.sp
    )
}

@Composable
private fun LegalBullet(title: String, description: String) {
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "\u2022 $title: $description",
        fontSize = 14.sp,
        color = DuckTheme.colors.textPrimary.copy(alpha = 0.8f),
        lineHeight = 22.sp,
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
private fun LegalBulletSimple(text: String) {
    Text(
        text = "\u2022 $text",
        fontSize = 14.sp,
        color = DuckTheme.colors.textPrimary.copy(alpha = 0.8f),
        lineHeight = 22.sp,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    )
}
