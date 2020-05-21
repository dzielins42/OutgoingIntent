package pl.dzielins42.outgoingintent

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.assertj.android.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class OutgoingIntentManagementActivityTest {

    private val outgoingIntent: Intent = Intent("OUTGOING")
    private val completeIntent = Intent("COMPLETE")
    private val completePendingIntent: PendingIntent = PendingIntent.getActivity(
        ApplicationProvider.getApplicationContext(),
        0,
        completeIntent,
        0
    )
    private val cancelIntent = Intent("CANCEL")
    private val cancelPendingIntent: PendingIntent = PendingIntent.getActivity(
        ApplicationProvider.getApplicationContext(),
        0,
        cancelIntent,
        0
    )

    @Test
    fun `when started then starts new Activity from outgoingIntent`() {
        // Arrange
        val activityController = instantiateActivity(
            OutgoingIntentManagementActivity.newIntent(
                ApplicationProvider.getApplicationContext(),
                outgoingIntent,
                completePendingIntent,
                cancelPendingIntent
            )
        )
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)

        // Act
        activityController.create().start().resume()

        // Assert
        assertThat(activityShadow.nextStartedActivity).hasAction(outgoingIntent.action)
    }

    //region With PendingIntents

    //region Success

    @Test
    fun `when resumed with URI and has PendingIntent then sends completeIntent`() {
        // Arrange
        val successUri = Uri.parse("SUCCESS")
        val activityController = instantiateActivityWithPendingIntents()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    successUri
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        val nextActivity = activityShadow.nextStartedActivity
        assertThat(nextActivity)
            .hasAction(completeIntent.action)
            .hasData(successUri)
        assertThat(activity).isFinishing
    }

    @Test
    fun `when destroyed and resumed with URI and has PendingIntent then sends completeIntent`() {
        // Arrange
        val successUri = Uri.parse("SUCCESS")
        var activityController = instantiateActivityWithPendingIntents()
        val savedState = Bundle()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .stop()
            .saveInstanceState(savedState)
            .destroy()

        activityController = instantiateActivityWithPendingIntents()
        activityController
            .create(savedState)
            .start()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    successUri
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        val nextActivity = activityShadow.nextStartedActivity
        assertThat(nextActivity)
            .hasAction(completeIntent.action)
            .hasData(successUri)
        assertThat(activity).isFinishing
    }

    //endregion

    //region Cancel/Failure

    @Test
    fun `when resumed without URI and has PendingIntent then sends cancelIntent`() {
        // Arrange
        val activityController = instantiateActivityWithPendingIntents()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    null
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        val nextActivity = activityShadow.nextStartedActivity
        assertThat(nextActivity)
            .hasAction(cancelIntent.action)
        assertThat(activity).isFinishing
    }

    @Test
    fun `when destroyed and resumed without URI and has PendingIntent then sends cancelIntent`() {
        // Arrange
        var activityController = instantiateActivityWithPendingIntents()
        val savedState = Bundle()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .stop()
            .saveInstanceState(savedState)
            .destroy()

        activityController = instantiateActivityWithPendingIntents()
        activityController
            .create(savedState)
            .start()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    null
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        val nextActivity = activityShadow.nextStartedActivity
        assertThat(nextActivity)
            .hasAction(cancelIntent.action)
        assertThat(activity).isFinishing
    }

    //endregion

    //endregion

    //region Without PendingIntents

    //region Success

    @Test
    fun `when resumed with URI and does not have PendingIntent then sends completeIntent`() {
        // Arrange
        val successUri = Uri.parse("SUCCESS")
        val activityController = instantiateActivityWithoutPendingIntents()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    successUri
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        assertThat(activityShadow.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(activityShadow.resultIntent).hasData(successUri)
        assertThat(activity).isFinishing
    }

    @Test
    fun `when destroyed and resumed with URI and does not have PendingIntent then sends completeIntent`() {
        // Arrange
        val successUri = Uri.parse("SUCCESS")
        var activityController = instantiateActivityWithoutPendingIntents()
        val savedState = Bundle()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .stop()
            .saveInstanceState(savedState)
            .destroy()

        activityController = instantiateActivityWithPendingIntents()
        activityController
            .create(savedState)
            .start()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    successUri
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        assertThat(activityShadow.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(activityShadow.resultIntent).hasData(successUri)
        assertThat(activity).isFinishing
    }

    //endregion

    //region Cancel/Failure

    @Test
    fun `when resumed without URI and does not have PendingIntent then sends cancelIntent`() {
        // Arrange
        val activityController = instantiateActivityWithPendingIntents()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    null
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        assertThat(activityShadow.resultCode).isEqualTo(Activity.RESULT_CANCELED)
        assertThat(activity).isFinishing
    }

    @Test
    fun `when destroyed and resumed without URI and does not have PendingIntent then sends cancelIntent`() {
        // Arrange
        var activityController = instantiateActivityWithPendingIntents()
        val savedState = Bundle()

        // Act
        activityController
            .create()
            .start()
            .resume()
            .pause()
            .stop()
            .saveInstanceState(savedState)
            .destroy()

        activityController = instantiateActivityWithPendingIntents()
        activityController
            .create(savedState)
            .start()
            .newIntent(
                OutgoingIntentManagementActivity.newIntent(
                    ApplicationProvider.getApplicationContext(),
                    null
                )
            )
            .resume()

        // Assert
        val activity = activityController.get()
        val activityShadow = shadowOf(activity)
        assertThat(activityShadow.resultCode).isEqualTo(Activity.RESULT_CANCELED)
        assertThat(activity).isFinishing
    }

    //endregion

    //endregion

    private fun instantiateActivityWithPendingIntents(): ActivityController<OutgoingIntentManagementActivity> {
        return instantiateActivity(
            OutgoingIntentManagementActivity.newIntent(
                ApplicationProvider.getApplicationContext(),
                outgoingIntent,
                completePendingIntent,
                cancelPendingIntent
            )
        )
    }

    private fun instantiateActivityWithoutPendingIntents(): ActivityController<OutgoingIntentManagementActivity> {
        return instantiateActivity(
            OutgoingIntentManagementActivity.newIntent(
                ApplicationProvider.getApplicationContext(),
                outgoingIntent
            )
        )
    }

    private fun instantiateActivity(
        startIntent: Intent
    ): ActivityController<OutgoingIntentManagementActivity> {
        return Robolectric.buildActivity(
            OutgoingIntentManagementActivity::class.java,
            startIntent
        )
    }
}