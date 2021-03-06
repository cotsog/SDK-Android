package eu.intent.sdk.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import eu.intent.sdk.auth.internal.Oauth;
import eu.intent.sdk.ui.activity.ITAuthActivity;

/**
 * An utility class to manage login and logout use cases.
 *
 * @see ITAuthRequest.Builder
 */
public final class ITAuthClient {
    private ITAuthClient() {
    }

    /**
     * Creates a login Intent to be started from your code.
     *
     * @param request the parameters of your authentication request (see ITAuthRequest.Builder)
     */
    public static Intent createLoginActivityIntent(Context context, ITAuthRequest request) {
        Intent intent = new Intent(context, ITAuthActivity.class);
        intent.putExtra(ITAuthActivity.EXTRA_WEB_FORM_URL, request.url.url().toExternalForm());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    /**
     * Starts an authentication activity with the given authentication request.
     *
     * @param activity    the activity you want to open the login activity from.
     * @param requestCode the activity will be opened with startActivityForResult, therefore you'll be able to catch the result in onActivityResult with the given request code.
     * @param request     the parameters of your authentication request (see ITAuthRequest.Builder)
     */
    public static void openLoginActivity(Activity activity, int requestCode, ITAuthRequest request) {
        Intent intent = createLoginActivityIntent(activity, request);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Logs out the current user. This basically clears all the saved tokens.
     */
    public static void logout(Context context) {
        Oauth.getInstance(context).logout();
    }
}
