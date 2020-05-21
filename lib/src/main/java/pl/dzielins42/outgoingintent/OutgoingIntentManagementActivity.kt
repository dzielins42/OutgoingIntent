package pl.dzielins42.outgoingintent

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * It addresses common issues when temporary leaving application context (e.g. to browser), as
 * described in [this](https://www.rallyhealth.com/back-stack-management-with-chrome-custom-tabs) article.
 *
 * This is based on AuthorizationManagementActivity from [AppAuth for Android](https://github.com/openid/AppAuth-Android).
 */
class OutgoingIntentManagementActivity : Activity() {

    private var outgoingIntent: Intent? = null
    private var completeIntent: PendingIntent? = null
    private var cancelIntent: PendingIntent? = null
    private var outgoingIntentStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (savedInstanceState ?: intent.extras)?.let {
            outgoingIntent = it.getParcelable(KEY_OUTGOING_INTENT) as? Intent
            completeIntent = it.getParcelable(KEY_COMPLETE_INTENT) as? PendingIntent
            cancelIntent = it.getParcelable(KEY_CANCEL_INTENT) as? PendingIntent
            outgoingIntentStarted = it.getBoolean(KEY_ALREADY_STARTED, false)
        }

        if (outgoingIntent == null) {
            finish()
            return
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        if (!outgoingIntentStarted) {
            outgoingIntentStarted = true
            startActivity(outgoingIntent)
        } else {
            handleResult(intent.data)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(KEY_OUTGOING_INTENT, outgoingIntent)
        outState.putParcelable(KEY_COMPLETE_INTENT, completeIntent)
        outState.putParcelable(KEY_CANCEL_INTENT, cancelIntent)
        outState.putBoolean(KEY_ALREADY_STARTED, outgoingIntentStarted)
    }

    private fun handleResult(resultData: Uri?) {
        resultData?.let {
            handleComplete(it)
        } ?: kotlin.run { handleCancel() }
    }

    private fun handleComplete(resultData: Uri) {
        val resultIntent = Intent().apply { data = resultData }
        completeIntent?.send(this, 0, resultIntent) ?: kotlin.run {
            setResult(RESULT_OK, resultIntent)
        }
    }

    private fun handleCancel() {
        cancelIntent?.send() ?: kotlin.run {
            setResult(RESULT_CANCELED)
        }
    }

    companion object {
        private const val KEY_OUTGOING_INTENT =
            "OutgoingIntentManagementActivity.OUTGOING_INTENT"
        private const val KEY_COMPLETE_INTENT =
            "OutgoingIntentManagementActivity.COMPLETE_INTENT"
        private const val KEY_CANCEL_INTENT =
            "OutgoingIntentManagementActivity.CANCEL_INTENT"
        private const val KEY_ALREADY_STARTED =
            "OutgoingIntentManagementActivity.KEY_ALREADY_STARTED"

        internal fun newIntent(context: Context, result: Uri?): Intent =
            Intent(context, OutgoingIntentManagementActivity::class.java).apply {
                data = result
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

        fun newIntent(
            context: Context,
            outgoingIntent: Intent,
            completeIntent: PendingIntent? = null,
            cancelIntent: PendingIntent? = null
        ): Intent =
            Intent(context, OutgoingIntentManagementActivity::class.java).apply {
                putExtra(KEY_OUTGOING_INTENT, outgoingIntent)
                putExtra(KEY_COMPLETE_INTENT, completeIntent)
                putExtra(KEY_CANCEL_INTENT, cancelIntent)
            }
    }
}