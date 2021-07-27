package com.herro.goldtime;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.http.SslError;
import android.os.Bundle;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Currency;
import java.util.Map;
import java.util.Random;

import static com.facebook.FacebookSdk.setAutoLogAppEventsEnabled;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    AppEventsLogger logger;
    Prefs prefs;

    WebView wv;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAnalytics mFirebaseAnalytics;
    DatabaseReference myRef = database.getReference("link_goldtime");

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        prefs = new Prefs(this);
        //getKeyHash();
        checkEvents();
        initOneSignal();
        initFacebook();
        //initEvents();
        //goolgeEvents();

        if(prefs.getLink().contains("-111")){

            //initFirebaseDeepLink();
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final String href = snapshot.getValue(String.class);
                    if(href != null){
                        initAppsFlyer(href);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else {
            initWV(prefs.getLink());
        }

    }

    private void checkEvents() {

        final String ch1 = getApplicationContext().getPackageName().replace(".", "")+"/events";
        String ch2 = prefs.getKey();

        final DatabaseReference mdatabase = database.getReference(ch1 + "/" + ch2);

        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Typedord href = snapshot.getValue(Typedord.class);
                if(href != null ) {
                    if(href.type.contains("reg")) initEventsReg();
                    else if(href.type.contains("dep")) logPurchasedEvent(1, "contenttype", "43", "USD", BigDecimal.valueOf((double) 324));
                    else if(href.type.contains("spend")) logSpentCreditsEvent("content", "type", 2);
                }
                snapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void remove(String ch1) {



    }

    private void initFacebook(){
        logger = AppEventsLogger.newLogger(this);
        FacebookSdk.setAutoInitEnabled(true);
        setAutoLogAppEventsEnabled(true);
        FacebookSdk.fullyInitialize();
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
    }

    private void initAppsFlyer(final String host){
        final String app_n = getApplicationContext().getPackageName().replace(".", "");
        final String key = getRandomString(25);
        prefs.setKey(key);
        AppsFlyerLib.getInstance().registerConversionListener(MainActivity.this, new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> map) {

                if(prefs.getLink() == "-111"){
                    String af_status = map.get("af_status").toString(); // Non-organic

                    if(af_status.contains("Non-organic")){
                        String media_source = map.get("media_source").toString();
                        String campaign = map.get("campaign").toString();
                        String t_url = host + "app_name=" + app_n +  "&media_source=" + media_source + "&campaign=" + campaign + "&af_status=" + af_status + "&type=naming" + "&key=" + key;
                        Log.d(TAG, t_url);
                        initWV(t_url);
                        prefs.setLink(t_url);
                    }else {
                        String url = host + "app_name=" + app_n + "&key=" + key;
                        initWV(url);
                        prefs.setLink(url);
                    }
                }


            }

            @Override
            public void onConversionDataFail(String s) {
                if(prefs.getLink() == "-111"){
                    String url = host + "app_name" + app_n + "&key=" + key;
                    initWV(url);
                }

            }

            @Override
            public void onAppOpenAttribution(Map<String, String> map) {

            }

            @Override
            public void onAttributionFailure(String s) {

            }
        });

        AppsFlyerLib.getInstance().start(MainActivity.this);
    }

    private void initWV(final String deep){

        Log.d("MAIN ACTIVITY deeplink ", deep);
        wv = (WebView) findViewById(R.id.wv);
        wv.post(new Runnable() {
            @Override
            public void run() {
                wv.setVisibility(View.VISIBLE);
                wv.getSettings().setJavaScriptEnabled(true);
                wv.getSettings().setDomStorageEnabled(true);
                wv.setWebViewClient(new AudioBrawser());

                wv.loadUrl(deep);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if(wv != null) wv.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if(wv != null) wv.restoreState(savedInstanceState);
    }

    private void initOneSignal(){

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    private void getKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.heo.goldtime",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initEventsReg(){
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, "Register");
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }

    public void logSpentCreditsEvent (String contentId, String contentType, double totalValue) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        logger.logEvent(AppEventsConstants.EVENT_NAME_SPENT_CREDITS, totalValue, params);
    }

    public void logAchievedLevelEvent (String level) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_LEVEL, level);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, params);
    }

    public void logUnlockedAchievementEvent (String description) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, description);
        logger.logEvent(AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT, params);
    }

    public void logPurchasedEvent (int numItems, String contentType, String contentId, String currency, BigDecimal price) {
        Bundle params = new Bundle();
        params.putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, numItems);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
        logger.logPurchase(price, Currency.getInstance("USD"), params);
    }

    private void initEvents(){
        initEventsReg();
        logSpentCreditsEvent("content", "type", 2);
        logAchievedLevelEvent("5");
        logUnlockedAchievementEvent("desc");
        logPurchasedEvent(1, "contenttype", "43", "USD", BigDecimal.valueOf((double) 324));
    }
    @Override
    public void onBackPressed() {
        if(wv != null && wv.canGoBack()){
            wv.goBack();
        }else{
            super.onBackPressed();
        }

    }


    class AudioBrawser extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {

            prefs.setLink(url.toString());

        }

        @Override
        @TargetApi(android.os.Build.VERSION_CODES.M)
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            //view.loadUrl(prefs.getLink());
            onReceivedError(view, errorResponse.getStatusCode(), errorResponse.getReasonPhrase(), request.getUrl().toString());
        }

        @Override
        @TargetApi(android.os.Build.VERSION_CODES.M)
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(error.toString());
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        @TargetApi(android.os.Build.VERSION_CODES.M)
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
            Log.d("FREEBROWSER", "----------------- ERROR " + rerr.getDescription() + " ---------------");
            // Redirect to deprecated method, so you can use it in all SDK versions
            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
        }


    }

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    private void goolgeEvents(){
        eventReg();
        eventDeposit();
    }

    private void eventReg() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Deposit");
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, String.valueOf(0.65456));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void eventDeposit(){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Registration");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}


