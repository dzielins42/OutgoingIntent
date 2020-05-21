package pl.dzielins42.outgoingintent

import android.app.Activity
import android.os.Bundle

class OutgoingIntentResultReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(OutgoingIntentManagementActivity.newIntent(this, intent.data))
        finish()
    }
}