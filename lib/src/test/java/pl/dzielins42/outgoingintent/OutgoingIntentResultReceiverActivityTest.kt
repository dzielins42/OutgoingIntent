package pl.dzielins42.outgoingintent

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.assertj.android.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class OutgoingIntentResultReceiverActivityTest {
    @Test
    fun `when receives Uri then forwards it to OutgoingIntentManagementActivity and finishes`() {
        // Arrange
        val redirectUri = Uri.parse("https://www.example.com/redirect")
        val redirectIntent = Intent().apply { data = redirectUri }
        val activityController = Robolectric.buildActivity(
            OutgoingIntentResultReceiverActivity::class.java, redirectIntent
        )

        // Act
        activityController.create()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        assertThat(activityShadow.nextStartedActivity)
            .hasFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .hasComponent(
                ComponentName(
                    ApplicationProvider.getApplicationContext(),
                    OutgoingIntentManagementActivity::class.java
                )
            )
            .hasData(redirectUri)
        assertThat(activity).isFinishing
    }
}